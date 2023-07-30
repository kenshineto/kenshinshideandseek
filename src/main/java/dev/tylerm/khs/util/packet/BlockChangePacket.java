package dev.tylerm.khs.util.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.jetbrains.annotations.NotNull;

public class BlockChangePacket extends AbstractPacket {

    public BlockChangePacket(){
        super(PacketType.Play.Server.BLOCK_CHANGE);
    }

    public void setBlockPosition(@NotNull Location location){
        super.packet.getBlockPositionModifier().write(0, new BlockPosition(location.toVector()));
    }

    public void setMaterial(Material material){
        super.packet.getBlockData().write(0, WrappedBlockData.createData(material));
    }

}
