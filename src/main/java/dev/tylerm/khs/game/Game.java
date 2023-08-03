/*
 * This file is part of Kenshins Hide and Seek
 *
 * Copyright (c) 2020-2021. Tyler Murphy
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

package dev.tylerm.khs.game;

import com.cryptomorin.xseries.messages.ActionBar;
import com.cryptomorin.xseries.messages.Titles;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import dev.tylerm.khs.game.events.Glow;
import dev.tylerm.khs.game.events.Taunt;
import dev.tylerm.khs.game.listener.RespawnHandler;
import dev.tylerm.khs.game.util.CountdownDisplay;
import dev.tylerm.khs.game.util.Status;
import dev.tylerm.khs.Main;
import dev.tylerm.khs.configuration.Map;
import dev.tylerm.khs.configuration.Maps;
import dev.tylerm.khs.game.util.WinType;
import org.bukkit.*;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

import static dev.tylerm.khs.configuration.Config.*;
import static dev.tylerm.khs.configuration.Localization.message;

public class Game {

	private final Taunt taunt;
	private final Glow glow;

	private final Board board;

	private Status status;

	private Map currentMap;

	private int gameTick;
	private int lobbyTimer;
	private int startingTimer;
	private int gameTimer;
	private boolean hiderLeft;

	public Game(Map map, Board board) {

		this.currentMap = map;

		this.taunt = new Taunt();
		this.glow = new Glow();

		this.status = Status.STANDBY;

		this.board = board;

		this.gameTick = 0;
		this.lobbyTimer = -1;
		this.startingTimer = -1;
		this.gameTimer = 0;
		this.hiderLeft = false;
	}

	public Status getStatus(){
		return status;
	}

	public int getTimeLeft(){
		return gameTimer;
	}

	public int getLobbyTime(){
		return lobbyTimer;
	}

	public Glow getGlow(){
		return glow;
	}

	public Taunt getTaunt(){
		return taunt;
	}

	public void start() {
		List<Player> seekers = new ArrayList<>(startingSeekerCount);
        List<Player> pool = board.getPlayers();
        for (int i = 0; i < startingSeekerCount; i++) {
		    try {
                int rand = (int)(Math.random() * pool.size());
		    	seekers.add(pool.remove(rand));
		    } catch (Exception e){
		    	Main.getInstance().getLogger().warning("Failed to select random seeker.");
		    	return;
		    }
        }
		start(seekers);
	}

	public void start(List<Player> seekers) {
		if (mapSaveEnabled) currentMap.getWorldLoader().rollback();
		board.reload();
        board.setInitialSeekers(seekers.stream().map(Player::getUniqueId).collect(Collectors.toList()));
        seekers.forEach(seeker -> {
		    board.addSeeker(seeker);
		    PlayerLoader.loadSeeker(seeker, currentMap);
        });
		board.getPlayers().forEach(player -> {
			if(board.isSeeker(player)) return;
			board.addHider(player);
			PlayerLoader.loadHider(player, currentMap);
		});
		board.getPlayers().forEach(board::createGameBoard);
		currentMap.getWorldBorder().resetWorldBorder();
		if (gameLength > 0) gameTimer = gameLength;
		status = Status.STARTING;
		startingTimer = hidingTimer;
	}

	private void stop(WinType type) {
		status = Status.ENDING;
		List<UUID> players = board.getPlayers().stream().map(Entity::getUniqueId).collect(Collectors.toList());
		if (type == WinType.HIDER_WIN) {
			List<UUID> winners = board.getHiders().stream().map(Entity::getUniqueId).collect(Collectors.toList());
			Main.getInstance().getDatabase().getGameData().addWins(board, players, winners, board.getHiderKills(), board.getHiderDeaths(), board.getSeekerKills(), board.getSeekerDeaths(), type);
		} else if (type == WinType.SEEKER_WIN) {
			List<UUID> winners = new ArrayList<>();
			board.getInitialSeekers().forEach(p -> {
                winners.add(p.getUniqueId());
            });
            if (!waitTillNoneLeft && board.getHiders().size() == 1) {
                winners.add(board.getHiders().get(0).getUniqueId());
            }
			Main.getInstance().getDatabase().getGameData().addWins(board, players, winners, board.getHiderKills(), board.getHiderDeaths(), board.getSeekerKills(), board.getSeekerDeaths(), type);
		}
		Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getInstance(), this::end, endGameDelay*20);
	}

	public void end() {
		board.getPlayers().forEach(PlayerLoader::unloadPlayer);
		currentMap.getWorldBorder().resetWorldBorder();
		Map nextMap = Maps.getRandomMap();
		if(nextMap != null) this.currentMap = nextMap;
		board.getPlayers().forEach(player -> {
			if (leaveOnEnd) {
				board.removeBoard(player);
				board.remove(player);
				handleBungeeLeave(player);
			} else {
				currentMap.getLobby().teleport(player);
				board.createLobbyBoard(player);
				board.addHider(player);
				PlayerLoader.joinPlayer(player, currentMap);
			}
		});
		RespawnHandler.temp_loc.clear();
		if (mapSaveEnabled) currentMap.getWorldLoader().unloadMap();
		board.reloadLobbyBoards();
		status = Status.ENDED;
	}

	public void join(Player player) {
		if (status != Status.STARTING && status != Status.PLAYING) {
			if(saveInventory) {
				ItemStack[] data = player.getInventory().getContents();
				Main.getInstance().getDatabase().getInventoryData().saveInventory(player.getUniqueId(), data);
			}
			PlayerLoader.joinPlayer(player, currentMap);
			board.addHider(player);
			board.createLobbyBoard(player);
			board.reloadLobbyBoards();
			if (announceMessagesToNonPlayers) Bukkit.broadcastMessage(messagePrefix + message("GAME_JOIN").addPlayer(player));
			else broadcastMessage(messagePrefix + message("GAME_JOIN").addPlayer(player));
		} else {
			PlayerLoader.loadSpectator(player, currentMap);
			board.addSpectator(player);
			board.createGameBoard(player);
			player.sendMessage(messagePrefix + message("GAME_JOIN_SPECTATOR"));
		}
	}

	public void leave(Player player) {
		PlayerLoader.unloadPlayer(player);
		if(saveInventory) {
			ItemStack[] data = Main.getInstance().getDatabase().getInventoryData().getInventory(player.getUniqueId());
			try {
				player.getInventory().setContents(data);
			} catch (NullPointerException ignored){}
		}
		if (announceMessagesToNonPlayers) Bukkit.broadcastMessage(messagePrefix + message("GAME_LEAVE").addPlayer(player));
		else broadcastMessage(messagePrefix + message("GAME_LEAVE").addPlayer(player));
		if (board.isHider(player) && status != Status.ENDING && status != Status.STANDBY) {
			hiderLeft = true;
		}
		board.removeBoard(player);
		board.remove(player);
		if (status == Status.STANDBY) {
			board.reloadLobbyBoards();
		} else {
			board.reloadGameBoards();
			board.reloadBoardTeams();
		}
		handleBungeeLeave(player);
	}

	@SuppressWarnings("UnstableApiUsage")
	private void handleBungeeLeave(Player player) {
		if (bungeeLeave) {
			ByteArrayDataOutput out = ByteStreams.newDataOutput();
			out.writeUTF("Connect");
			out.writeUTF(leaveServer);
			player.sendPluginMessage(Main.getInstance(), "BungeeCord", out.toByteArray());
		} else {
			exitPosition.teleport(player);
		}
	}

	public void onTick() {
		if (currentMap == null || currentMap.isNotSetup()) return;
		if (status == Status.STANDBY) whileWaiting();
		else if (status == Status.STARTING) whileStarting();
		else if (status == Status.PLAYING) whilePlaying();
		gameTick++;
	}

	private void whileWaiting() {
		if (!lobbyCountdownEnabled) return;
		if (lobbyMin <= board.size()) {
			if (lobbyTimer < 0)
				lobbyTimer = countdown;
			if (board.size() >= changeCountdown)
				lobbyTimer = Math.min(lobbyTimer, 10);
			if (gameTick % 20 == 0) {
				lobbyTimer--;
				board.reloadLobbyBoards();
			}
			if (lobbyTimer == 0) {
				start();
			}
		} else {
			lobbyTimer = -1;
			if (gameTick % 20 == 0) {
				board.reloadLobbyBoards();
			}
		}
	}

	private void whileStarting() {
		if(gameTick % 20 == 0) {
			if (startingTimer % 5 == 0 || startingTimer < 5) {
				String message;
				if (startingTimer == 0) {
					message = message("START").toString();
					status = Status.PLAYING;
					board.getPlayers().forEach(player -> {
						PlayerLoader.resetPlayer(player, board);
						if(board.isSeeker(player)){
							currentMap.getGameSpawn().teleport(player);
						}
					});
				} else if (startingTimer == 1){
					message = message("START_COUNTDOWN_LAST").addAmount(startingTimer).toString();
				} else {
					message = message("START_COUNTDOWN").addAmount(startingTimer).toString();
				}
				board.getPlayers().forEach(player -> {
					if (countdownDisplay == CountdownDisplay.CHAT) {
						player.sendMessage(messagePrefix + message);
					} else if (countdownDisplay == CountdownDisplay.ACTIONBAR) {
						ActionBar.clearActionBar(player);
						ActionBar.sendActionBar(player, messagePrefix + message);
					} else if (countdownDisplay == CountdownDisplay.TITLE && startingTimer != 30) {
						Titles.clearTitle(player);
						Titles.sendTitle(player, 10, 40, 10, " ", message);
					}
				});
			}
			startingTimer--;
		}
		checkWinConditions();
	}

	private void whilePlaying() {
		for(Player hider : board.getHiders()) {
			int distance = 100, temp = 100;
			for(Player seeker : board.getSeekers()) {
				try {
					temp = (int) hider.getLocation().distance(seeker.getLocation());
				} catch (Exception e) {
					//Players in different worlds, NOT OK!!!
				}
				if (distance > temp) {
					distance = temp;
				}
			}
			if (seekerPing) switch(gameTick %10) {
				case 0:
					if (distance < seekerPingLevel1) heartbeatSound.play(hider, seekerPingLeadingVolume, seekerPingPitch);
					if (distance < seekerPingLevel3) ringingSound.play(hider, seekerPingVolume, seekerPingPitch);
					break;
				case 3:
					if (distance < seekerPingLevel1) heartbeatSound.play(hider, seekerPingVolume, seekerPingPitch);
					if (distance < seekerPingLevel3) ringingSound.play(hider, seekerPingVolume, seekerPingPitch);
					break;
				case 6:
					if (distance < seekerPingLevel3) ringingSound.play(hider, seekerPingVolume, seekerPingPitch);
					break;
				case 9:
					if (distance < seekerPingLevel2) ringingSound.play(hider, seekerPingVolume, seekerPingPitch);
					break;
			}
		}
		if (gameTick %20 == 0) {
			if (gameLength > 0) {
				board.reloadGameBoards();
				gameTimer--;
			}
			if (currentMap.isWorldBorderEnabled()) currentMap.getWorldBorder().update();
			if (tauntEnabled) taunt.update();
			if (glowEnabled || alwaysGlow) glow.update();
		}
		board.getSpectators().forEach(spectator -> spectator.setFlying(spectator.getAllowFlight()));
		checkWinConditions();
	}

	public void broadcastMessage(String message) {
		for(Player player : board.getPlayers()) {
			player.sendMessage(message);
		}
	}

    public void broadcastTitle(String title, String subtitle) {
        for (Player player : board.getPlayers()) {
            Titles.sendTitle(player, 10, 70, 20, title, subtitle); 
        }
    }

	public boolean isCurrentMapValid() {
		return currentMap != null && !currentMap.isNotSetup();
	}

	public boolean checkCurrentMap() {
		if(currentMap != null && !currentMap.isNotSetup()) return false;
		this.currentMap = Maps.getRandomMap();
		return this.currentMap == null;
	}

	public void setCurrentMap(Map map) {
		this.currentMap = map;
	}

	public Map getCurrentMap() {
		return currentMap;
	}

	private void checkWinConditions() {
        int hiderCount = board.sizeHider();
		if (hiderCount < 1 || (!waitTillNoneLeft && hiderCount < 2)) {
			if (hiderLeft && dontRewardQuit) {
				if (announceMessagesToNonPlayers) Bukkit.broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_HIDERS_QUIT"));
				else broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_HIDERS_QUIT"));
                if (gameOverTitle) broadcastTitle(message("GAME_TITLE_NO_WIN").toString(), message("GAME_GAMEOVER_HIDERS_QUIT").toString());
				stop(WinType.NONE);
			} else {
                if (hiderCount < 1 || waitTillNoneLeft) {
				    if (announceMessagesToNonPlayers) Bukkit.broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_HIDERS_FOUND"));
				    else broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_HIDERS_FOUND"));
                if (gameOverTitle) broadcastTitle(message("GAME_TITLE_SEEKERS_WIN").toString(), message("GAME_GAMEOVER_HIDERS_FOUND").toString());
                } else {
                    Player hider = board.getHiders().get(0);
				    if (announceMessagesToNonPlayers) Bukkit.broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_LAST_HIDER").addPlayer(hider));
				    else broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_LAST_HIDER").addPlayer(hider));
                    if (gameOverTitle) broadcastTitle(message("GAME_TITLE_SINGLE_HIDER_WIN").addPlayer(hider).toString(), message("GAME_SUBTITLE_SINGLE_HIDER_WIN").addPlayer(hider).toString());
                }
				stop(WinType.SEEKER_WIN);
			}
		} else if (board.sizeSeeker() < 1) {
			if (announceMessagesToNonPlayers) Bukkit.broadcastMessage(abortPrefix + message("GAME_GAMEOVER_SEEKERS_QUIT"));
			else broadcastMessage(abortPrefix + message("GAME_GAMEOVER_SEEKERS_QUIT"));
            if (gameOverTitle) broadcastTitle(message("GAME_TITLE_NO_WIN").toString(), message("GAME_GAMEOVER_SEEKERS_QUIT").toString());
			stop(dontRewardQuit ? WinType.NONE : WinType.HIDER_WIN);
		} else if (gameTimer < 1) {
			if (announceMessagesToNonPlayers) Bukkit.broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_TIME"));
			else broadcastMessage(gameOverPrefix + message("GAME_GAMEOVER_TIME"));
            if (gameOverTitle) broadcastTitle(message("GAME_TITLE_HIDERS_WIN").toString(), message("GAME_GAMEOVER_TIME").toString());
			stop(WinType.HIDER_WIN);
		}
		hiderLeft = false;
	}

}
