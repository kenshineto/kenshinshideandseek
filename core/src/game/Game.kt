package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.config.ConfigCountdownDisplay
import cat.freya.khs.config.ConfigLeaveType
import cat.freya.khs.config.ConfigScoringMode
import cat.freya.khs.inv.createBlockHuntPicker
import cat.freya.khs.player.Player
import cat.freya.khs.world.Item
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.random.Random
import kotlin.synchronized
import kotlin.toUInt

class Game(val plugin: Khs) {
    /// represents what state the game is in
    enum class Status {
        LOBBY,
        HIDING,
        SEEKING,
        FINISHED;

        fun inProgress(): Boolean =
            when (this) {
                LOBBY -> false
                HIDING -> true
                SEEKING -> true
                FINISHED -> false
            }
    }

    /// what team a player is on
    enum class Team {
        HIDER,
        SEEKER,
        SPECTATOR,
    }

    /// why was the game stopped?
    enum class WinType {
        NONE,
        SEEKER_WIN,
        HIDER_WIN,
    }

    @Volatile
    /// the state the game is in
    var status: Status = Status.LOBBY
        private set

    @Volatile
    /// timer for current game status (lobby, hiding, seeking, finished)
    var timer: ULong? = null
        private set

    @Volatile
    /// keep track till next second
    private var gameTick: UInt = 0u
    private val isSecond: Boolean
        get() = gameTick % 20u == 0u

    @Volatile
    /// if the last event was a hider leaving the game
    private var hiderLeft: Boolean = false

    @Volatile
    /// the current game round
    private var round: UInt = 0u

    val glow: Glow = Glow(this)
    val taunt: Taunt = Taunt(this)
    val border: Border = Border(this)

    @Volatile
    var map: KhsMap? = null
        private set

    private val mappings: MutableMap<UUID, Team> = ConcurrentHashMap<UUID, Team>()

    val players: List<Player>
        get() = mappings.keys.mapNotNull { plugin.shim.getPlayer(it) }

    val UUIDs: Set<UUID>
        get() = mappings.keys.toSet()

    val hiderUUIDs: Set<UUID>
        get() = mappings.filter { it.value == Team.HIDER }.keys

    val hiderPlayers: List<Player>
        get() = hiderUUIDs.mapNotNull { plugin.shim.getPlayer(it) }

    val seekerUUIDs: Set<UUID>
        get() = mappings.filter { it.value == Team.SEEKER }.keys

    val seekerPlayers: List<Player>
        get() = seekerUUIDs.mapNotNull { plugin.shim.getPlayer(it) }

    val spectatorUUIDs: Set<UUID>
        get() = mappings.filter { it.value == Team.SPECTATOR }.keys

    val spectatorPlayers: List<Player>
        get() = spectatorUUIDs.mapNotNull { plugin.shim.getPlayer(it) }

    val size: UInt
        get() = mappings.size.toUInt()

    val hiderSize: UInt
        get() = hiderUUIDs.size.toUInt()

    val seekerSize: UInt
        get() = seekerUUIDs.size.toUInt()

    val spectatorsSize: UInt
        get() = spectatorUUIDs.size.toUInt()

    fun hasPlayer(uuid: UUID): Boolean = mappings.containsKey(uuid)

    fun hasPlayer(player: Player): Boolean = hasPlayer(player.uuid)

    fun isHider(uuid: UUID): Boolean = mappings[uuid] == Team.HIDER

    fun isHider(player: Player): Boolean = isHider(player.uuid)

    fun isSeeker(uuid: UUID): Boolean = mappings[uuid] == Team.SEEKER

    fun isSeeker(player: Player): Boolean = isSeeker(player.uuid)

    fun isSpectator(uuid: UUID): Boolean = mappings[uuid] == Team.SPECTATOR

    fun isSpectator(player: Player): Boolean = isSpectator(player.uuid)

    fun getTeam(uuid: UUID): Team? = mappings[uuid]

    fun setTeam(uuid: UUID, team: Team) {
        mappings[uuid] = team
    }

    fun sameTeam(a: UUID, b: UUID): Boolean = mappings[a] == mappings[b]

