package dev.tylerm.khs.configuration;

import java.util.Collections;
import java.util.List;

public class Leaderboard {

    public static String
            LOBBY_TITLE,
            GAME_TITLE,
            COUNTDOWN_WAITING,
            COUNTDOWN_COUNTING,
            COUNTDOWN_ADMINSTART,
            TAUNT_COUNTING,
            TAUNT_ACTIVE,
            TAUNT_EXPIRED,
            GLOW_ACTIVE,
            GLOW_INACTIVE,
            BORDER_COUNTING,
            BORDER_DECREASING;

    public static List<String>
            LOBBY_CONTENTS,
            GAME_CONTENTS;

    public static void loadLeaderboard() {

        ConfigManager leaderboard = ConfigManager.create("leaderboard.yml");

        LOBBY_TITLE = leaderboard.getString("lobby.title");
        GAME_TITLE = leaderboard.getString("game.title");
        LOBBY_CONTENTS = leaderboard.getStringList("lobby.content");
        Collections.reverse(LOBBY_CONTENTS);
        GAME_CONTENTS = leaderboard.getStringList("game.content");
        Collections.reverse(GAME_CONTENTS);
        COUNTDOWN_WAITING = leaderboard.getString("countdown.waiting");
        COUNTDOWN_COUNTING = leaderboard.getString("countdown.counting");
        COUNTDOWN_ADMINSTART = leaderboard.getString("countdown.adminStart");
        TAUNT_COUNTING = leaderboard.getString("taunt.counting");
        TAUNT_ACTIVE = leaderboard.getString("taunt.active");
        TAUNT_EXPIRED = leaderboard.getString("taunt.expired");
        GLOW_ACTIVE = leaderboard.getString("glow.active");
        GLOW_INACTIVE = leaderboard.getString("glow.inactive");
        BORDER_COUNTING = leaderboard.getString("border.counting");
        BORDER_DECREASING = leaderboard.getString("border.decreasing");

        leaderboard.saveConfig();

    }

}
