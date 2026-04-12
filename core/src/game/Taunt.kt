package cat.freya.khs.game

import java.util.UUID

class Taunt(val game: Game) {
    @Volatile var timer: ULong = 0UL

    @Volatile var running: Boolean = true

    @Volatile var taunted: UUID? = null

    @Volatile var last: UUID? = null

    val expired: Boolean
        get() = (game.hiderSize <= 1UL && game.plugin.config.taunt.disableForLastHider)

    fun reset() {
        running = false
        timer = game.plugin.config.taunt.delay
        last = taunted
        taunted = null
    }

    fun update() {
        if (!game.plugin.config.taunt.enabled || expired) return

        if (timer != 0UL) {
            timer--
            return
        }

        // running means we are to taunt!
        if (running) {
            // if player left, well, damn
            if (taunted?.let { game.hasPlayer(it) } != true) {
                reset()
                return
            }

            val player = taunted?.let { game.plugin.shim.getPlayer(it) }
            player?.taunt()

            game.broadcast(game.plugin.locale.prefix.taunt + game.plugin.locale.taunt.activate)
            reset()
            return
        }

        // select a hider to taunt
        val hider =
            game.hiderPlayers
                .filter {
                    // only block last hider if there is another
                    // hider to taunt
                    it.uuid != last || (game.hiderSize <= 1UL)
                }.randomOrNull() ?: return

        game.broadcast(game.plugin.locale.prefix.taunt + game.plugin.locale.taunt.warning)
        hider.message(game.plugin.locale.taunt.chosen)
        timer = 30UL
        running = true
        taunted = hider.uuid
    }
}