    // what round was the uuid last picked to be seeker
    private val lastPicked: MutableMap<UUID, UInt> = ConcurrentHashMap<UUID, UInt>()

    @Volatile
    // teams at the start of the game
    private var initialTeams: Map<UUID, Team> = emptyMap()

    @Volatile
    // stores saved inventories
    private var savedInventories: MutableMap<UUID, List<Item?>> =
        ConcurrentHashMap<UUID, List<Item?>>()

    // status for this round
    private var hiderKills: MutableMap<UUID, UInt> = ConcurrentHashMap<UUID, UInt>()
    private var seekerKills: MutableMap<UUID, UInt> = ConcurrentHashMap<UUID, UInt>()
    private var hiderDeaths: MutableMap<UUID, UInt> = ConcurrentHashMap<UUID, UInt>()
    private var seekerDeaths: MutableMap<UUID, UInt> = ConcurrentHashMap<UUID, UInt>()

    fun doTick() {
        if (map?.setup != true) return

        when (status) {
            Status.LOBBY -> whileWaiting()
            Status.HIDING -> whileHiding()
            Status.SEEKING -> whileSeeking()
            Status.FINISHED -> whileFinished()
        }

        gameTick++
    }

    fun selectMap(): KhsMap? {
        map = map ?: plugin.maps.values.filter { it.setup }.randomOrNull()
        return map
    }

    fun setMap(map: KhsMap?) {
        if (status != Status.LOBBY) return

        if (map == null && size > 0u) return

        this.map = map
        players.forEach { player -> joinPlayer(player) }
    }

    fun getSeekerWeight(uuid: UUID): Double {
        val maxWeight = 4u
        val lastRoundSeeker = lastPicked[uuid]?.let { minOf(it, round) }
        val roundsSinceSeeker = lastRoundSeeker?.let { round - lastRoundSeeker }
        val weight = minOf(roundsSinceSeeker ?: maxWeight, maxWeight)
        return weight.toDouble()
    }

    fun getSeekerChance(uuid: UUID): Double {
        val weights = mappings.keys.map { getSeekerWeight(it) }
        val totalWeight = weights.sum()
        val weight = getSeekerWeight(uuid)
        if (totalWeight == 0.0) return 0.0
        val percent = weight / totalWeight

        // calculate probabal team sizes
        val wantedSeekerCount = maxOf(plugin.config.startingSeekerCount, 1u)
        val numPlayers = maxOf(size, 1u)
        val numSeekers = minOf(wantedSeekerCount, numPlayers - 1u)

        // return percent * num seekers
        return percent * numSeekers.toDouble()
    }

    private fun randomSeeker(pool: Set<UUID>): UUID {
        val weights = pool.map { uuid -> uuid to getSeekerWeight(uuid) }

        val totalWeight = weights.sumOf { it.second }
        var r = Random.nextDouble() * totalWeight

        for ((uuid, weight) in weights) {
            r -= weight
            if (r <= 0) {
                lastPicked[uuid] = round
                return uuid
            }
        }

        return pool.random()
    }

    fun start() {
        start(emptySet())
    }

    fun start(requestedPool: Collection<UUID>) {
        val seekers = mutableSetOf<UUID>()
        val pool =
            if (requestedPool.isEmpty()) mappings.keys.toMutableSet()
            else requestedPool.toMutableSet()

        while (
            pool.isNotEmpty() &&
                seekers.size.toUInt() < plugin.config.startingSeekerCount &&
                seekers.size.toUInt() + 1u < size
        ) {
            val uuid = randomSeeker(pool)
            pool.remove(uuid)
            seekers.add(uuid)
        }

        if (seekers.isEmpty()) // warning here?
         return

        startWithSeekers(seekers)
    }

    private fun startWithSeekers(seekers: Set<UUID>) {
        if (status != Status.LOBBY) return

        if (plugin.config.mapSaveEnabled) map?.loader?.rollback()

        synchronized(this) {
            // set teams
            mappings.forEach { mappings[it.key] = Team.HIDER }
            seekers.forEach { mappings[it] = Team.SEEKER }

            // reset game state
            initialTeams = mappings.toMap()
            hiderKills.clear()
            seekerKills.clear()
            hiderDeaths.clear()
            seekerDeaths.clear()

            // give items
            loadHiders()
            loadSeekers()

            // reload sidebar
            reloadGameBoards()

            glow.reset()
            taunt.reset()
            border.reset()

            status = Status.HIDING
            timer = null
        }
    }

