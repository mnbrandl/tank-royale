package io.robocode2.bootstrap.util

import io.robocode2.bootstrap.BootstrapException
import io.robocode2.bootstrap.model.BotInfo
import io.robocode2.bootstrap.util.OSUtil.OSType.Windows
import io.robocode2.bootstrap.util.OSUtil.OSType.MacOS
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.MissingFieldException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonParsingException
import kotlinx.serialization.parse
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Paths
import java.io.*


class BotFinder(val bootDirPath: String) {

    @ImplicitReflectionSerializer
    fun findBotInfos(): Set<BotInfo> {
        val botNames = findBotNames()
        val botInfo = HashSet<BotInfo>()
        botNames.forEach { botName ->
            try {
                botInfo += getBotInfo(botName)
            } catch (ex: Exception) {
                System.err.println(ex.message)
            }
        }
        return botInfo
    }

    fun findOsScript(botName: String): String? = when (OSUtil.getOsType()) {
        Windows -> findWindowsScript(botName)
        MacOS -> findMacOsScript(botName)
        else -> findFirstUnixScript(botName)
    }

    private fun findWindowsScript(botName: String): String? = when {
        File(botsDir(), "$botName.bat").exists() -> "$botName.bat"
        File(botsDir(), "$botName.ps1").exists() -> "$botName.ps1"
        else -> null
    }

    private fun findMacOsScript(botName: String): String? = when {
        File(botsDir(), "$botName.command").exists() -> "$botName.command"
        else -> findFirstUnixScript(botName)
    }

    private fun findFirstUnixScript(botName: String): String? {
        if (File(botsDir(), "$botName.sh").exists()) return "$botName.sh"

        val files = botsDir().listFiles(BotFilenameFilter(botName))
        files.forEach { file ->
            if (readFirstLine(file).startsWith("#!")) return file.absolutePath
        }
        return null
    }

    private fun readFirstLine(filePath: File): String {
        return FileInputStream(filePath).bufferedReader().readLine()
    }

    private fun findBotNames(): Set<String> {
        val files = botsDir().listFiles(FilenameExtFilter(arrayOf("json")))

        val names = HashSet<String>()
        files?.forEach { names += it.nameWithoutExtension }
        return names
    }

    @ImplicitReflectionSerializer
    private fun getBotInfo(botName: String): BotInfo {
        var filePath = "<unknown>"
        try {
            filePath = File(botsDir(), "$botName.json").absolutePath
            val content = readAllLines(filePath)
            return Json.parse(content)
        } catch (ex: JsonParsingException) {
            throw BootstrapException("Could not parse JSON file: $filePath")
        } catch (ex: MissingFieldException) {
            throw BootstrapException("${ex.message}. File: $filePath")
        }
    }

    private fun readAllLines(filePath: String): String {
        val contentBuilder = StringBuilder()
        Files.lines(Paths.get(filePath), StandardCharsets.UTF_8).use { stream ->
            stream.forEach { s -> contentBuilder.append(s).append('\n') }
        }
        return contentBuilder.toString()
    }

    private fun botsDir(): File = File(bootDirPath, "bots")
}

internal class FilenameExtFilter(private val fileExtensions: Array<String>) : FilenameFilter {
    override fun accept(dir: File, filename: String): Boolean {
        fileExtensions.forEach {
            if (File(dir, filename).isDirectory) return false
            val ext = it.replace(".", "").toLowerCase()
            if (filename.toLowerCase().endsWith(".$ext")) return true
        }
        return false
    }
}

internal class BotFilenameFilter(private val botName: String) : FilenameFilter {
    override fun accept(dir: File, filename: String): Boolean =
            filename.toLowerCase().startsWith('.' + botName.toLowerCase())
}