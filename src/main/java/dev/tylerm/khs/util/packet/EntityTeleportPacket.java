package dev.tylerm.khs.util.packet;

import com.comphenix.protocol.PacketType;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class EntityTeleportPacket extends AbstractPacket {

    public EntityTeleportPacket(){
        super(PacketType.Play.Server.ENTITY_TELEPORT);
    }

    public void setEntity(@NotNull Entity entity){
        super.packet.getIntegers().write(0, entity.getEntityId());
    }

    public void setX(double x){
        super.packet.getDoubles().write(0, x);
    }

    public void setY(double y){
        super.packet.getDoubles().write(1, y);
    }

    public void setZ(double z){
        super.packet.getDoubles().write(2, z);
    }

}
