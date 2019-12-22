package io.github.jakejmattson.autounzipper

import org.rauschig.jarchivelib.*
import java.io.*
import java.nio.file.*

private const val SHOULD_DELETE_ORIGINAL = true

fun main() {
    val downloads = System.getProperty("user.home") + "/Downloads/"
    watchDirectoryPath(File(downloads))
}

private fun watchDirectoryPath(dir: File) {
    val path = dir.toPath()

    path.fileSystem.newWatchService().use { service ->
        //Watch for creation events
        path.register(service, StandardWatchEventKinds.ENTRY_CREATE)

        while (true) {
            val key = service.take()

            for (watchEvent in key.pollEvents()) if (watchEvent.kind() === StandardWatchEventKinds.ENTRY_CREATE) {
                val newPath = (watchEvent as WatchEvent<*>).context()
                val newFile = File(dir, newPath.toString())
                unArchive(newFile)
            }

            if (!key.reset())
                break
        }
    }
}

@Throws(IOException::class)
private fun unArchive(src: File) {
    val split = src.name.split("\\.".toRegex(), 2)

    if (split.size < 2) return

    val archiver = when (split[1]) {
        "zip" -> ArchiverFactory.createArchiver(ArchiveFormat.ZIP)
        "tar.gz" -> ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP)
        "7z" -> ArchiverFactory.createArchiver(ArchiveFormat.SEVEN_Z)
        else -> return
    }

    val dst = File(src.parentFile, split[0])
    archiver.extract(src, dst)

    if (SHOULD_DELETE_ORIGINAL)
        src.delete()
}