package cat.freya.khs.config.util

import cat.freya.khs.config.Comment
import cat.freya.khs.config.KhsDeprecated
import cat.freya.khs.config.LocaleString1
import cat.freya.khs.config.LocaleString2
import cat.freya.khs.config.LocaleString3
import cat.freya.khs.config.Omittable
import cat.freya.khs.config.Section
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.memberProperties
import kotlin.reflect.full.primaryConstructor
import kotlin.text.buildString
import org.yaml.snakeyaml.DumperOptions
import org.yaml.snakeyaml.Yaml

fun typeInline(value: Any?): Boolean {
    if (value == null) return true

    return when (value) {
        is List<*> -> value.all { typeInline(it) }
        is Map<*, *> -> value.isEmpty()
        is Boolean -> true
        value::class.isData -> false
        else -> true
    }
}

fun serializeSection(section: Section): String {
    val width = 100
    val prefixWidth = 3
    val headerWidth = section.text.length
    val slugWidth = width - prefixWidth - headerWidth

    return buildString {
        appendLine() // spacing

        // top line
        append("#")
        append(" ".repeat(prefixWidth))
        append("┌")
        append("─".repeat(headerWidth + 2))
        appendLine("┐")

        // bottom line
        append("#")
        append("─".repeat(prefixWidth))
        append("┘ ${section.text} └")
        appendLine("─".repeat(slugWidth))

        appendLine() // spacing
    }
}

fun serializeComment(comment: Comment): String {
    return buildString {
        for (line in comment.text.lines()) {
            appendLine("# $line")
        }
    }
}

fun serializeDeprecated(deprecated: KhsDeprecated): String {
    return "Warning: This field has been DEPRECATED since ${deprecated.since}"
}

fun <T : Any> serializeClass(instance: T): String {
    val type = instance::class
    require(type.isData) { "$type is not a data class" }

    val propValues =
        type.primaryConstructor!!
            .parameters
            .mapNotNull { param -> type.memberProperties.find { it.name == param.name } }
            .associateWith { prop -> prop.getter.call(instance) }

    return buildString {
        for ((prop, value) in propValues) {
            if (value == null && prop.annotations.contains(Omittable())) continue

            val lines = serialize(value).trim().lines().filter { it.isNotEmpty() }

            // append comments
            for (annotation in prop.annotations) {
                when (annotation) {
                    is Section -> append(serializeSection(annotation))
                    is Comment -> append(serializeComment(annotation))
                    is KhsDeprecated -> append(serializeDeprecated(annotation))
                }
            }

            // no content, then skip
            if (lines.isEmpty()) continue

            // no indentation if only a single item
            if (lines.size == 1 && typeInline(value)) {
                appendLine("${prop.name}: ${lines[0]}")
                continue
            }

            appendLine("${prop.name}:")
            for (line in lines) {
                appendLine("  $line")
            }
        }
    }
}

fun <T> serializeList(list: List<T>): String {
    if (list.isEmpty()) return "[]"

    if (list.size == 1 && typeInline(list)) {
        val text = serialize(list[0])
        return "[$text]"
    }

    return buildString {
        for (value in list) {
            val lines = serialize(value).trim().lines().filter { it.isNotEmpty() }
            for ((i, line) in lines.withIndex()) {
                append(if (i == 0) "- " else "  ")
                appendLine(line)
            }
        }
    }
}

fun <K, V> serializeMap(map: Map<K, V>): String {
    if (map.isEmpty()) return "{}"

    return buildString {
        for ((key, value) in map) {
            if (key !is String) error("Map values must be strings")
            val keyString = key.toString()
            val lines = serialize(value).trim().lines().filter { it.isNotEmpty() }

            if (lines.isEmpty()) continue

            if (lines.size == 1 && typeInline(value)) {
                appendLine("$keyString: ${lines[0]}")
                continue
            }

            appendLine("$keyString:")
            for (line in lines) {
                append("  ")
                appendLine(line)
            }
        }
    }
}

fun <T : Any> serializePrimitive(value: T): String {
    val stringYaml =
        Yaml(
            DumperOptions().apply {
                defaultScalarStyle = DumperOptions.ScalarStyle.SINGLE_QUOTED
                splitLines = false
            }
        )
    val yaml = Yaml()
    return when (value) {
        is String -> stringYaml.dump(value)
        is LocaleString1 -> stringYaml.dump(value.inner)
        is LocaleString2 -> stringYaml.dump(value.inner)
        is LocaleString3 -> stringYaml.dump(value.inner)
        is Int -> yaml.dump(value)
        is UInt -> yaml.dump(value.toInt())
        is Long -> yaml.dump(value)
        is ULong -> yaml.dump(value.toLong())
        is Boolean -> yaml.dump(value)
        is Float -> yaml.dump(value)
        is Double -> yaml.dump(value)
        else -> error("cannot serialize '$value'")
    }.trim()
}

fun <T : Any> serialize(value: T?): String {
    if (value == null) return "null"

    val type = value::class
    return when {
        type.isData -> serializeClass(value)
        type.java.isEnum -> value.toString()
        type.isSubclassOf(List::class) -> serializeList(value as List<*>)
        type.isSubclassOf(Map::class) -> serializeMap(value as Map<*, *>)
        else -> serializePrimitive(value)
    }
}
