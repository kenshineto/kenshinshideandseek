package dev.tylerm.khs.game.events;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Map;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Localization.message;

public class Border {

    private int delay;
    private boolean running;
    private final Map map;
    private int currentSize;

    public Border(Map map) {
        this.map = map;
        this.delay = (int) (60 * map.getWorldBorderData().getY());
        this.currentSize = (int) map.getWorldBorderData().getX();
    }

    public void update() {
        if (delay == 30 && !running) {
            Main.getInstance().getGame().broadcastMessage(worldBorderPrefix + message("WORLDBORDER_WARN"));
        } else if (delay == 0) {
            if (running) {
                delay = (int) (60 * map.getWorldBorderData().getY());
                running = false;
            }
            else decreaseWorldBorder();
        }
        delay--;
    }

    private void decreaseWorldBorder() {
        if (currentSize == 100) return;
        if(map.getGameSpawn().load() == null) return;
        int change = (int) map.getWorldBorderData().getZ();
        if (currentSize-change < 100) {
            change = currentSize-100;
        }
        running = true;
        Main.getInstance().getGame().broadcastMessage(worldBorderPrefix + message("WORLDBORDER_DECREASING").addAmount(change));
        currentSize -= map.getWorldBorderData().getZ();
        org.bukkit.WorldBorder border = map.getGameSpawn().load().getWorldBorder();
        border.setSize(border.getSize()-change,30);
        delay = 30;
    }

    public void resetWorldBorder() {
        if(map.getGameSpawn().load() == null) return;
        org.bukkit.WorldBorder border = map.getGameSpawn().load().getWorldBorder();
        if (map.isWorldBorderEnabled()) {
            border.setSize(map.getWorldBorderData().getX());
            border.setCenter(map.getWorldBorderPos().getX(), map.getWorldBorderPos().getY());
            currentSize = (int) map.getWorldBorderData().getX();
        } else {
            border.setSize(30000000);
            border.setCenter(0, 0);
        }
        delay = (int) (60 * map.getWorldBorderData().getY());
    }

    public int getDelay() {
        return delay;
    }

    public boolean isRunning() {
        return running;
    }

}