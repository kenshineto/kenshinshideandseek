package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.config.ConfigCountdownDisplay
import cat.freya.khs.config.ConfigGameMode
import cat.freya.khs.config.ConfigLeaveType
import cat.freya.khs.config.ItemConfig
import cat.freya.khs.game.gamemode.GameMode
import cat.freya.khs.game.gamemode.HideAndSeek
import cat.freya.khs.game.gamemode.Tag
import cat.freya.khs.menu.BlockHuntMenu
import cat.freya.khs.type.Item
import cat.freya.khs.world.Player
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.min
import kotlin.random.Random
import kotlin.synchronized
import kotlin.toUInt

class Game(val plugin: Khs) {
    /** represents what state the game is in */
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

    /** what team a player is on */
    enum class Team {
        HIDER,
        SEEKER,
        SPECTATOR,
        UNASSIGNED,
    }

    /** why was the game stopped? */
    enum class WinType {
        STOPPED,
        PLAYERS_LEFT,
        SEEKERS_WIN,
        HIDERS_WIN,
        LAST_HIDER_WIN,
    }

    /** the state the game is in */
    @Volatile
    var status: Status = Status.LOBBY
        private set

    /** timer for current game status (lobby, hiding, seeking, finished) */
    @Volatile
    var timer: ULong? = null
        private set

    /** the active gamemode */
    var gameMode: GameMode = HideAndSeek(this)
        private set

    /** keep track till next second */
    private var gameTick: UByte = 0u
    private var isSecond: Boolean = false

    /** if the last event was a hider leaving the game */
    private var playerLeft: Boolean = false

    /** the current game round */
    private var round: UInt = 0u

    /** what round was the uuid last picked to be seeker */
    private val lastPicked: MutableMap<UUID, UInt> = ConcurrentHashMap()

    /** what uuid's won last game */
    private val lastWinners: MutableSet<UUID> = ConcurrentHashMap.newKeySet()

    /** teams at the start of the game */
    private var initialTeams: Map<UUID, Team> = emptyMap()

    /** stores saved inventories */
    private var savedInventories: MutableMap<UUID, List<Item?>> = ConcurrentHashMap()

    /** stores saved scoreboards */
    private var savedScoreBoards: MutableMap<UUID, Board> = ConcurrentHashMap()

    // status for this round
    private var hiderKills: MutableMap<UUID, UInt> = ConcurrentHashMap()
    private var seekerKills: MutableMap<UUID, UInt> = ConcurrentHashMap()
    private var hiderDeaths: MutableMap<UUID, UInt> = ConcurrentHashMap()
    private var seekerDeaths: MutableMap<UUID, UInt> = ConcurrentHashMap()

    private var lock = Any()

    /* what players are in the game and what teams
     * are they on */
    val teams: Teams = Teams(plugin.shim)

    // events and powerups
    val glow: Glow = Glow(this)
    val taunt: Taunt = Taunt(this)
    val border: Border = Border(this)

    @Volatile
    var map: KhsMap? = null
        private set

    fun doTick() {
        synchronized(lock) {
            if (map?.isSetup() != true) return

            isSecond = gameTick == 0u.toUByte()

            when (status) {
                Status.LOBBY -> whileWaiting()
                Status.HIDING -> whileHiding()
                Status.SEEKING -> whileSeeking()
                Status.FINISHED -> whileFinished()
            }

            gameTick++
            gameTick = (gameTick % 20u).toUByte()
        }
    }

    /** If a map is not set, select a new map */
    fun selectMap(): KhsMap? {
        synchronized(lock) {
            map = map ?: plugin.maps.values.filter { it.isSetup() }.randomOrNull()
            return map
        }
    }

    fun setMap(map: KhsMap?) {
        synchronized(lock) {
            if (status != Status.LOBBY) return

            if (map == null && teams.size() > 0u) return

            this.map = map
            teams.getPlayers().forEach { player -> loadPlayerIntoLobby(player) }
        }
    }

