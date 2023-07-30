package dev.tylerm.khs.util.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;

import dev.tylerm.khs.Main;

import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class EntityMetadataPacket extends AbstractPacket {

    private final WrappedDataWatcher watcher;
    private final WrappedDataWatcher.Serializer serializer;

    public EntityMetadataPacket(){
        super(PacketType.Play.Server.ENTITY_METADATA);
        watcher = new WrappedDataWatcher();
        serializer = WrappedDataWatcher.Registry.get(Byte.class);
    }

    public void setEntity(@NotNull Entity target){
        super.packet.getIntegers().write(0, target.getEntityId());
        watcher.setEntity(target);
    }

    public void setGlow(boolean glowing){
        if (glowing) {
            watcher.setObject(0, serializer, (byte) (0x40));
        } else {
            watcher.setObject(0, serializer, (byte) (0x0));
        }
    }

    public void writeMetadata() {

        if (Main.getInstance().supports(19, 3)) {

            final List<WrappedDataValue> wrappedDataValueList = new ArrayList<>();

            for(final WrappedWatchableObject entry : watcher.getWatchableObjects()) {
                if(entry == null) continue;

                final WrappedDataWatcher.WrappedDataWatcherObject watcherObject = entry.getWatcherObject();
                wrappedDataValueList.add(
                        new WrappedDataValue(
                                watcherObject.getIndex(),
                                watcherObject.getSerializer(),
                                entry.getRawValue()
                        )
                );
            }

            packet.getDataValueCollectionModifier().write(0, wrappedDataValueList);

        } else {

            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        
        }

    }

}
