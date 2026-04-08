package cat.freya.khs.game

import cat.freya.khs.Khs
import cat.freya.khs.world.MAP_SAVE_PREFIX
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.error
import kotlin.io.path.exists

class MapSaver(val plugin: Khs, val map: KhsMap) {

    private val loader = map.getWorldLoader()
    private val bounds = map.getBounds()

    private val rootSrcDir = loader.dir
    private val worldContainer = rootSrcDir.parent

    private val rootTempDir = worldContainer.resolve("temp_$MAP_SAVE_PREFIX${map.name}")
    private val rootDestDir = worldContainer.resolve("$MAP_SAVE_PREFIX${map.name}")

    private var failed = false

    private fun isMcaInBounds(name: String): Boolean {
        val parts = name.split(".")

        val regionX = parts.getOrNull(1)?.toInt() ?: return true
        val regionZ = parts.getOrNull(2)?.toInt() ?: return true
        val bounds = bounds ?: return true

        val inBounds =
            (regionX >= bounds.minX / 512) &&
                (regionX <= bounds.maxX / 512) &&
                (regionZ >= bounds.minZ / 512) &&
                (regionZ <= bounds.maxZ / 512)

        return inBounds
    }

    private fun isFileIgnored(path: Path): Boolean {
        val parent = path.parent
        val parentFileName = parent.fileName.toString()
        val fileName = path.fileName.toString()

        if (parentFileName == "data") {
            // handle paper/spiggot custom level data
            return when (fileName) {
                "paper" -> true
                "spigot" -> true
                "bukkit" -> true
                else -> false
            }
        }

        return false
    }

    private fun saveFolder(path: Path) {
        val src = rootSrcDir.resolve(path).toFile()
        val dest = rootTempDir.resolve(path).toFile()

        if (!src.exists()) return

        if (!dest.exists() && !dest.mkdirs()) {
            plugin.shim.logger.error("could not create directory: ${dest.path}")
            failed = true
            return
        }

        src.listFiles().forEach { file ->
            val filePath = path.resolve(file.name)

            if (isFileIgnored(filePath)) {
                return@forEach
            }

            if (file.isDirectory) {
                saveFolder(filePath)
            } else {
                saveFile(filePath)
            }
        }
    }

    private fun saveFolder(fileName: String) {
        saveFolder(Paths.get(fileName))
    }

    private fun saveFile(path: Path) {
        val src = rootSrcDir.resolve(path).toFile()
        val dest = rootTempDir.resolve(path).toFile()

        if (!src.exists()) return

        if (src.endsWith(".mca") && !isMcaInBounds(path.fileName.toString())) return

        src.copyTo(dest, overwrite = true)
    }

    private fun saveFile(fileName: String) {
        saveFile(Paths.get(fileName))
    }

    private fun unloadGameWorld() {
        val gameWorldLoader = map.getGameWorldLoader()
        gameWorldLoader.unload()
    }

    fun save(): Result<Unit> {
        if (!plugin.config.mapSaveEnabled) return Result.success(Unit)

        val saving = plugin.saving.getAndSet(true)
        if (saving) {
            // map save is already in progress, abort
            plugin.shim.logger.warning("mapSave called while map save is in progress")
            return Result.success(Unit)
        }

        return runCatching {
                plugin.shim.logger.info("starting map save for: ${map.worldName}")
                plugin.shim.broadcast(plugin.locale.prefix.default + plugin.locale.map.save.start)
                plugin.shim.broadcast(plugin.locale.prefix.warning + plugin.locale.map.save.warning)

                if (!rootSrcDir.exists()) {
                    plugin.shim.broadcast(
                        plugin.locale.prefix.error + plugin.locale.map.save.failedLocate
                    )
                    error("there is no map to save")
                }

                unloadGameWorld()

                saveFolder("data")
                saveFolder("datapacks")
                saveFolder("dimensions")
                saveFolder("entities")
                saveFolder("region")
                saveFile("level.dat")

                if (rootDestDir.exists() && !rootDestDir.toFile().deleteRecursively()) {
                    plugin.shim.broadcast(
                        plugin.locale.prefix.error +
                            plugin.locale.map.save.failedDir.with(rootDestDir)
                    )
                    error("could not delete destination directory")
                }

                if (!rootTempDir.toFile().renameTo(rootDestDir.toFile())) {
                    plugin.shim.broadcast(
                        plugin.locale.prefix.error +
                            plugin.locale.map.save.failedDir.with(rootTempDir)
                    )
                    error("could not rename: ${rootTempDir}")
                }
            }
            .onSuccess {
                plugin.saving.set(false)
                plugin.shim.broadcast(
                    plugin.locale.prefix.default + plugin.locale.map.save.finished
                )
            }
            .onFailure {
                plugin.saving.set(false)
                plugin.shim.broadcast(
                    plugin.locale.prefix.error +
                        plugin.locale.map.save.failed.with(it.message ?: "unknown error")
                )
            }
    }
}
