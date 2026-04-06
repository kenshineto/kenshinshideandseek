package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.config.ConfigCountdownDisplay
import cat.freya.khs.config.ConfigLeaveType
import cat.freya.khs.config.ConfigScoringMode
import cat.freya.khs.menu.BlockHuntMenu
import cat.freya.khs.world.Item
import cat.freya.khs.world.Player
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

        fun inProgress(): Boolean {
            return when (this) {
                LOBBY -> false
                HIDING -> true
                SEEKING -> true
                FINISHED -> false
            }
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
        get() = playerUUIDs.mapNotNull { plugin.shim.getPlayer(it) }

    val playerUUIDs: Set<UUID>
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

    val spectatorSize: UInt
        get() = spectatorUUIDs.size.toUInt()

    fun hasPlayer(uuid: UUID): Boolean {
        return mappings.containsKey(uuid)
    }

    fun hasPlayer(player: Player): Boolean {
        return hasPlayer(player.uuid)
    }

    fun isHider(uuid: UUID): Boolean {
        return getTeam(uuid) == Team.HIDER
    }

    fun isHider(player: Player): Boolean {
        return isHider(player.uuid)
    }

    fun isSeeker(uuid: UUID): Boolean {
        return getTeam(uuid) == Team.SEEKER
    }

    fun isSeeker(player: Player): Boolean {
        return isSeeker(player.uuid)
    }

    fun isSpectator(uuid: UUID): Boolean {
        return getTeam(uuid) == Team.SPECTATOR
    }

    fun isSpectator(player: Player): Boolean {
        return isSpectator(player.uuid)
    }

    fun getTeam(uuid: UUID): Team? {
        return mappings[uuid]
    }

    fun setTeam(uuid: UUID, team: Team) {
        mappings[uuid] = team
    }

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
        if (map?.isSetup() != true) return

        when (status) {
            Status.LOBBY -> whileWaiting()
            Status.HIDING -> whileHiding()
            Status.SEEKING -> whileSeeking()
            Status.FINISHED -> whileFinished()
        }

        gameTick++
    }

    /** If a map is not set, select a new map */
    fun selectMap(): KhsMap? {
        map = map ?: plugin.maps.values.filter { it.isSetup() }.randomOrNull()
        return map
    }

    fun setMap(map: KhsMap?) {
        if (status != Status.LOBBY) return

        if (map == null && size > 0u) return

        this.map = map
        players.forEach { player -> loadPlayerIntoLobby(player) }
    }

    fun getSeekerWeight(uuid: UUID): Double {
        val maxWeight = 4u
        val lastRoundSeeker = lastPicked[uuid]?.let { minOf(it, round) }
        val roundsSinceSeeker = lastRoundSeeker?.let { round - lastRoundSeeker }
        val weight = minOf(roundsSinceSeeker ?: maxWeight, maxWeight)
        return weight.toDouble()
    }

    fun getSeekerChance(uuid: UUID): Double {
        val weights = playerUUIDs.map { getSeekerWeight(it) }
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
            if (requestedPool.isEmpty()) playerUUIDs.toMutableSet()
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

        if (plugin.config.mapSaveEnabled) {
            // roll back the mapsave
            map?.getGameWorld()?.loader?.rollback()
        }

        synchronized(this) {
            // set teams
            playerUUIDs.forEach { setTeam(it, Team.HIDER) }
            seekers.forEach { setTeam(it, Team.SEEKER) }

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
        playerUUIDs.forEach { updatePlayerInfo(it, reason) }

        round++
        status = Status.FINISHED
        timer = null

        if (plugin.config.leaveOnEnd) {
            playerUUIDs.forEach { leave(it) }
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
            setTeam(uuid, Team.SPECTATOR)
            loadSpectator(player)
            reloadGameBoards()
            player.message(plugin.locale.prefix.default + plugin.locale.game.join)
            return
        }

        if (plugin.config.saveInventory) {
            savedInventories[uuid] = player.getInventory().getContents()
        }

        setTeam(uuid, Team.HIDER)
        loadPlayerIntoLobby(player)
        reloadLobbyBoards()

        broadcast(plugin.locale.prefix.default + plugin.locale.lobby.join.with(player.name))
    }

    fun leave(uuid: UUID) {
        val player = plugin.shim.getPlayer(uuid) ?: return

        broadcast(plugin.locale.prefix.default + plugin.locale.game.leave.with(player.name))

        mappings.remove(uuid)
        resetPlayer(player)

        if (plugin.config.saveInventory) {
            savedInventories[uuid]?.let { player.getInventory().setContents(it) }
        }

        // reload sidebar
        player.hideScoreBoard()
        if (status.inProgress()) {
            reloadGameBoards()
        } else {
            reloadLobbyBoards()
        }

        if (plugin.config.leaveType == ConfigLeaveType.PROXY) {
            val server = plugin.config.leaveServer
            val successfull = plugin.shim.sendPlayerToServer(uuid, server)
            if (!successfull) {
                player.message(
                    plugin.locale.prefix.error +
                        plugin.locale.command.sendToServerFailed.with(server)
                )
                player.teleport(plugin.config.exit)
            }
        } else {
            plugin.config.exit?.let { player.teleport(it) }
        }
    }

    fun addKill(uuid: UUID) {
        val team = getTeam(uuid) ?: return
        when (team) {
            Team.HIDER -> hiderKills[uuid] = hiderKills.getOrDefault(uuid, 0u) + 1u
            Team.SEEKER -> seekerKills[uuid] = seekerKills.getOrDefault(uuid, 0u) + 1u
            else -> {}
        }
    }

    fun addDeath(uuid: UUID) {
        val team = getTeam(uuid) ?: return
        when (team) {
            Team.HIDER -> hiderDeaths[uuid] = hiderDeaths.getOrDefault(uuid, 0u) + 1u
            Team.SEEKER -> seekerDeaths[uuid] = seekerDeaths.getOrDefault(uuid, 0u) + 1u
            else -> {}
        }
    }

    private fun reloadLobbyBoards() {
        playerUUIDs.forEach { reloadLobbyBoard(plugin, it) }
    }

    private fun reloadGameBoards() {
        playerUUIDs.forEach { reloadGameBoard(plugin, it) }
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
                        it.teleport(map?.gameSpawn)
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
        val distances =
            seekerPlayers.mapNotNull { seeker ->
                player.getLocation().distance(seeker.getLocation())
            }
        return distances.minOrNull() ?: Double.POSITIVE_INFINITY
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
        spectatorPlayers.forEach { it.setFlying(it.getAllowedFlight()) }

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

                players.forEach { loadPlayerIntoLobby(it) }
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

    private fun setPlayerHidden(player: Player, hidden: Boolean) {
        players.forEach { observer ->
            // cannot hide oneself
            if (observer.uuid == player.uuid) {
                return@forEach
            }

            if (hidden) {
                plugin.entityHider.hideEntity(observer, player)
            } else {
                plugin.entityHider.showEntity(observer, player)
            }
        }
    }

    fun resetPlayer(player: Player) {
        player.setFlying(false)
        player.setAllowedFlight(false)
        player.setGameMode(Player.GameMode.ADVENTURE)
        player.getInventory().clearAll()
        player.clearEffects()
        player.satiate()
        player.heal()
        plugin.disguiser.reveal(player.uuid)
        setPlayerHidden(player, false)
    }

    fun loadHider(hider: Player) {
        hider.teleport(map?.gameSpawn)
        resetPlayer(hider)
        hider.setSpeed(5u)
        hider.title(plugin.locale.game.team.hider, plugin.locale.game.team.hiderSubtitle)

        // open block hunt picker
        if (map?.config?.blockHunt?.enabled == true) {
            val map = map ?: return
            val inv = BlockHuntMenu.create(plugin, map) ?: return
            hider.showInventory(inv)
        }
    }

    fun giveHiderItems(hider: Player) {
        val inventory = hider.getInventory()
        val items = plugin.itemsConfig.hiderItems.mapNotNull { plugin.shim.parseItem(it) }
        val effects = plugin.itemsConfig.hiderEffects.mapNotNull { plugin.shim.parseEffect(it) }

        inventory.clearAll()
        items.withIndex().forEach { (i, item) -> inventory.set(i.toUInt(), item) }

        // glow power-up
        if (!plugin.config.alwaysGlow && plugin.config.glow.enabled) {
            val item = plugin.shim.parseItem(plugin.config.glow.item)
            item?.let { hider.getInventory().set(items.size.toUInt(), it) }
        }

        val helmet = plugin.shim.parseItem(plugin.itemsConfig.hiderHelmet)
        val chestplate = plugin.shim.parseItem(plugin.itemsConfig.hiderChestplate)
        val leggings = plugin.shim.parseItem(plugin.itemsConfig.hiderLeggings)
        val boots = plugin.shim.parseItem(plugin.itemsConfig.hiderBoots)

        inventory.setHelmet(helmet)
        inventory.setChestplate(chestplate)
        inventory.setLeggings(leggings)
        inventory.setBoots(boots)

        hider.clearEffects()
        for (effect in effects) hider.giveEffect(effect)
    }

    fun loadSeeker(seeker: Player) {
        seeker.teleport(map?.seekerLobbySpawn)
        resetPlayer(seeker)
        seeker.title(plugin.locale.game.team.seeker, plugin.locale.game.team.seekerSubtitle)
    }

    fun giveSeekerItems(seeker: Player) {
        val inventory = seeker.getInventory()
        val items = plugin.itemsConfig.seekerItems.mapNotNull { plugin.shim.parseItem(it) }
        val effects = plugin.itemsConfig.seekerEffects.mapNotNull { plugin.shim.parseEffect(it) }

        inventory.clearAll()
        items.withIndex().forEach { (i, item) -> inventory.set(i.toUInt(), item) }

        val helmet = plugin.shim.parseItem(plugin.itemsConfig.seekerHelmet)
        val chestplate = plugin.shim.parseItem(plugin.itemsConfig.seekerChestplate)
        val leggings = plugin.shim.parseItem(plugin.itemsConfig.seekerLeggings)
        val boots = plugin.shim.parseItem(plugin.itemsConfig.seekerBoots)

        inventory.setHelmet(helmet)
        inventory.setChestplate(chestplate)
        inventory.setLeggings(leggings)
        inventory.setBoots(boots)

        seeker.clearEffects()
        for (effect in effects) seeker.giveEffect(effect)
    }

    fun loadSpectator(spectator: Player) {
        spectator.teleport(map?.gameSpawn)
        resetPlayer(spectator)
        spectator.setAllowedFlight(true)
        spectator.setFlying(true)

        val inventory = spectator.getInventory()
        val teleportItem = plugin.shim.parseItem(plugin.config.spectatorItems.teleport)
        val flightItem = plugin.shim.parseItem(plugin.config.spectatorItems.flight)

        inventory.set(3u, teleportItem)
        inventory.set(6u, flightItem)

        setPlayerHidden(spectator, true)
    }

    private fun loadPlayerIntoLobby(player: Player) {
        player.teleport(map?.lobbySpawn)
        resetPlayer(player)

        val inventory = player.getInventory()
        val leaveItem = plugin.shim.parseItem(plugin.config.lobby.leaveItem)
        val startItem = plugin.shim.parseItem(plugin.config.lobby.startItem)

        inventory.set(0u, leaveItem)
        if (player.hasPermission("hs.start")) {
            inventory.set(8u, startItem)
        }
    }
}
