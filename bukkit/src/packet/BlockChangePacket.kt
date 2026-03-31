package cat.freya.khs.bukkit.packet

import com.github.retrooper.packetevents.PacketEvents
import com.github.retrooper.packetevents.util.Vector3i
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerBlockChange
import io.github.retrooper.packetevents.util.SpigotConversionUtil
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player as BukkitPlayer

data class BlockChangePacket(val location: Location, val material: Material) {
    fun send(player: BukkitPlayer) {
        // 1.9+ only
        val blockData = Bukkit.createBlockData(material)
        val state = SpigotConversionUtil.fromBukkitBlockData(blockData)
        val vector = Vector3i(location.blockX, location.blockY, location.blockZ)
        val packet = WrapperPlayServerBlockChange(vector, state)
        PacketEvents.getAPI().playerManager.sendPacket(player, packet)
    }
}
