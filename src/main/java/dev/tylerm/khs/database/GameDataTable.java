/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2021-2022 Tyler Murphy.
 *
 * Kenshins Hide and Seek free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * he Free Software Foundation version 3.
 *
 * Kenshins Hide and Seek is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

package dev.tylerm.khs.database;

import dev.tylerm.khs.Main;
import dev.tylerm.khs.database.util.PlayerInfo;
import dev.tylerm.khs.game.Board;
import dev.tylerm.khs.game.util.WinType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

public class GameDataTable {

    private final Map<UUID, PlayerInfo> CACHE = new HashMap<>();
    private final Database database;

    protected GameDataTable(Database database) {

        String sql = "CREATE TABLE IF NOT EXISTS hs_data (\n"
                + "	uuid BINARY(16) PRIMARY KEY,\n"
                + "	hider_wins int NOT NULL,\n"
                + "	seeker_wins int NOT NULL,\n"
                + "	hider_games int NOT NULL,\n"
                + "	seeker_games int NOT NULL,\n"
                + "	hider_kills int NOT NULL,\n"
                + "	seeker_kills int NOT NULL,\n"
                + "	hider_deaths int NOT NULL,\n"
                + "	seeker_deaths int NOT NULL\n"
                + ");";

        try(Connection connection = database.connect(); Statement statement = connection.createStatement()) {
            statement.executeUpdate(sql);
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }

        this.database = database;
    }

    @Nullable
    public PlayerInfo getInfo(@Nullable UUID uuid) {
        if (uuid == null) return null;
        if(CACHE.containsKey(uuid)) return CACHE.get(uuid);
        String sql = "SELECT * FROM hs_data WHERE uuid = ?;";
        try(Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBytes(1, database.encodeUUID(uuid));
            ResultSet rs  = statement.executeQuery();
            if (rs.next()) {
                PlayerInfo info = new PlayerInfo(
                        uuid,
                        rs.getInt("hider_wins"),
                        rs.getInt("seeker_wins"),
                        rs.getInt("hider_games"),
                        rs.getInt("seeker_games"),
                        rs.getInt("hider_kills"),
                        rs.getInt("seeker_kills"),
                        rs.getInt("hider_deaths"),
                        rs.getInt("seeker_deaths")
                );
                rs.close();
                CACHE.put(uuid, info);
                return info;
            }
            rs.close();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public PlayerInfo getInfoRanking(@NotNull String order, int place) {
        String sql = "SELECT * FROM hs_data ORDER BY "+order+" DESC LIMIT 1 OFFSET ?;";
        try(Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, place-1);
            ResultSet rs  = statement.executeQuery();
            if (rs.next()) {
                UUID uuid = database.decodeUUID(rs.getBytes("uuid"));
                PlayerInfo info = new PlayerInfo(
                        uuid,
                        rs.getInt("hider_wins"),
                        rs.getInt("seeker_wins"),
                        rs.getInt("hider_games"),
                        rs.getInt("seeker_games"),
                        rs.getInt("hider_kills"),
                        rs.getInt("seeker_kills"),
                        rs.getInt("hider_deaths"),
                        rs.getInt("seeker_deaths")
                );
                rs.close();
                CACHE.put(uuid, info);
                return info;
            }
            rs.close();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public List<PlayerInfo> getInfoPage(int page) {
        String sql = "SELECT * FROM hs_data ORDER BY (hider_wins + seeker_wins) DESC LIMIT 10 OFFSET ?;";
        try(Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, (page-1)*10);
            ResultSet rs  = statement.executeQuery();
            List<PlayerInfo> infoList = new ArrayList<>();
            while(rs.next()) {
                PlayerInfo info = new PlayerInfo(
                        database.decodeUUID(rs.getBytes("uuid")),
                        rs.getInt("hider_wins"),
                        rs.getInt("seeker_wins"),
                        rs.getInt("hider_games"),
                        rs.getInt("seeker_games"),
                        rs.getInt("hider_kills"),
                        rs.getInt("seeker_kills"),
                        rs.getInt("hider_deaths"),
                        rs.getInt("seeker_deaths")
                );
                infoList.add(info);
            }
            rs.close();
            return infoList;
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    @Nullable
    public Integer getRanking(@NotNull String order, @NotNull UUID uuid) {
        String sql = "SELECT count(*) AS total FROM hs_data WHERE "+order+" >= (SELECT "+order+" FROM hs_data WHERE uuid = ?) AND "+order+" > 0;";
        try(Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBytes(1, database.encodeUUID(uuid));
            ResultSet rs  = statement.executeQuery();
            if (rs.next()) {
                return rs.getInt("total");
            }
            rs.close();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("SQL Error: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public void addWins(
            @NotNull Board board,
            @NotNull List<UUID> uuids,
            @NotNull List<UUID> winners,
            @NotNull Map<UUID,Integer> hider_kills,
            @NotNull Map<UUID,Integer> hider_deaths,
            @NotNull Map<UUID,Integer> seeker_kills,
            @NotNull Map<UUID,Integer> seeker_deaths,
            @NotNull WinType type
    ) {
        for(UUID uuid : uuids) {
            PlayerInfo info = getInfo(uuid);
            if(info == null){
                info = new PlayerInfo(uuid, 0, 0, 0, 0, 0, 0, 0, 0);
            }
            updateInfo(
                    database.encodeUUID(info.getUniqueId()),
                    info.getHiderWins() + (winners.contains(uuid) && type == WinType.HIDER_WIN ? 1 : 0),
                    info.getSeekerWins() + (winners.contains(uuid) && type == WinType.SEEKER_WIN ? 1 : 0),
                    info.getHiderGames() + (board.isHider(uuid) || (board.isSeeker(uuid) && winners.contains(uuid)) ? 1 : 0),
                    info.getSeekerGames() + (board.isSeeker(uuid) && winners.contains(uuid) ? 1 : 0),
                    info.getHiderKills() + hider_kills.getOrDefault(uuid, 0),
                    info.getSeekerKills() + seeker_kills.getOrDefault(uuid, 0),
                    info.getHiderDeaths() + hider_deaths.getOrDefault(uuid, 0),
                    info.getSeekerDeaths() + seeker_deaths.getOrDefault(uuid, 0)
            );
        }
    }

    protected void updateInfo(byte[] uuid, int hider_wins, int seeker_wins, int hider_games, int seeker_games, int hider_kills, int seeker_kills, int hider_deaths, int seeker_deaths){
        String sql = "REPLACE INTO hs_data (uuid, hider_wins, seeker_wins, hider_games, seeker_games, hider_kills, seeker_kills, hider_deaths, seeker_deaths) VALUES (?,?,?,?,?,?,?,?,?)";
        try(Connection connection = database.connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setBytes(1, uuid);
            statement.setInt(2, hider_wins);
            statement.setInt(3, seeker_wins);
            statement.setInt(4, hider_games);
            statement.setInt(5, seeker_games);
            statement.setInt(6, hider_kills);
            statement.setInt(7, seeker_kills);
            statement.setInt(8, hider_deaths);
            statement.setInt(9, seeker_deaths);
            statement.execute();
        } catch (SQLException e) {
            Main.getInstance().getLogger().severe("SQL Error: " + e.getMessage());
            e.printStackTrace();
        } finally {
            CACHE.remove(database.decodeUUID(uuid));
        }
    }

}
