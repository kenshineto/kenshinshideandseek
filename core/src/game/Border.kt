package cat.freya.khs.game

import cat.freya.khs.config.WorldBorderConfig
import cat.freya.khs.world.World

const val BORDER_MIN_SIZE: ULong = 100UL

class Border(val game: Game) {

    enum class State {
        WAITING,
        WARNED,
        SHRINKING,
    }

    @Volatile var timer: ULong = 0UL

    @Volatile var state: State = State.WAITING

    @Volatile private var configValid: Boolean = false

    private val border: World.Border?
        get() = game.map?.gameWorld?.border

    private val borderConfig: WorldBorderConfig?
        get() = game.map?.config?.worldBorder

    val expired: Boolean
        get() = configValid && border?.size?.let { it > BORDER_MIN_SIZE.toDouble() } != true

    fun reset() {
        configValid = false
        state = State.WAITING

        val border = border ?: return
        border.reset()

        if (borderConfig?.enabled != true) return

        val x = borderConfig?.pos?.x ?: return
        val z = borderConfig?.pos?.z ?: return
        val size = borderConfig?.size ?: return
        val delay = borderConfig?.delay ?: return

        configValid = true
        border.move(x, z, size, 0UL)
        timer = delay
    }

    fun update() {
        // the map needs to be loaded for the reset()
        // function to succeed, and the first time it was
        // called may be before everything is setup
        if (!configValid) reset()

        if (!configValid || expired) return // nope

        if (timer != 0UL) {
            timer--
            return
        }

        if (state == State.WARNED) {
            // start the world border movement!
            var amount = borderConfig?.move ?: return
            val currentSize = border?.size?.toULong() ?: return

            if (amount >= currentSize) return
            val newSize = maxOf(currentSize - amount, BORDER_MIN_SIZE)

            timer = 30UL
            state = State.SHRINKING

            border?.move(newSize, timer)
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
