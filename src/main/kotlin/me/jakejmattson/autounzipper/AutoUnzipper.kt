package me.jakejmattson.autounzipper

import org.rauschig.jarchivelib.*
import java.io.*
import java.nio.file.*

private const val SHOULD_DELETE_ORIGINAL = true

fun main(args: Array<String>) {
    require(args.isNotEmpty()) { "Expected an input path as a command line argument." }

    val inputFile = File(args.first())

    require(inputFile.exists()) { "Input path does not exist." }
    require(inputFile.isDirectory) { "Input path must be a directory." }

    watchDirectory(inputFile)
}

private fun watchDirectory(dir: File) {
    val path = dir.toPath()
    val service = path.fileSystem.newWatchService()

    path.register(service, StandardWatchEventKinds.ENTRY_CREATE)

    while (true) {
        val key = service.take()

        key.pollEvents()
            .filter { it.kind() === StandardWatchEventKinds.ENTRY_CREATE }
            .map { File( dir, it.context().toString()) }
            .forEach {
                unArchive(it)
            }

        if (!key.reset())
            break
    }
}

private fun unArchive(src: File) {
    val archiver = try {
        ArchiverFactory.createArchiver(src)
    } catch (e: Exception) {
        return
    }

    val dst = File(src.parentFile, src.nameWithoutExtension)
    archiver.extract(src, dst)

    if (SHOULD_DELETE_ORIGINAL)
        src.delete()
}