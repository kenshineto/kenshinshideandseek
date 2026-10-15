package cat.freya.khs

import java.util.UUID
import kotlin.synchronized

class DebounceManager {
    val blockHuntAttack = Debouncer()
    val disguiseSwitch = Debouncer()

    class Debouncer {
        private val inner = mutableMapOf<UUID, ULong>()

        fun cleanup() {
            synchronized(inner) { inner.clear() }
        }

        fun isDebounced(uuid: UUID): Boolean {
            synchronized(inner) {
                return inner.containsKey(uuid)
            }
        }

        fun getDebounce(uuid: UUID): ULong? {
            synchronized(inner) {
                return inner[uuid]
            }
        }

        fun setDebounce(uuid: UUID, delayTicks: ULong) {
            synchronized(inner) {
                inner[uuid] = delayTicks
            }
        }

        fun removeDebounce(uuid: UUID) {
            synchronized(inner) {
                inner.remove(uuid)
            }
        }

        fun update() {
            synchronized(inner) {
                inner.forEach { (uuid, delay) ->
                    if (delay == 0UL) {
                        inner.remove(uuid)
                    } else {
                        inner[uuid] = delay - 1UL
                    }
                }
            }
        }
    }

    fun update() {
        blockHuntAttack.update()
        disguiseSwitch.update()
    }

    fun cleanup() {
        blockHuntAttack.cleanup()
        disguiseSwitch.cleanup()
    }
}
