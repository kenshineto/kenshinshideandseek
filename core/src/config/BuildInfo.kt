package cat.freya.khs.config

// stores project metadata
data class BuildInfo(
    var id: String = "KenshinsHideAndSeek",
    var name: String = "Kenshin's Hide and Seek",
    var author: String = "KenshinEto",
    var version: String = "2.0.0",
    var telemetry: Boolean = false,
    var bstatsId: Int = 0,
)
