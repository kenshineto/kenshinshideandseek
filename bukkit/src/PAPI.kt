package cat.freya.khs.bukkit

import cat.freya.khs.PlaceholderRequest
import cat.freya.khs.handlePlaceholder
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.OfflinePlayer

class KhsPAPI(val plugin: KhsPlugin) : PlaceholderExpansion() {
    override fun getIdentifier() = "hs"

    override fun getAuthor() = "KenshinEto"

    override fun getVersion() = plugin.description.version

    override fun persist() = true

    override fun onRequest(player: OfflinePlayer?, params: String): String? {
        val uuid = player?.uniqueId ?: return null
        val req = PlaceholderRequest(plugin.khs, uuid, params)
        return handlePlaceholder(req)
    }
}
