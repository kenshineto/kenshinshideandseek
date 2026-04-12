package cat.freya.khs.game

import cat.freya.khs.packet.EntityMetadataPacket

class Glow(val game: Game) {
    @Volatile var timer: ULong = 0UL

    @Volatile var running: Boolean = true

    // glow is only supported on 1.9+
    val supported: Boolean
        get() = game.plugin.shim.supports(9)

    fun start() {
        if (!supported) return

        running = true
        if (game.plugin.config.glow.stackable) {
            timer += game.plugin.config.glow.time
        } else {
            timer = game.plugin.config.glow.time
        }
    }

    fun reset() {
        if (!supported) return

        running = false
        timer = 0UL
    }

    private fun sendPackets(glow: Boolean) {
        game.hiderPlayers.forEach { hider ->
            game.seekerPlayers.forEach { seeker ->
                val flags = EntityMetadataPacket.Flags(glow)
                val packet = EntityMetadataPacket(game.plugin, seeker, flags)
                packet.send(hider)
            }
        }
    }

    fun update() {
        if (!game.plugin.config.glow.enabled || !supported) return

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