    fun reset() {
        val uuids: Set<UUID>
        synchronized(lock) {
            map = null
            round = 0u
            gameTick = 0u
            status = Status.LOBBY
            uuids = teams.clear()
            lastPicked.clear()
            gameMode =
                when (plugin.config.gameMode) {
                    ConfigGameMode.HIDE_AND_SEEK -> HideAndSeek(this)
                    ConfigGameMode.TAG -> Tag(this)
                }
        }

        uuids.forEach { leave(it) }

        savedInventories.clear()
        savedScoreBoards.clear()
    }

    private fun getSeekerWeight(uuid: UUID): Double {
        val maxWeight = 4u
        val lastRoundSeeker = lastPicked[uuid]?.let { minOf(it, round) }
        val roundsSinceSeeker = lastRoundSeeker?.let { round - lastRoundSeeker }
        val weight = minOf(roundsSinceSeeker ?: maxWeight, maxWeight)
        return weight.toDouble()
    }

    fun getSeekerChance(uuid: UUID): Double {
        val weights = teams.getUUIDs().map { getSeekerWeight(it) }
        val totalWeight = weights.sum()
        val weight = getSeekerWeight(uuid)
        if (totalWeight == 0.0) return 0.0
        val percent = weight / totalWeight

        // calculate probable team sizes
        val wantedSeekerCount = maxOf(plugin.config.startingSeekerCount, 1u)
        val numPlayers = maxOf(teams.size(), 1u)
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
            if (requestedPool.isEmpty()) {
                teams.getUUIDs().toMutableSet()
            } else {
                requestedPool.toMutableSet()
            }

        if (teams.size() < gameMode.getMinPlayers()) {
            return
        }

        while (
            pool.isNotEmpty() &&
                seekers.size.toUInt() < plugin.config.startingSeekerCount &&
                seekers.size.toUInt() + 1u < teams.size()
        ) {
            val uuid = randomSeeker(pool)
            pool.remove(uuid)
            seekers.add(uuid)
        }

        if (seekers.isEmpty()) {
            // warning here?
            return
        }

        if (status != Status.LOBBY) return

        if (plugin.config.mapSaveEnabled) {
            // roll back the mapsave
            map?.getGameWorld()?.loader?.rollback()
            plugin.shim.scheduleEvent(1UL) {
                // this need to be a 1 tick delay
                // to stop a possible death loop inside
                // the minecraft server code
                startWithSeekers(seekers)
            }
        } else {
            startWithSeekers(seekers)
        }
    }

    private fun startWithSeekers(seekers: Set<UUID>) {
        synchronized(lock) {
            if (status != Status.LOBBY) return

            status = Status.HIDING
            timer = null

            glow.reset()
            taunt.reset()
            border.reset()

            val players = teams.getPlayers()
            teams.reset()

            // load players into teams
            players.forEach {
                if (seekers.contains(it.uuid)) {
                    loadSeeker(it)
                } else {
                    loadHider(it)
                }
            }

            // reset game state
            initialTeams = teams.getMappings()
            hiderKills.clear()
            seekerKills.clear()
            hiderDeaths.clear()
            seekerDeaths.clear()

            // reload sidebar
            reloadGameBoards()
        }
    }

    fun getInitialTeams(): Map<UUID, Team> {
        return initialTeams.toMap()
    }

    fun getLastWinners(): Set<UUID> {
        return lastWinners.toSet()
    }

    fun hasPlayerLeft(): Boolean {
        return playerLeft
    }

    private fun updatePlayerInfo(uuid: UUID, reason: WinType) {
        val team = gameMode.getEffectiveTeam(uuid) ?: return
        val data = plugin.database?.getPlayer(uuid) ?: return

        when (reason) {
            WinType.SEEKERS_WIN -> {
                if (team == Team.SEEKER) {
                    data.seekerWins++
                    lastWinners.add(uuid)
                }
                if (team == Team.HIDER) data.hiderLosses++
            }

            WinType.HIDERS_WIN -> {
                if (team == Team.SEEKER) data.seekerLosses++
                if (team == Team.HIDER) {
                    data.hiderWins++
                    lastWinners.add(uuid)
                }
            }

            WinType.LAST_HIDER_WIN -> {
                if (team == Team.SEEKER) data.seekerLosses++
                if (team == Team.HIDER) {
                    val lastHider = gameMode.getLastHider()
                    if (uuid == lastHider) {
                        data.hiderWins++
                        lastWinners.add(uuid)
                    } else {
                        data.hiderLosses++
                    }
                }
            }

            else -> {}
        }

        data.seekerKills += seekerKills.getOrDefault(uuid, 0u)
        data.hiderKills += hiderKills.getOrDefault(uuid, 0u)
        data.seekerDeaths += seekerDeaths.getOrDefault(uuid, 0u)
        data.hiderDeaths += hiderDeaths.getOrDefault(uuid, 0u)

        plugin.database?.upsertPlayer(data)
    }