    private fun updatePlayerInfo(uuid: UUID, reason: WinType) {
        val team = initialTeams[uuid] ?: return
        val data = plugin.database?.getPlayer(uuid) ?: return

        when (reason) {
            WinType.SEEKER_WIN -> {
                if (team == Team.SEEKER) data.seekerWins++
                if (team == Team.HIDER) data.hiderLosses++
            }
            WinType.HIDER_WIN -> {
                if (team == Team.SEEKER) data.seekerLosses++
                if (team == Team.HIDER) data.hiderWins++
            }
            WinType.NONE -> {}
        }

        data.seekerKills += seekerKills.getOrDefault(uuid, 0u)
        data.hiderKills += hiderKills.getOrDefault(uuid, 0u)
        data.seekerDeaths += seekerDeaths.getOrDefault(uuid, 0u)
        data.hiderDeaths += hiderDeaths.getOrDefault(uuid, 0u)

        plugin.database?.upsertPlayer(data)
    }

    fun stop(reason: WinType) {
        if (!status.inProgress()) return

        // update database
        mappings.keys.forEach { updatePlayerInfo(it, reason) }

        round++
        status = Status.FINISHED
        timer = null

        if (plugin.config.leaveOnEnd) {
            mappings.keys.forEach { leave(it) }
        }
    }

    fun join(uuid: UUID) {
        val player = plugin.shim.getPlayer(uuid) ?: return

        if (map == null) selectMap()

        if (map == null) {
            player.message(plugin.locale.prefix.error + plugin.locale.map.none)
            return
        }

        if (status != Status.LOBBY) {
            mappings[uuid] = Team.SPECTATOR
            loadSpectator(player)
            reloadGameBoards()
            player.message(plugin.locale.prefix.default + plugin.locale.game.join)
            return
        }

        if (plugin.config.saveInventory) savedInventories[uuid] = player.inventory.contents

        mappings[uuid] = Team.HIDER
        joinPlayer(player)
        reloadLobbyBoards()

        broadcast(plugin.locale.prefix.default + plugin.locale.lobby.join.with(player.name))
    }

    fun leave(uuid: UUID) {
        val player = plugin.shim.getPlayer(uuid) ?: return

        broadcast(plugin.locale.prefix.default + plugin.locale.game.leave.with(player.name))

        mappings.remove(uuid)
        resetPlayer(player)

        if (plugin.config.saveInventory)
            savedInventories[uuid]?.let { player.inventory.contents = it }

        // reload sidebar
        player.hideBoards()
        if (status.inProgress()) {
            reloadGameBoards()
        } else {
            reloadLobbyBoards()
        }

        when (plugin.config.leaveType) {
            ConfigLeaveType.EXIT -> plugin.config.exit?.let { player.teleport(it) }
            ConfigLeaveType.PROXY -> player.sendToServer(plugin.config.leaveServer)
        }
    }

    fun addKill(uuid: UUID) {
        val team = mappings[uuid] ?: return
        when (team) {
            Team.HIDER -> hiderKills[uuid] = hiderKills.getOrDefault(uuid, 0u) + 1u
            Team.SEEKER -> seekerKills[uuid] = seekerKills.getOrDefault(uuid, 0u) + 1u
            else -> {}
        }
    }

    fun addDeath(uuid: UUID) {
        val team = mappings[uuid] ?: return
        when (team) {
            Team.HIDER -> hiderDeaths[uuid] = hiderDeaths.getOrDefault(uuid, 0u) + 1u
            Team.SEEKER -> seekerDeaths[uuid] = seekerDeaths.getOrDefault(uuid, 0u) + 1u
            else -> {}
        }
    }

    private fun reloadLobbyBoards() {
        mappings.keys.forEach { reloadLobbyBoard(plugin, it) }
    }

