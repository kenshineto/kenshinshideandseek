package cat.freya.khs.game

class Glow(val game: Game) {

    @Volatile var timer: ULong = 0UL
    @Volatile var running: Boolean = true

    fun start() {
        running = true
        if (game.plugin.config.glow.stackable) {
            timer += game.plugin.config.glow.time
        } else {
            timer = game.plugin.config.glow.time
        }
    }

    fun reset() {
        running = false
        timer = 0UL
    }

    private fun sendPackets(glow: Boolean) {
        for (hider in game.hiderPlayers) for (seeker in game.seekerPlayers) hider.setGlow(
            seeker,
            glow,
        )
    }

    fun update() {
        if (!game.plugin.config.glow.enabled) return

        if (game.plugin.config.alwaysGlow) {
            sendPackets(true)
            return
        }

        if (!running) return

        if (timer >= 0UL) timer--

        if (timer == 0UL) {
            running = false
            sendPackets(false)
        } else {
            sendPackets(true)
        }
    }
}