    fun stop(reason: WinType) {
        if (!status.inProgress()) return
        val uuids = teams.getUUIDs()

        synchronized(lock) {
            round++
            status = Status.FINISHED
            timer = null
        }

        val message = gameMode.gameOverMessage(reason)
        val title = gameMode.gameOverTitle(reason)
        val prefix =
            when (reason) {
                WinType.STOPPED,
                WinType.PLAYERS_LEFT -> plugin.locale.prefix.abort
                else -> plugin.locale.prefix.gameOver
            }

        broadcast(prefix + message)
        if (plugin.config.gameOverTitle) {
            broadcastTitle(title, message)
        }

        // update database
        lastWinners.clear()
        uuids.forEach { updatePlayerInfo(it, reason) }

        if (plugin.config.leaveOnEnd) {
            uuids.forEach { leave(it) }
        }

        teams.reset()
    }

    fun join(uuid: UUID) {
        val player = plugin.shim.getPlayer(uuid) ?: return
        val spectator: Boolean

        synchronized(lock) {
            if (teams.contains(uuid)) return
            if (teams.size() >= plugin.config.lobby.max) return

            // try to select a map
            if (map == null && selectMap() == null) {
                // map loading failed :(
                player.message(plugin.locale.prefix.error + plugin.locale.map.none)
                return
            }

            spectator = status != Status.LOBBY

            if (plugin.config.saveInventory) {
                savedInventories[uuid] = player.getInventory().getContents()
            }

            if (plugin.config.saveScoreBoard) {
                savedScoreBoards[uuid] = player.getScoreBoard()
            }
        }

        if (spectator) {
            loadSpectator(player)
            reloadGameBoard(plugin, player)
            player.message(plugin.locale.prefix.default + plugin.locale.game.join)
            return
        }

        loadPlayerIntoLobby(player)
        reloadLobbyBoards()

        broadcast(plugin.locale.prefix.default + plugin.locale.lobby.join.with(player.name))
    }

    fun leave(uuid: UUID) {
        synchronized(lock) {
            if (!teams.contains(uuid)) return
            if (teams.isHider(uuid) || teams.isSeeker(uuid)) playerLeft = true
            teams.remove(uuid)
        }

        val savedInv = savedInventories.remove(uuid)
        val savedBoard = savedScoreBoards.remove(uuid)
        val player = plugin.shim.getPlayer(uuid) ?: return

        resetPlayer(player)

        broadcast(plugin.locale.prefix.default + plugin.locale.game.leave.with(player.name))

        // restore inventory
        if (plugin.config.saveInventory) {
            savedInv?.let { player.getInventory().setContents(it) }
        }

        // reset score board
        if (plugin.config.saveScoreBoard) {
            player.setScoreBoard(savedBoard)
        } else {
            player.setScoreBoard(null)
        }

        // reload sidebar
        if (status.inProgress()) {
            reloadGameBoards()
        } else {
            reloadLobbyBoards()
        }

        // teleport away player
        if (plugin.config.leaveType == ConfigLeaveType.PROXY) {
            val server = plugin.config.leaveServer
            val successful = plugin.shim.sendPlayerToServer(uuid, server)
            if (!successful) {
                player.message(plugin.locale.prefix.error + plugin.locale.command.sendToServerFailed.with(server))
                player.teleport(plugin.config.exit)
            }
        } else {
            plugin.config.exit?.let { player.teleport(it) }
        }
    }