    private fun reloadGameBoards() {
        mappings.keys.forEach { reloadGameBoard(plugin, it) }
    }

    /// during Status.LOBBY
    private fun whileWaiting() {
        val countdown = plugin.config.lobby.countdown
        val changeCountdown = plugin.config.lobby.changeCountdown

        synchronized(this) {
            // countdown is disabled when set to at 0s
            if (countdown == 0UL || size < plugin.config.lobby.min) {
                timer = null
                return@synchronized
            }

            var time = timer ?: countdown
            if (size >= changeCountdown && changeCountdown != 0u) time = min(time, 10UL)
            if (isSecond && time > 0UL) time--
            timer = time
        }

        if (isSecond) reloadLobbyBoards()

        if (timer == 0UL) start()
    }

    /// during Status.HIDING
    private fun whileHiding() {
        if (!isSecond) return

        if (timer != 0UL) checkWinConditions()

        if (isSecond) reloadGameBoards()

        val time: ULong
        val message: String
        synchronized(this) {
            time = timer ?: plugin.config.hidingLength
            when (time) {
                0UL -> {
                    message = plugin.locale.game.start
                    status = Status.SEEKING
                    timer = null
                    seekerPlayers.forEach {
                        giveSeekerItems(it)
                        map?.gameSpawn?.teleport(it)
                    }
                    hiderPlayers.forEach { giveHiderItems(it) }
                }
                1UL -> message = plugin.locale.game.countdown.last
                else -> message = plugin.locale.game.countdown.notify.with(time)
            }

            if (status == Status.HIDING) timer = if (time > 0UL) (time - 1UL) else time
        }

        if (time % 5UL == 0UL || time <= 5UL) {
            val prefix = plugin.locale.prefix.default
            players.forEach { player ->
                when (plugin.config.countdownDisplay) {
                    ConfigCountdownDisplay.CHAT -> player.message(prefix + message)
                    ConfigCountdownDisplay.ACTIONBAR -> player.actionBar(prefix + message)
                    ConfigCountdownDisplay.TITLE -> {
                        if (time != 30UL) player.title(" ", message)
                    }
                }
            }
        }
    }

    /// @returns distance to the closest seeker to the player
    private fun distanceToSeeker(player: Player): Double {
        return seekerPlayers.minOfOrNull { seeker -> player.location.distance(seeker.location) }
            ?: Double.POSITIVE_INFINITY
    }

    /// plays the seeker ping for a hider
    private fun playSeekerPing(hider: Player) {
        val distance = distanceToSeeker(hider)

        // read config values
        val distances = plugin.config.seekerPing.distances
        val sounds = plugin.config.seekerPing.sounds

        when (gameTick % 10u) {
            0u -> {
                if (distance < distances.level1.toDouble())
                    hider.playSound(sounds.heartbeatNoise, sounds.leadingVolume, sounds.pitch)
                if (distance < distances.level3.toDouble())
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
            }
            3u -> {
                if (distance < distances.level1.toDouble())
                    hider.playSound(sounds.heartbeatNoise, sounds.volume, sounds.pitch)
                if (distance < distances.level3.toDouble())
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
            }
            6u -> {
                if (distance < distances.level3.toDouble())
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
            }
            9u -> {
                if (distance < distances.level2.toDouble())
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
            }
        }
    }

