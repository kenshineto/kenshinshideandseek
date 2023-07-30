package dev.tylerm.khs.game;

import dev.tylerm.khs.game.events.Border;
import dev.tylerm.khs.game.events.Glow;
import dev.tylerm.khs.game.events.Taunt;
import dev.tylerm.khs.game.util.Status;
import dev.tylerm.khs.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;
import java.util.stream.Collectors;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Leaderboard.*;
import static dev.tylerm.khs.configuration.Localization.message;

public class Board {

    private enum Type {
        HIDER,
        SEEKER,
        SPECTATOR,
    }

    private List<UUID> initialSeekers = null;
    private final Map<UUID, Type> Players = new HashMap<>();
    private final Map<UUID, CustomBoard> customBoards = new HashMap<>();
    private final Map<UUID, Integer> hider_kills = new HashMap<>(), seeker_kills = new HashMap<>(), hider_deaths = new HashMap<>(), seeker_deaths = new HashMap<>();

    public boolean contains(Player player) {
        return Players.containsKey(player.getUniqueId());
    }

    public boolean containsUUID(UUID uuid) {
        return Players.containsKey(uuid);
    } 

    public boolean isHider(Player player) {
        return isHider(player.getUniqueId());
    }

    public boolean isHider(UUID uuid) {
        if(!Players.containsKey(uuid)) return false;
        return Players.get(uuid) == Type.HIDER;
    }

    public boolean isSeeker(Player player) {
        return isSeeker(player.getUniqueId());
    }

    public boolean isSeeker(UUID uuid) {
        if(!Players.containsKey(uuid)) return false;
        return Players.get(uuid) == Type.SEEKER;
    }

    public boolean isSpectator(Player player) {
        return isSpectator(player.getUniqueId());
    }

    public boolean isSpectator(UUID uuid) {
        if(!Players.containsKey(uuid)) return false;
        return Players.get(uuid) == Type.SPECTATOR;
    }

    public int sizeHider() {
        return getHiders().size();
    }

    public int sizeSeeker() {
        return getSeekers().size();
    }

    public int size() {
        return getPlayers().size();
    }

