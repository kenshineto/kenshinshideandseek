package cat.freya.khs.event

abstract class Event {
    var cancelled: Boolean = false

    fun cancel() {
        cancelled = true
    }
}