    private fun checkWinConditions() {
        var stopReason: WinType? = null

        val scoreMode = plugin.config.scoringMode
        val notEnoughHiders =
            when (scoreMode) {
                ConfigScoringMode.ALL_HIDERS_FOUND -> hiderSize == 0u
                ConfigScoringMode.LAST_HIDER_WINS -> hiderSize == 1u
            }
        val lastHider = hiderPlayers.firstOrNull()

        val doTitle = plugin.config.gameOverTitle
        val prefix = plugin.locale.prefix

        when {
            // time ran out
            timer == 0UL -> {
                broadcast(prefix.gameOver + plugin.locale.game.gameOver.time)
                if (doTitle)
                    broadcastTitle(
                        plugin.locale.game.title.hidersWin,
                        plugin.locale.game.gameOver.time,
                    )
                stopReason = WinType.HIDER_WIN
            }
            // all seekers quit
            seekerSize < 1u -> {
                broadcast(prefix.abort + plugin.locale.game.gameOver.seekerQuit)
                if (doTitle)
                    broadcastTitle(
                        plugin.locale.game.title.noWin,
                        plugin.locale.game.gameOver.seekerQuit,
                    )
                stopReason = if (plugin.config.dontRewardQuit) WinType.NONE else WinType.HIDER_WIN
            }
            // hiders quit
            notEnoughHiders && hiderLeft -> {
                broadcast(prefix.abort + plugin.locale.game.gameOver.hiderQuit)
                if (doTitle)
                    broadcastTitle(
                        plugin.locale.game.title.noWin,
                        plugin.locale.game.gameOver.hiderQuit,
                    )
                stopReason = if (plugin.config.dontRewardQuit) WinType.NONE else WinType.SEEKER_WIN
            }
            // all hiders found
            notEnoughHiders && lastHider == null -> {
                broadcast(prefix.gameOver + plugin.locale.game.gameOver.hidersFound)
                if (doTitle)
                    broadcastTitle(
                        plugin.locale.game.title.seekersWin,
                        plugin.locale.game.gameOver.hidersFound,
                    )
                stopReason = WinType.SEEKER_WIN
            }
            // last hider wins (depends on scoring more)
            notEnoughHiders && lastHider != null -> {
                val msg = plugin.locale.game.gameOver.lastHider.with(lastHider.name)
                broadcast(prefix.gameOver + msg)
                if (doTitle)
                    broadcastTitle(
                        plugin.locale.game.title.singleHiderWin.with(lastHider.name),
                        msg,
                    )
                stopReason = WinType.HIDER_WIN
            }
        }

        if (stopReason != null) stop(stopReason)

        hiderLeft = false
    }

    /// during Status.SEEKING
    private fun whileSeeking() {
        if (plugin.config.seekerPing.enabled) hiderPlayers.forEach { playSeekerPing(it) }

        synchronized(this) {
            var time = timer
            if (time == null && plugin.config.gameLength != 0UL) time = plugin.config.gameLength

            if (isSecond) {
                if (time != null && time > 0UL) time--

                taunt.update()
                glow.update()
                border.update()
            }

            timer = time
        }

        if (isSecond) reloadGameBoards()

        // update spectator flight
        // (the toggle they have only changed allowed flight)
        spectatorPlayers.forEach { it.flying = it.allowFlight }

        checkWinConditions()
    }

    /// during Status.FINISHED
    private fun whileFinished() {
        synchronized(this) {
            var time = timer ?: plugin.config.endGameDelay
            if (isSecond && time > 0UL) time--

            timer = time

            if (time == 0UL) {
                timer = null
                map = null
                selectMap()

                if (map == null) {
                    broadcast(plugin.locale.prefix.warning + plugin.locale.map.none)
                    return
                }

                status = Status.LOBBY

                players.forEach { joinPlayer(it) }
            }
        }
    }

    fun broadcast(message: String) {
        players.forEach { it.message(message) }
    }

    fun broadcastTitle(title: String, subTitle: String) {
        players.forEach { it.title(title, subTitle) }
    }

    private fun loadHiders() = hiderPlayers.forEach { loadHider(it) }

    private fun loadSeekers() = seekerPlayers.forEach { loadSeeker(it) }

    private fun hidePlayer(player: Player, hidden: Boolean) {
        players.forEach { other -> if (other.uuid != player.uuid) other.setHidden(player, hidden) }
    }

    fun resetPlayer(player: Player) {
        player.flying = false
        player.allowFlight = false
        player.gameMode = Player.GameMode.ADVENTURE
        player.inventory.clear()
        player.clearEffects()
        player.hunger = 20u
        player.health = 20.0
        player.heal()
        plugin.disguiser.reveal(player.uuid)
        hidePlayer(player, false)
    }

    fun loadHider(hider: Player) {
        map?.gameSpawn?.teleport(hider)
        resetPlayer(hider)
        hider.setSpeed(5u)
        hider.title(plugin.locale.game.team.hider, plugin.locale.game.team.hiderSubtitle)

        // open block hunt picker
        if (map?.config?.blockHunt?.enabled == true) {
            val map = map ?: return
            val inv = createBlockHuntPicker(plugin, map) ?: return
            hider.showInventory(inv)
        }
    }

