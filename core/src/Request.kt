package cat.freya.khs

data class Request(private val delayTicks: ULong, private val fn: () -> Unit) {
    private var ticksLeft = delayTicks

    fun update() {
        if (ticksLeft > 0UL) ticksLeft--
    }

    fun isExpired(): Boolean {
        return ticksLeft == 0UL
    }

    fun execute() {
        fn()
    }
}
