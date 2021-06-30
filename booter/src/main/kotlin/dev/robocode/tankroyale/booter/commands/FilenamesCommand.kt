package dev.robocode.tankroyale.booter.commands

import dev.robocode.tankroyale.booter.model.BotEntry
import java.nio.file.Files
import java.nio.file.Files.list
import java.nio.file.Path
import java.util.function.Predicate
import kotlin.collections.ArrayList
import kotlin.collections.HashSet

class FilenamesCommand(private val botPaths: List<Path>) : Command(botPaths) {

    fun listBotEntries(gameTypesCSV: String?): List<BotEntry> {
        val gameTypes: List<String>? = gameTypesCSV?.split(",")?.map { it.trim() }

        val botNames = listBotNames()
        val botEntries = ArrayList<BotEntry>()
        botNames.forEach { botName ->
            try {
                val botInfo = getBotInfo(botName)
                if (botInfo != null && (gameTypes == null || botInfo.gameTypes.split(",").containsAll(gameTypes)))
                    botEntries.add(BotEntry(botName, botInfo))
            } catch (ex: Exception) {
                System.err.println("ERROR: ${ex.message}")
            }
        }
        return botEntries
    }

    private fun listBotNames(): Set<String> {
        val names = HashSet<String>()
        botPaths.forEach { dirPath ->
            val files = list(dirPath).filter(HasFileExtensions(arrayOf("json")))
            files?.forEach { path -> names += path.toFile().nameWithoutExtension }
        }
        return names
    }
}

private class HasFileExtensions(private val fileExtensions: Array<String>) : Predicate<Path> {

    override fun test(path: Path): Boolean {
        if (Files.isDirectory(path)) return false
        fileExtensions.forEach { ext ->
            if (path.toString().lowercase().endsWith(".${ext.lowercase()}")) return true
        }
        return false
    }
}