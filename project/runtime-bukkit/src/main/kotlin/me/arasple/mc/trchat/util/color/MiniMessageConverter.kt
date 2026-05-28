package me.arasple.mc.trchat.util.color

import org.bukkit.ChatColor

object MiniMessageConverter {

    private val LEGACY_TO_MINIMESSAGE = mapOf(
        ChatColor.BLACK to "<black>",
        ChatColor.DARK_BLUE to "<dark_blue>",
        ChatColor.DARK_GREEN to "<dark_green>",
        ChatColor.DARK_AQUA to "<dark_aqua>",
        ChatColor.DARK_RED to "<dark_red>",
        ChatColor.DARK_PURPLE to "<dark_purple>",
        ChatColor.GOLD to "<gold>",
        ChatColor.GRAY to "<gray>",
        ChatColor.DARK_GRAY to "<dark_gray>",
        ChatColor.BLUE to "<blue>",
        ChatColor.GREEN to "<green>",
        ChatColor.AQUA to "<aqua>",
        ChatColor.RED to "<red>",
        ChatColor.LIGHT_PURPLE to "<light_purple>",
        ChatColor.YELLOW to "<yellow>",
        ChatColor.WHITE to "<white>",
        ChatColor.MAGIC to "<obfuscated>",
        ChatColor.BOLD to "<bold>",
        ChatColor.STRIKETHROUGH to "<strikethrough>",
        ChatColor.UNDERLINE to "<underlined>",
        ChatColor.ITALIC to "<italic>",
        ChatColor.RESET to "<reset>"
    )

    private val LEGACY_CODE_TO_MINIMESSAGE = mapOf(
        '0' to "<black>",
        '1' to "<dark_blue>",
        '2' to "<dark_green>",
        '3' to "<dark_aqua>",
        '4' to "<dark_red>",
        '5' to "<dark_purple>",
        '6' to "<gold>",
        '7' to "<gray>",
        '8' to "<dark_gray>",
        '9' to "<blue>",
        'a' to "<green>",
        'b' to "<aqua>",
        'c' to "<red>",
        'd' to "<light_purple>",
        'e' to "<yellow>",
        'f' to "<white>",
        'k' to "<obfuscated>",
        'l' to "<bold>",
        'm' to "<strikethrough>",
        'n' to "<underlined>",
        'o' to "<italic>",
        'r' to "<reset>"
    )

    fun String.convertToMiniMessage(): String {
        var result = this
        
        result = convertLegacySection(result)
        result = convertAmpersandCodes(result)
        result = convertHexColors(result)
        
        return result
    }

    private fun convertLegacySection(text: String): String {
        val result = StringBuilder()
        var i = 0
        
        while (i < text.length) {
            if (text[i] == '§' && i + 1 < text.length) {
                val code = text[i + 1].lowercaseChar()
                LEGACY_CODE_TO_MINIMESSAGE[code]?.let { miniMessage ->
                    result.append(miniMessage)
                    i += 2
                    continue
                }
            }
            result.append(text[i])
            i++
        }
        
        return result.toString()
    }

    private fun convertAmpersandCodes(text: String): String {
        val result = StringBuilder()
        var i = 0
        
        while (i < text.length) {
            if (text[i] == '&' && i + 1 < text.length) {
                val code = text[i + 1].lowercaseChar()
                LEGACY_CODE_TO_MINIMESSAGE[code]?.let { miniMessage ->
                    result.append(miniMessage)
                    i += 2
                    continue
                }
            }
            result.append(text[i])
            i++
        }
        
        return result.toString()
    }

    private fun convertHexColors(text: String): String {
        var result = text
        
        val hexPatterns = listOf(
            Regex("&\\{#([A-Fa-f0-9]{6})\\}"),
            Regex("&#([A-Fa-f0-9]{6})"),
            Regex("\\{#([A-Fa-f0-9]{6})\\}"),
            Regex("<#([A-Fa-f0-9]{6})>")
        )
        
        for (pattern in hexPatterns) {
            result = result.replace(pattern) { match ->
                val hex = match.groupValues[1]
                "<#$hex>"
            }
        }
        
        return result
    }

    fun String.stripLegacyCodes(): String {
        var result = this
        
        result = result.replace(Regex("§[0-9a-fA-Fk-orK-OR]"), "")
        result = result.replace(Regex("&[0-9a-fA-Fk-orK-OR]"), "")
        
        return result
    }
}