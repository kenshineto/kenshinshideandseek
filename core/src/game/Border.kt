package cat.freya.khs.game

import cat.freya.khs.config.WorldBorderConfig
import cat.freya.khs.world.World

class Border(val game: Game) {

    enum class State {
        WAITING,
        WARNED,
        SHRINKING,
    }

    @Volatile var timer: ULong = 0UL

    @Volatile var state: State = State.WAITING

    @Volatile private var enabled: Boolean = false

    private val border: World.Border?
        get() = game.map?.gameWorld?.border

    private val borderConfig: WorldBorderConfig?
        get() = game.map?.config?.worldBorder

    val expired: Boolean
        get() = border?.size?.let { it <= 100.0 } != true

    fun reset() {
        enabled = false
        state = State.WAITING

        val border = border ?: return
        val borderConfig = borderConfig ?: return

        val x = borderConfig.pos?.x ?: return
        val z = borderConfig.pos?.z ?: return
        val size = borderConfig.size ?: return

        border.move(x, z, size, 0UL)
    }

    fun update() {
        if (borderConfig?.enabled != true) return

        if (timer != 0UL) {
            timer--
            return
        }

        if (state == State.WARNED) {
            // start the world border movement!
            var amount = borderConfig?.move ?: return
            val currentSize = border?.size?.toULong() ?: return

            if (amount >= currentSize) return

            if (amount - 100UL <= currentSize) amount = 100UL

            timer = 30UL
            state = State.SHRINKING

            border?.move(amount, timer)
            game.broadcast(game.plugin.locale.worldBorder.shrinking)
            return
        }

        if (state == State.SHRINKING) {
            timer = borderConfig?.delay ?: return
            state = State.WAITING
            return
        }

        game.broadcast(game.plugin.locale.prefix.border + game.plugin.locale.worldBorder.warn)
        timer = 30UL
        state = State.WARNED
    }
}