    fun giveHiderItems(hider: Player) {
        val items = plugin.itemsConfig.hiderItems.mapNotNull { plugin.shim.parseItem(it) }
        val effects = plugin.itemsConfig.hiderEffects.mapNotNull { plugin.shim.parseEffect(it) }

        hider.inventory.clear()
        for ((i, item) in items.withIndex()) hider.inventory.set(i.toUInt(), item)

        // glow power-up
        if (!plugin.config.alwaysGlow && plugin.config.glow.enabled) {
            val item = plugin.shim.parseItem(plugin.config.glow.item)
            item?.let { hider.inventory.set(items.size.toUInt(), it) }
        }

        plugin.itemsConfig.hiderHelmet
            ?.let { plugin.shim.parseItem(it) }
            ?.let { hider.inventory.helmet = it }
        plugin.itemsConfig.hiderChestplate
            ?.let { plugin.shim.parseItem(it) }
            ?.let { hider.inventory.chestplate = it }
        plugin.itemsConfig.hiderLeggings
            ?.let { plugin.shim.parseItem(it) }
            ?.let { hider.inventory.leggings = it }
        plugin.itemsConfig.hiderBoots
            ?.let { plugin.shim.parseItem(it) }
            ?.let { hider.inventory.boots = it }

        hider.clearEffects()
        for (effect in effects) hider.giveEffect(effect)
    }

    fun loadSeeker(seeker: Player) {
        map?.seekerLobbySpawn?.teleport(seeker)
        resetPlayer(seeker)
        seeker.title(plugin.locale.game.team.seeker, plugin.locale.game.team.seekerSubtitle)
    }

    fun giveSeekerItems(seeker: Player) {
        val items = plugin.itemsConfig.seekerItems.mapNotNull { plugin.shim.parseItem(it) }
        val effects = plugin.itemsConfig.seekerEffects.mapNotNull { plugin.shim.parseEffect(it) }

        seeker.inventory.clear()
        for ((i, item) in items.withIndex()) seeker.inventory.set(i.toUInt(), item)

        plugin.itemsConfig.seekerHelmet
            ?.let { plugin.shim.parseItem(it) }
            ?.let { seeker.inventory.helmet = it }
        plugin.itemsConfig.seekerChestplate
            ?.let { plugin.shim.parseItem(it) }
            ?.let { seeker.inventory.chestplate = it }
        plugin.itemsConfig.seekerLeggings
            ?.let { plugin.shim.parseItem(it) }
            ?.let { seeker.inventory.leggings = it }
        plugin.itemsConfig.seekerBoots
            ?.let { plugin.shim.parseItem(it) }
            ?.let { seeker.inventory.boots = it }

        seeker.clearEffects()
        for (effect in effects) seeker.giveEffect(effect)
    }

    fun loadSpectator(spectator: Player) {
        map?.gameSpawn?.teleport(spectator)
        resetPlayer(spectator)
        spectator.allowFlight = true
        spectator.flying = true

        plugin.config.spectatorItems.teleport
            .let { plugin.shim.parseItem(it) }
            ?.let { spectator.inventory.set(3u, it) }

        plugin.config.spectatorItems.flight
            .let { plugin.shim.parseItem(it) }
            ?.let { spectator.inventory.set(6u, it) }

        hidePlayer(spectator, true)
    }

    private fun joinPlayer(player: Player) {
        map?.lobbySpawn?.teleport(player)
        resetPlayer(player)

        plugin.config.lobby.leaveItem
            .let { plugin.shim.parseItem(it) }
            ?.let { player.inventory.set(0u, it) }

        if (player.hasPermission("hs.start")) {
            plugin.config.lobby.startItem
                .let { plugin.shim.parseItem(it) }
                ?.let { player.inventory.set(8u, it) }
        }

        if (getTeam(player.uuid) != Team.SPECTATOR)
            spectatorPlayers.forEach { player.setHidden(it, true) }
    }
}
