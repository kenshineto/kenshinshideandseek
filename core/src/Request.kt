package cat.freya.khs

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class Request(val fn: () -> Unit, val lengthSeconds: Long) {
    val start = System.currentTimeMillis()
    val expired: Boolean
        get() = (System.currentTimeMillis() - start) < lengthSeconds * 1000
}

val REQUESTS: MutableMap<UUID, Request> = ConcurrentHashMap<UUID, Request>()
