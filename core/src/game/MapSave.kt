package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.world.World
import java.io.File
import kotlin.error
import kotlin.io.deleteRecursively

private fun copyWorldFolder(
    plugin: Khs,
    map: KhsMap,
    loader: World.Loader,
    name: String,
    isMca: Boolean,
): Boolean {
    val dir = loader.dir
    val temp = loader.tempSaveDir

    val bounds = map.bounds() ?: return false

    val region = File(dir, name)
    val tempRegion = File(temp, name)

    if (!tempRegion.exists() && tempRegion.mkdirs() == false) {
        plugin.shim.logger.error("could not create directory: ${tempRegion.getPath()}")
        return false
    }

    val files = region.list()
    if (files == null) {
        plugin.shim.logger.error("could not access directory: ${region.getPath()}")
        return false
    }

    for (fileName in files) {
        val parts = fileName.split("\\.")
        if (isMca && parts.size > 1) {
            if (
                (parts[1].toInt() < bounds.minX / 512) ||
                    (parts[1].toInt() > bounds.maxX / 512) ||
                    (parts[2].toInt() < bounds.minZ / 512) ||
                    (parts[2].toInt() > bounds.maxZ / 512)
            )
                continue
        }

        val srcFile = File(region, fileName)
        if (srcFile.isDirectory()) {
            copyWorldFolder(plugin, map, loader, name + File.separator + fileName, false)
        } else {
            val destFile = File(tempRegion, fileName)
            srcFile.copyTo(destFile, overwrite = true)
        }
    }

    return true
}

private fun copyWorldFile(loader: World.Loader, name: String) {
    val dir = loader.dir
    val temp = loader.tempSaveDir

    val srcFile = File(dir, name)
    val destFile = File(temp, name)

    srcFile.copyTo(destFile, overwrite = true)
}

fun mapSave(plugin: Khs, map: KhsMap): Result<Unit> =
    runCatching {
            plugin.shim.logger.info("starting map save for: ${map.worldName}")
            plugin.saving = true

            plugin.shim.broadcast(plugin.locale.prefix.default + plugin.locale.map.save.start)
            plugin.shim.broadcast(plugin.locale.prefix.warning + plugin.locale.map.save.warning)

            if (plugin.config.mapSaveEnabled == false) error("map saves are disabled!")

            val loader = plugin.shim.getWorldLoader(map.worldName)
            val mapSaveLoader = plugin.shim.getWorldLoader(map.gameWorldName)
            val dir = loader.dir

            if (!dir.exists()) {
                plugin.shim.broadcast(
                    plugin.locale.prefix.error + plugin.locale.map.save.failedLocate
                )
                error("there is no map to save")
            }

            mapSaveLoader.unload()

            copyWorldFolder(plugin, map, loader, "region", true)
            copyWorldFolder(plugin, map, loader, "entities", true)
            copyWorldFolder(plugin, map, loader, "datapacks", false)
            copyWorldFolder(plugin, map, loader, "data", false)
            copyWorldFile(loader, "level.dat")

            val dest = mapSaveLoader.dir
            if (dest.exists() && !dest.deleteRecursively()) {
                plugin.shim.broadcast(
                    plugin.locale.prefix.error +
                        plugin.locale.map.save.failedDir.with(dest.toPath())
                )
                error("could not delete destination directory")
            }

            val tempDest = loader.tempSaveDir
            if (!tempDest.renameTo(dest)) {
                plugin.shim.broadcast(
                    plugin.locale.prefix.error +
                        plugin.locale.map.save.failedDir.with(tempDest.toPath())
                )
                error("could not rename: ${tempDest.toPath()}")
            }
        }
        .onSuccess {
            plugin.saving = false
            plugin.shim.broadcast(plugin.locale.prefix.default + plugin.locale.map.save.finished)
        }
        .onFailure {
            plugin.saving = false
            plugin.shim.broadcast(
                plugin.locale.prefix.error +
                    plugin.locale.map.save.failed.with(it.message ?: "unknown error")
            )
        }
