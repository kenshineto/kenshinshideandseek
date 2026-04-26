package cat.freya.khs.game

import cat.freya.khs.game.Game.Team
import cat.freya.khs.world.Player
import java.util.UUID

class Teams {
    // mapping of player to their team
    private val mappings: MutableMap<UUID, Team> = mutableMapOf()

    // cache of what teams have what uuids
    private val hiders: MutableSet<UUID> = mutableSetOf()
    private val seekers: MutableSet<UUID> = mutableSetOf()
    private val spectators: MutableSet<UUID> = mutableSetOf()

    // cache of online players
    private val playerCache: MutableMap<UUID, Player> = mutableMapOf()

    private val lock = Any()

    fun put(uuid: UUID, team: Team) {
        synchronized(lock) {
            remove(uuid)
            mappings[uuid] = team
            when (team) {
                Team.HIDER -> hiders.add(uuid)
                Team.SEEKER -> seekers.add(uuid)
                Team.SPECTATOR -> spectators.add(uuid)
            }
        }
    }

    fun remove(uuid: UUID) {
        synchronized(lock) {
            mappings.remove(uuid)
            hiders.remove(uuid)
            seekers.remove(uuid)
            spectators.remove(uuid)
        }
    }

    fun get(uuid: UUID): Team? {
        synchronized(lock) {
            return mappings.get(uuid)
        }
    }

    fun contains(uuid: UUID): Boolean {
        synchronized(lock) {
            return mappings.contains(uuid)
        }
    }

    fun reset() {
        synchronized(lock) {
            val uuids = mappings.keys.toSet()
            for (uuid in uuids) {
                remove(uuid)
                mappings[uuid] = Team.HIDER
                hiders.add(uuid)
            }
        }
    }

    fun clear(): Set<UUID> {
        synchronized(lock) {
            val uuids = mappings.keys.toSet()
            mappings.clear()
            hiders.clear()
            seekers.clear()
            spectators.clear()
            return uuids
        }
    }

    fun getHiders(): Set<UUID> {
        synchronized(lock) {
            return hiders.toSet()
        }
    }

    fun getSeekers(): Set<UUID> {
        synchronized(lock) {
            return seekers.toSet()
        }
    }

    fun getUUIDs(): Set<UUID> {
        synchronized(lock) {
            return mappings.keys.toSet()
        }
    }

    fun getMappings(): Map<UUID, Team> {
        synchronized(lock) {
            return mappings.toMap()
        }
    }

    fun getSpectators(): Set<UUID> {
        synchronized(lock) {
            return spectators.toSet()
        }
    }

    fun getHiderPlayers(): List<Player> {
        synchronized(lock) {
            return hiders.mapNotNull { playerCache[it] }
        }
    }

    fun getSeekerPlayers(): List<Player> {
        synchronized(lock) {
            return seekers.mapNotNull { playerCache[it] }
        }
    }

    fun getSpectatorPlayers(): List<Player> {
        synchronized(lock) {
            return spectators.mapNotNull { playerCache[it] }
        }
    }

    fun getPlayers(): List<Player> {
        synchronized(lock) {
            return mappings.keys.mapNotNull { playerCache[it] }
        }
    }

    fun isHider(uuid: UUID): Boolean {
        synchronized(lock) {
            return hiders.contains(uuid)
        }
    }

    fun isSeeker(uuid: UUID): Boolean {
        synchronized(lock) {
            return seekers.contains(uuid)
        }
    }

    fun isSpectator(uuid: UUID): Boolean {
        synchronized(lock) {
            return spectators.contains(uuid)
        }
    }

    fun size(): UInt {
        synchronized(lock) {
            return mappings.size.toUInt()
        }
    }

    fun hiderCount(): UInt {
        synchronized(lock) {
            return hiders.size.toUInt()
        }
    }

    fun seekerCount(): UInt {
        synchronized(lock) {
            return seekers.size.toUInt()
        }
    }

    fun spectatorCount(): UInt {
        synchronized(lock) {
            return spectators.size.toUInt()
        }
    }

    fun cachePut(player: Player) {
        synchronized(lock) {
            playerCache[player.uuid] = player
        }
    }

    fun cacheRemove(uuid: UUID) {
        synchronized(lock) {
            playerCache.remove(uuid)
        }
    }
}
