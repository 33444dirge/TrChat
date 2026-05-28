package me.arasple.mc.trchat.module.adventure

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer
import org.bukkit.inventory.ItemStack
import taboolib.module.chat.ComponentText
import taboolib.module.chat.Components
import taboolib.module.chat.impl.AdventureComponent

private val legacySerializer: Any? = try {
    LegacyComponentSerializer.legacySection()
} catch (_: Throwable) {
    null
}

private val gsonSerializer: Any? = try {
    GsonComponentSerializer.gson()
} catch (_: Throwable) {
    null
}

private val plainSerializer: Any? = try {
    PlainTextComponentSerializer.plainText()
} catch (_: Throwable) {
    null
}

val miniMessage: Any? = try {
    MiniMessage.miniMessage()
} catch (_: Throwable) {
    null
}

fun gson(component: Component) = (gsonSerializer as GsonComponentSerializer).serialize(component)

fun gson(string: String) = (gsonSerializer as GsonComponentSerializer).deserialize(string)

fun Component.toPlain() = (plainSerializer as PlainTextComponentSerializer).serialize(this)

fun ComponentText.toAdventure(): Component {
    return if (this is AdventureComponent) this.component
    else gson(this.toRawMessage())
}

fun Component.toNative(): ComponentText {
    return if (Components.useAdventure) AdventureComponent(this)
    else Components.parseRaw(gson(this))
}

fun ComponentText.hoverItemAdventure(item: ItemStack): ComponentText {
    this as? AdventureComponent ?: error("Unsupported component type.")
    this.latest.hoverEvent(item.asHoverEvent())
    return this
}

fun String.parseMiniMessage(): ComponentText {
    val mm = miniMessage as? MiniMessage ?: return Components.text("No MiniMessage support in your environment!")
    val converted = this.convertToMiniMessage()
    val component = mm.deserialize(converted)
    return if (Components.useAdventure) {
        AdventureComponent(component)
    } else {
        Components.parseRaw(gson(component))
    }
}

fun String.convertToMiniMessage(): String {
    if (!this.contains('&') && !this.contains('§')) {
        return this
    }

    var result = this

    result = convertHexColors(result)
    result = convertLegacyCodesToMiniMessage(result)

    return result
}

private fun convertHexColors(text: String): String {
    var result = text

    val hexPatterns = listOf(
        Regex("§#([A-Fa-f0-9]{6})"),
        Regex("&\\{#([A-Fa-f0-9]{6})}"),
        Regex("&#([A-Fa-f0-9]{6})"),
        Regex("\\{#([A-Fa-f0-9]{6})}")
    )

    for (pattern in hexPatterns) {
        result = result.replace(pattern) { match ->
            val hex = match.groupValues[1]
            "<#$hex>"
        }
    }

    return result
}

private fun convertLegacyCodesToMiniMessage(text: String): String {
    if (text.length < 2) return text

    val result = StringBuilder(text.length + 50)
    var i = 0

    while (i < text.length) {
        if (i < text.length - 13 &&
            (text[i] == '§' || text[i] == '&') &&
            text[i + 1].lowercaseChar() == 'x') {
            var isValidMinecraftRgb = true
            for (j in 2..13 step 2) {
                if (i + j >= text.length || (text[i + j] != '§' && text[i + j] != '&')) {
                    isValidMinecraftRgb = false
                    break
                }
                if (i + j + 1 >= text.length) {
                    isValidMinecraftRgb = false
                    break
                }
                val hexChar = text[i + j + 1].lowercaseChar()
                if (!hexChar.isDigit() && hexChar !in 'a'..'f') {
                    isValidMinecraftRgb = false
                    break
                }
            }

            if (isValidMinecraftRgb) {
                val r1 = text[i + 3]
                val r2 = text[i + 5]
                val g1 = text[i + 7]
                val g2 = text[i + 9]
                val b1 = text[i + 11]
                val b2 = text[i + 13]
                val hexColor = "<#$r1$r2$g1$g2$b1$b2>"
                result.append(hexColor)
                i += 14
                continue
            }
        }

        if (text[i] == '<' && i + 8 < text.length && text[i + 1] == '#') {
            var isValidHex = true
            for (j in 2..7) {
                val c = text[i + j]
                if (!c.isLetterOrDigit() || (c.isLetter() && !c.lowercaseChar().let { it in 'a'..'f' })) {
                    isValidHex = false
                    break
                }
            }
            if (isValidHex && i + 8 < text.length && text[i + 8] == '>') {
                result.append(text.substring(i, i + 9))
                i += 9
                continue
            }
        }

        if (i < text.length - 1 && (text[i] == '&' || text[i] == '§')) {
            val code = text[i + 1].lowercaseChar()

            if (LEGACY_CODE_TO_MINIMESSAGE.containsKey(code)) {
                result.append(LEGACY_CODE_TO_MINIMESSAGE[code])
                i += 2
                continue
            }
        }

        result.append(text[i])
        i++
    }

    return result.toString()
}

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