package dev.tylerm.khs.util.packet;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;

public class AbstractPacket {

    private static final ProtocolManager protocolManager;
    static {
        protocolManager = ProtocolLibrary.getProtocolManager();
    }

    protected final PacketContainer packet;

    protected AbstractPacket(PacketType type){
        packet = protocolManager.createPacket(type);
        packet.getModifier().writeDefaults();
    }

    public void send(Player player){
        protocolManager.sendServerPacket(player, packet);
    }

}