    fun addKill(uuid: UUID) {
        when (teams.get(uuid)) {
            Team.HIDER -> {
                hiderKills[uuid] = hiderKills.getOrDefault(uuid, 0u) + 1u
            }

            Team.SEEKER -> {
                seekerKills[uuid] = seekerKills.getOrDefault(uuid, 0u) + 1u
            }

            else -> {}
        }
    }

    fun addDeath(uuid: UUID) {
        when (teams.get(uuid)) {
            Team.HIDER -> {
                hiderDeaths[uuid] = hiderDeaths.getOrDefault(uuid, 0u) + 1u
            }

            Team.SEEKER -> {
                seekerDeaths[uuid] = seekerDeaths.getOrDefault(uuid, 0u) + 1u
            }

            else -> {}
        }
    }

    private fun reloadLobbyBoards() {
        teams.getPlayers().forEach { reloadLobbyBoard(plugin, it) }
    }

    private fun reloadGameBoards() {
        teams.getPlayers().forEach { reloadGameBoard(plugin, it) }
    }

    /** during Status.LOBBY */
    private fun whileWaiting() {
        val countdown = plugin.config.lobby.countdown
        val changeCountdown = plugin.config.lobby.changeCountdown

        if (isSecond) reloadLobbyBoards()

        var time: ULong
        // countdown is disabled when set to at 0s
        if (countdown == 0UL || teams.size() < plugin.config.lobby.min) {
            timer = null
            return
        }

        time = timer ?: countdown
        if (teams.size() >= changeCountdown && changeCountdown != 0u) time = min(time, 10UL)
        if (isSecond && time > 0UL) time--
        timer = time

        if (time == 0UL) {
            start()
            return
        }
    }

    /** during Status.HIDING */
    private fun whileHiding() {
        if (!isSecond) return

        if (timer != 0UL) {
            gameMode.getWinCondition()?.let(this::stop)
            playerLeft = false
        }

        if (isSecond) reloadGameBoards()

        val time = timer ?: plugin.config.hidingLength
        val message: String

        when (time) {
            0UL -> {
                message = plugin.locale.game.start
                status = Status.SEEKING
                timer = null
                loadHiders()
                loadSeekers()
            }

            1UL -> {
                message = plugin.locale.game.countdown.last
                timer = time - 1UL
            }

            else -> {
                message = plugin.locale.game.countdown.notify.with(time)
                timer = time - 1UL
            }
        }

        if (time % 5UL == 0UL || time <= 5UL) {
            val prefix = plugin.locale.prefix.default
            teams.getPlayers().forEach { player ->
                when (plugin.config.countdownDisplay) {
                    ConfigCountdownDisplay.CHAT -> {
                        player.message(prefix + message)
                    }

                    ConfigCountdownDisplay.ACTIONBAR -> {
                        player.actionBar(prefix + message)
                    }

                    ConfigCountdownDisplay.TITLE -> {
                        if (time != 30UL) player.title(" ", message)
                    }
                }
            }
        }
    }

    /** @returns distance to the closest seeker to the player */
    private fun distanceToSeeker(player: Player): Double {
        val distances =
            teams.getSeekerPlayers().mapNotNull { seeker ->
                player.getLocation().distance(seeker.getLocation())
            }
        return distances.minOrNull() ?: Double.POSITIVE_INFINITY
    }

    /** plays the seeker ping for a hider */
    private fun playSeekerPing(hider: Player) {
        val distance = distanceToSeeker(hider)

        // read config values
        val distances = plugin.config.seekerPing.distances
        val sounds = plugin.config.seekerPing.sounds

        when (gameTick % 10u) {
            0u -> {
                if (distance < distances.level1.toDouble()) {
                    hider.playSound(sounds.heartbeatNoise, sounds.leadingVolume, sounds.pitch)
                }
                if (distance < distances.level3.toDouble()) {
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
                }
            }

            3u -> {
                if (distance < distances.level1.toDouble()) {
                    hider.playSound(sounds.heartbeatNoise, sounds.volume, sounds.pitch)
                }
                if (distance < distances.level3.toDouble()) {
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
                }
            }

            6u -> {
                if (distance < distances.level3.toDouble()) {
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
                }
            }

            9u -> {
                if (distance < distances.level2.toDouble()) {
                    hider.playSound(sounds.ringingNoise, sounds.volume, sounds.pitch)
                }
            }
        }
    }

