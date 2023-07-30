package dev.tylerm.khs.game.events;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import dev.tylerm.khs.util.packet.EntityMetadataPacket;
import dev.tylerm.khs.Main;
import org.bukkit.entity.Player;

import static dev.tylerm.khs.configuration.Config.*;

public class Glow {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    private int glowTime;
    private boolean running;

    public Glow() {
        this.glowTime = 0;
    }

    public void onProjectile() {
        if (glowStackable) glowTime += glowLength;
        else glowTime = glowLength;
        running = true;
    }

    private void sendPackets() {
        for (Player hider : Main.getInstance().getBoard().getHiders())
            for (Player seeker : Main.getInstance().getBoard().getSeekers())
                setGlow(hider, seeker, true);
    }

    public void update() {
        if(alwaysGlow){
            sendPackets();
            return;
        }
        if (running) {
            sendPackets();
            glowTime--;
            glowTime = Math.max(glowTime, 0);
            if (glowTime == 0) {
                stopGlow();
            }
        }
    }

    private void stopGlow() {
        running = false;
        for (Player hider : Main.getInstance().getBoard().getHiders()) {
            for (Player seeker : Main.getInstance().getBoard().getSeekers()) {
                setGlow(hider, seeker, false);
            }
        }
    }

    public boolean isRunning() {
        return running;
    }

    public void setGlow(Player player, Player target, boolean glowing) {

        EntityMetadataPacket packet = new EntityMetadataPacket();
        packet.setEntity(target);
        packet.setGlow(glowing);
        packet.writeMetadata();
        packet.send(player);

    }

}