    public List<Player> getHiders() {
        return Players.keySet().stream()
                .filter(s -> Players.get(s) == Type.HIDER)
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Player> getSeekers() {
        return Players.keySet().stream()
                .filter(s -> Players.get(s) == Type.SEEKER)
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Player> getSpectators() {
        return Players.keySet().stream()
                .filter(s -> Players.get(s) == Type.SPECTATOR)
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Player> getPlayers() {
        return Players.keySet().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void setInitialSeekers(List<UUID> seekers) {
        initialSeekers = seekers;
    }

    public List<Player> getInitialSeekers() {
        if(initialSeekers == null) return null;
        return initialSeekers.stream().map(u -> {
            return Bukkit.getPlayer(u);
        }).collect(Collectors.toList());
    }

    public Player getPlayer(UUID uuid) {
        if(!Players.containsKey(uuid)) {
            return null;
        }
        return Bukkit.getPlayer(uuid);
    }

    public void addHider(Player player) {
        Players.put(player.getUniqueId(), Type.HIDER);
    }

    public void addSeeker(Player player) {
        Players.put(player.getUniqueId(), Type.SEEKER);
    }

    public void addSpectator(Player player) {
        Players.put(player.getUniqueId(), Type.SPECTATOR);
    }

    public void remove(Player player) {
        Players.remove(player.getUniqueId());
    }

    public boolean onSameTeam(Player player1, Player player2) {
        return Players.get(player1.getUniqueId()) == Players.get(player2.getUniqueId());
    }

    public void reload() {
        Players.replaceAll((u, v) -> Type.HIDER);
        hider_kills.clear();
        seeker_kills.clear();
        hider_deaths.clear();
        seeker_deaths.clear();
    }

    public void addKill(UUID uuid) {
        if(Players.get(uuid) == Type.HIDER) {
            int kills = hider_kills.getOrDefault(uuid, 0);
            hider_kills.put(uuid, kills + 1);
        } else if(Players.get(uuid) == Type.SEEKER) {
            int kills = seeker_kills.getOrDefault(uuid, 0);
            seeker_kills.put(uuid, kills + 1);
        }
    }

    public void addDeath(UUID uuid) {
        if(Players.get(uuid) == Type.HIDER) {
            int kills = hider_deaths.getOrDefault(uuid, 0);
            hider_deaths.put(uuid, kills + 1);
        } else if(Players.get(uuid) == Type.SEEKER) {
            int kills = seeker_deaths.getOrDefault(uuid, 0);
            seeker_deaths.put(uuid, kills + 1);
        }
    }

    public Map<UUID, Integer> getHiderKills() {
        return new HashMap<>(hider_kills);
    }

    public Map<UUID, Integer> getSeekerKills() {
        return new HashMap<>(seeker_kills);
    }

    public Map<UUID, Integer> getHiderDeaths() {
        return new HashMap<>(hider_deaths);
    }

    public Map<UUID, Integer> getSeekerDeaths() {
        return new HashMap<>(seeker_deaths);
    }

    public void createLobbyBoard(Player player) {
        createLobbyBoard(player, true);
    }

    private void createLobbyBoard(Player player, boolean recreate) {
        CustomBoard board = customBoards.get(player.getUniqueId());
        if (recreate || board == null) {
            board = new CustomBoard(player, LOBBY_TITLE);
            board.updateTeams();
        }
        int i=0;
        for(String line : LOBBY_CONTENTS) {
            if (line.equalsIgnoreCase("")) {
                board.addBlank();
            } else if (line.contains("{COUNTDOWN}")) {
                if (!lobbyCountdownEnabled) {
                    board.setLine(String.valueOf(i), line.replace("{COUNTDOWN}", COUNTDOWN_ADMINSTART));
                } else if (Main.getInstance().getGame().getLobbyTime() == -1) {
                    board.setLine(String.valueOf(i), line.replace("{COUNTDOWN}", COUNTDOWN_WAITING));
                } else {
                    board.setLine(String.valueOf(i), line.replace("{COUNTDOWN}", COUNTDOWN_COUNTING.replace("{AMOUNT}",Main.getInstance().getGame().getLobbyTime()+"")));
                }
            } else if (line.contains("{COUNT}")) {
                board.setLine(String.valueOf(i), line.replace("{COUNT}", getPlayers().size()+""));
            } else if (line.contains("{SEEKER%}")) {
                board.setLine(String.valueOf(i), line.replace("{SEEKER%}", getSeekerPercent()+""));
            } else if (line.contains("{HIDER%}")) {
                board.setLine(String.valueOf(i), line.replace("{HIDER%}", getHiderPercent() + ""));
            } else if (line.contains("{MAP}")) {
                board.setLine(String.valueOf(i), line.replace("{MAP}", getMapName() + ""));
            } else {
                board.setLine(String.valueOf(i), line);
            }
            i++;
        }
        board.display();
        customBoards.put(player.getUniqueId(), board);
    }

    public String getMapName() {
        dev.tylerm.khs.configuration.Map map = Main.getInstance().getGame().getCurrentMap();
        if(map == null) return "Invalid";
        else return map.getName();
    }

    public void createGameBoard(Player player) {
        createGameBoard(player, true);
    }

    private void createGameBoard(Player player, boolean recreate) {
        CustomBoard board = customBoards.get(player.getUniqueId());
        if (recreate || board == null) {
            board = new CustomBoard(player, GAME_TITLE);
            board.updateTeams();
        }

        int timeLeft = Main.getInstance().getGame().getTimeLeft();
        Status status = Main.getInstance().getGame().getStatus();

        Taunt taunt = Main.getInstance().getGame().getTaunt();
        Border worldBorder = Main.getInstance().getGame().getCurrentMap().getWorldBorder();
        Glow glow = Main.getInstance().getGame().getGlow();

        int i = 0;
        for(String line : GAME_CONTENTS) {
            if (line.equalsIgnoreCase("")) {
                board.addBlank();
            } else {
                if (line.contains("{TIME}")) {
                    String value = timeLeft/60 + "m" + timeLeft%60 + "s";
                    board.setLine(String.valueOf(i), line.replace("{TIME}", value));
                } else if (line.contains("{TEAM}")) {
                    String value = getTeam(player);
                    board.setLine(String.valueOf(i), line.replace("{TEAM}", value));
                } else if (line.contains("{BORDER}")) {
                    if (!Main.getInstance().getGame().getCurrentMap().isWorldBorderEnabled()) continue;
                    if (status == Status.STARTING) {
                        board.setLine(String.valueOf(i), line.replace("{BORDER}", BORDER_COUNTING.replace("{AMOUNT}", "0")));
                    } else if (!worldBorder.isRunning()) {
                        board.setLine(String.valueOf(i), line.replace("{BORDER}", BORDER_COUNTING.replaceFirst("\\{AMOUNT}", worldBorder.getDelay()/60+"").replaceFirst("\\{AMOUNT}", worldBorder.getDelay()%60+"")));
                    } else {
                        board.setLine(String.valueOf(i), line.replace("{BORDER}", BORDER_DECREASING));
                    }
                } else if (line.contains("{TAUNT}")) {
                    if (!tauntEnabled) continue;
                    if (taunt == null || status == Status.STARTING) {
                        board.setLine(String.valueOf(i), line.replace("{TAUNT}", TAUNT_COUNTING.replace("{AMOUNT}", "0")));
                    } else if (!tauntLast && sizeHider() == 1) {
                        board.setLine(String.valueOf(i), line.replace("{TAUNT}", TAUNT_EXPIRED));
                    } else if (!taunt.isRunning()) {
                        board.setLine(String.valueOf(i), line.replace("{TAUNT}", TAUNT_COUNTING.replaceFirst("\\{AMOUNT}", taunt.getDelay() / 60 + "").replaceFirst("\\{AMOUNT}", taunt.getDelay() % 60 + "")));
                    } else {
                        board.setLine(String.valueOf(i), line.replace("{TAUNT}", TAUNT_ACTIVE));
                    }
                } else if (line.contains("{GLOW}")) {
                    if (!glowEnabled)  continue;
                    if (glow == null || status == Status.STARTING || !glow.isRunning()) {
                        board.setLine(String.valueOf(i), line.replace("{GLOW}", GLOW_INACTIVE));
                    } else {
                        board.setLine(String.valueOf(i), line.replace("{GLOW}", GLOW_ACTIVE));
                    }
                } else if (line.contains("{#SEEKER}")) {
                    board.setLine(String.valueOf(i), line.replace("{#SEEKER}", getSeekers().size()+""));
                } else if (line.contains("{#HIDER}")) {
                    board.setLine(String.valueOf(i), line.replace("{#HIDER}", getHiders().size()+""));
                } else if (line.contains("{MAP}")) {
                    board.setLine(String.valueOf(i), line.replace("{MAP}", getMapName() + ""));
                } else {
                    board.setLine(String.valueOf(i), line);
                }
            }
            i++;
        }
        board.display();
        customBoards.put(player.getUniqueId(), board);
    }

    public void removeBoard(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        player.setScoreboard(manager.getMainScoreboard());
        customBoards.remove(player.getUniqueId());
    }

    public void reloadLobbyBoards() {
        for(Player player : getPlayers())
            createLobbyBoard(player, false);
    }

    public void reloadGameBoards() {
        for(Player player : getPlayers())
            createGameBoard(player, false);
    }

    public void reloadBoardTeams() {
        for(CustomBoard board : customBoards.values())
            board.updateTeams();
    }

    private String getSeekerPercent() {
        int size = size();
        if (size < 2)
            return " --";
        else
            return " "+(int)(100*(1.0/size));
    }

    private String getHiderPercent() {
        int size = size();
        if (size < 2)
            return " --";
        else
            return " "+(int)(100-100*(1.0/size));
    }

    private String getTeam(Player player) {
        if (isHider(player)) return message("HIDER_TEAM_NAME").toString();
        else if (isSeeker(player)) return message("SEEKER_TEAM_NAME").toString();
        else if (isSpectator(player)) return message("SPECTATOR_TEAM_NAME").toString();
        else return ChatColor.WHITE + "UNKNOWN";
    }

    public void cleanup() {
        Players.clear();;
        initialSeekers = null;
        customBoards.clear();
    }

}

@SuppressWarnings("deprecation")
class CustomBoard {

    private final Scoreboard board;
    private final Objective obj;
    private final Player player;
    private final Map<String,Line> LINES;
    private int blanks;
    private boolean displayed;

    public CustomBoard(Player player, String title) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        assert manager != null;
        this.board = manager.getNewScoreboard();
        this.LINES = new HashMap<>();
        this.player = player;
        if (Main.getInstance().supports(13)) {
            this.obj = board.registerNewObjective(
                    "Scoreboard", "dummy", ChatColor.translateAlternateColorCodes('&', title));
        } else {
            this.obj = board.registerNewObjective("Scoreboard", "dummy");
            this.obj.setDisplayName(ChatColor.translateAlternateColorCodes('&', title));
        }
        this.blanks = 0;
        this.displayed = false;
        this.updateTeams();
    }

    public void updateTeams() {
        try{ board.registerNewTeam("Hider"); } catch (Exception ignored) {}
        try{ board.registerNewTeam("Seeker"); } catch (Exception ignored) {}
        Team hiderTeam = board.getTeam("Hider");
        assert hiderTeam != null;
        for(String entry : hiderTeam.getEntries())
            hiderTeam.removeEntry(entry);
        for(Player player : Main.getInstance().getBoard().getHiders())
            hiderTeam.addEntry(player.getName());
        Team seekerTeam = board.getTeam("Seeker");
        assert seekerTeam != null;
        for(String entry : seekerTeam.getEntries())
            seekerTeam.removeEntry(entry);
        for(Player player  : Main.getInstance().getBoard().getSeekers())
            seekerTeam.addEntry(player.getName());
        if (Main.getInstance().supports(9)) {
            if (nameTagsVisible) {
                hiderTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OWN_TEAM);
                seekerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.FOR_OTHER_TEAMS);
            } else {
                hiderTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
                seekerTeam.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            }
            hiderTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
            seekerTeam.setOption(Team.Option.COLLISION_RULE, Team.OptionStatus.NEVER);
        } else {
            if (nameTagsVisible) {
                hiderTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OTHER_TEAMS);
                seekerTeam.setNameTagVisibility(NameTagVisibility.HIDE_FOR_OWN_TEAM);
            } else {
                hiderTeam.setNameTagVisibility(NameTagVisibility.NEVER);
                seekerTeam.setNameTagVisibility(NameTagVisibility.NEVER);
            }
        }
        hiderTeam.setPrefix(message("HIDER_TEAM_NAME").toString() + " " + ChatColor.RESET);
        seekerTeam.setPrefix(message("SEEKER_TEAM_NAME").toString() + " " + ChatColor.RESET);
    }

    public void setLine(String key, String message) {
        Line line = LINES.get(key);
        if (line == null)
            addLine(key, ChatColor.translateAlternateColorCodes('&',message));
        else
            updateLine(key, ChatColor.translateAlternateColorCodes('&',message));
    }

    private void addLine(String key, String message) {
        Score score = obj.getScore(message);
        score.setScore(LINES.values().size()+1);
        Line line = new Line(LINES.values().size()+1, message);
        LINES.put(key, line);
    }

    public void addBlank() {
        if (displayed) return;
        StringBuilder temp = new StringBuilder();
        for(int i = 0; i <= blanks; i ++)
            temp.append(ChatColor.RESET);
        blanks++;
        addLine("blank"+blanks, temp.toString());
    }

    private void updateLine(String key, String message) {
        Line line = LINES.get(key);
        board.resetScores(line.getMessage());
        line.setMessage(message);
        Score newScore = obj.getScore(message);

        newScore.setScore(line.getScore());
    }

    public void display() {
        displayed = true;
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);
        player.setScoreboard(board);
    }

}

class Line {

    private final int score;
    private String message;

    public Line(int score, String message) {
        this.score = score;
        this.message = message;
    }

    public int getScore() {
        return score;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