    /** during Status.SEEKING */
    private fun whileSeeking() {
        var time = timer

        if (time == null && plugin.config.gameLength != 0UL) {
            time = plugin.config.gameLength
        }

        if (isSecond) {
            if (time != null && time > 0UL) time--

            taunt.update()
            glow.update()
            border.update()
        }

        timer = time

        if (isSecond) {
            // seperate to have correct up to date time
            reloadGameBoards()
        }

        // play seeker ping
        if (plugin.config.seekerPing.enabled) {
            teams.getHiderPlayers().forEach { playSeekerPing(it) }
        }

        // update spectator flight
        // (the toggle they have only changed allowed flight)
        teams.getSpectatorPlayers().forEach { it.setFlying(it.getAllowedFlight()) }

        gameMode.getWinCondition()?.let(this::stop)
        playerLeft = false
    }

    /** during Status.FINISHED */
    private fun whileFinished() {
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

            teams.getPlayers().forEach { loadPlayerIntoLobby(it) }
        }
    }

    fun broadcast(message: String) {
        teams.getPlayers().forEach { it.message(message) }
    }

    private fun broadcastTitle(title: String, subTitle: String) {
        teams.getPlayers().forEach { it.title(title, subTitle) }
    }

    private fun loadHiders() = teams.getHiderPlayers().forEach { loadHider(it) }

    private fun loadSeekers() = teams.getSeekerPlayers().forEach { loadSeeker(it) }

    private fun setPlayerHidden(player: Player, hidden: Boolean) {
        if (hidden) {
            plugin.entityHider.hideEntity(player, player.uuid)
        } else {
            plugin.entityHider.showEntity(player)
        }
    }

    private fun resetPlayer(player: Player, revealDisguise: Boolean = true) {
        player.setFlying(false)
        player.setAllowedFlight(false)
        player.setGameMode(Player.GameMode.ADVENTURE)
        player.getInventory().clearAll()
        player.clearEffects()
        player.satiate()
        player.heal()
        if (revealDisguise) {
            plugin.disguiser.reveal(player.uuid)
            setPlayerHidden(player, false)
        }
    }

    private fun givePlayerItems(player: Player, items: List<ItemConfig>): UInt {
        val inventory = player.getInventory()
        var nextSlot = 0u
        for (itemConfig in items) {
            val item = plugin.parseItem(itemConfig) ?: continue
            val slot = itemConfig.slot ?: nextSlot
            inventory.set(slot, item)
            nextSlot = maxOf(nextSlot, slot) + 1u
        }
        return nextSlot
    }

    fun loadHider(hider: Player) {
        if (teams.get(hider.uuid) != Team.HIDER) {
            hider.title(plugin.locale.game.team.hider, plugin.locale.game.team.hiderSubtitle)
        }

        teams.put(hider.uuid, Team.HIDER)

        if (status == Status.HIDING) {
            hider.teleport(map?.gameSpawn)
        }

        resetPlayer(hider, status != Status.SEEKING)

        if (status == Status.HIDING) {
            hider.setSpeed(5u)

            // open block hunt picker
            if (map?.config?.blockHunt?.enabled == true) {
                val map = map ?: return
                val inv = BlockHuntMenu.create(plugin, map) ?: return
                hider.showInventory(inv)
            }

            // dont give hider items
            // when in hiding mode
            return
        }

        val nextSlot =
            if (plugin.config.pvp) {
                givePlayerItems(hider, plugin.itemsConfig.hiderItems)
            } else {
                // only give the glow power-up if pvp is disabled
                0u
            }

        // glow power-up
        if (!plugin.config.alwaysGlow && plugin.config.glow.enabled) {
            val item = plugin.parseItem(plugin.config.glow.item)
            val slot = plugin.config.glow.item.slot ?: nextSlot
            item?.let { hider.getInventory().set(slot, it) }
        }

        if (!plugin.config.pvp) return

        val inventory = hider.getInventory()
        inventory.setHelmet(plugin.parseItem(plugin.itemsConfig.hiderHelmet))
        inventory.setChestplate(plugin.parseItem(plugin.itemsConfig.hiderChestplate))
        inventory.setLeggings(plugin.parseItem(plugin.itemsConfig.hiderLeggings))
        inventory.setBoots(plugin.parseItem(plugin.itemsConfig.hiderBoots))

        plugin.itemsConfig.hiderEffects.mapNotNull { plugin.parseEffect(it) }.forEach { hider.giveEffect(it) }
    }

    fun loadSeeker(seeker: Player, onDeath: Boolean = false) {
        if (teams.get(seeker.uuid) != Team.SEEKER) {
            seeker.title(plugin.locale.game.team.seeker, plugin.locale.game.team.seekerSubtitle)
        }

        teams.put(seeker.uuid, Team.SEEKER)

        when (status) {
            Status.HIDING -> {
                seeker.teleport(map?.seekerLobbySpawn)
            }
            Status.SEEKING if plugin.config.delayedRespawn.enabled && onDeath -> {
                val time = plugin.config.delayedRespawn.delay
                val currentRound = round
                seeker.teleport(map?.seekerLobbySpawn)
                seeker.message(plugin.locale.prefix.default + plugin.locale.game.respawn.with(time))
                plugin.shim.scheduleEvent(time * 20UL) {
                    if (status == Status.SEEKING && round == currentRound) {
                        seeker.teleport(map?.gameSpawn)
                    }
                }
            }
            else -> {
                seeker.teleport(map?.gameSpawn)
            }
        }

        resetPlayer(seeker)

        if (status == Status.HIDING || !plugin.config.pvp) {
            // dont give players items in the
            // hiding phase
            return
        }

        givePlayerItems(seeker, plugin.itemsConfig.seekerItems)

        val inventory = seeker.getInventory()
        inventory.setHelmet(plugin.parseItem(plugin.itemsConfig.seekerHelmet))
        inventory.setChestplate(plugin.parseItem(plugin.itemsConfig.seekerChestplate))
        inventory.setLeggings(plugin.parseItem(plugin.itemsConfig.seekerLeggings))
        inventory.setBoots(plugin.parseItem(plugin.itemsConfig.seekerBoots))

        plugin.itemsConfig.seekerEffects.mapNotNull { plugin.parseEffect(it) }.forEach { seeker.giveEffect(it) }
    }

    fun loadSpectator(spectator: Player) {
        if (teams.get(spectator.uuid) != Team.SPECTATOR) {
            spectator.title(
                plugin.locale.game.team.spectator,
                plugin.locale.game.team.spectatorSubtitle,
            )
        }

        teams.put(spectator.uuid, Team.SPECTATOR)
        spectator.teleport(map?.gameSpawn)
        resetPlayer(spectator)
        spectator.setAllowedFlight(true)
        spectator.setFlying(true)

        val inventory = spectator.getInventory()
        val teleportItem = plugin.parseItem(plugin.config.spectatorItems.teleport)
        val flightItem = plugin.parseItem(plugin.config.spectatorItems.flight)

        teleportItem?.let { inventory.set(plugin.config.spectatorItems.teleport.slot ?: 3u, it) }
        flightItem?.let { inventory.set(plugin.config.spectatorItems.flight.slot ?: 6u, it) }

        setPlayerHidden(spectator, true)
    }

    private fun loadPlayerIntoLobby(player: Player) {
        teams.put(player.uuid, Team.UNASSIGNED)
        player.teleport(map?.lobbySpawn)
        resetPlayer(player)

        val inventory = player.getInventory()
        val leaveItem = plugin.parseItem(plugin.config.lobby.leaveItem)
        val startItem = plugin.parseItem(plugin.config.lobby.startItem)

        leaveItem?.let { inventory.set(plugin.config.lobby.leaveItem.slot ?: 0u, it) }
        if (player.hasPermission("hs.start")) {
            startItem?.let { inventory.set(plugin.config.lobby.startItem.slot ?: 8u, it) }
        }
    }
}
