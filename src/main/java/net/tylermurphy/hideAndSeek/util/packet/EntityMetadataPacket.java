package net.tylermurphy.hideAndSeek.util.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.WrappedDataValue;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
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

        // thank you to
        // https://www.spigotmc.org/threads/unable-to-modify-entity-metadata-packet-using-protocollib-1-19-3.582442/

        try {
            // 1.19.3 And Up
            Class.forName("com.comphenix.protocol.wrappers.WrappedDataValue");

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

        } catch (ClassCastException | ClassNotFoundException ignored) {
            // 1.9 to 1.19.2 And Up
            packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
        }

    }

}