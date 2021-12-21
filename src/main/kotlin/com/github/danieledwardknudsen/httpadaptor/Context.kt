package com.github.danieledwardknudsen.httpadaptor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.TextNode
import mu.KotlinLogging

private val logger = KotlinLogging.logger { }

private val MAPPER = ObjectMapper()

typealias ContextKey = String

private val escaped = Regex.escape("\${")
private val PLACEHOLDER = "(?<FullPlaceholder>($escaped(?<Key>[A-Za-z_0-9#]{0,100})}))".toPattern()
private const val NESTED_KEY_DELIMITER = "#"

class Context {
    private val context: MutableMap<ContextKey, ContextEntry> = mutableMapOf()

    fun put(key: ContextKey, value: ContextEntry): ContextEntry {
        context[key] = value
        return value
    }

    fun put(key: ContextKey, value: JsonNode): ContextEntry {
        context[key] = ContextEntry(value)
        return context[key]!!
    }

    fun put(key: ContextKey, value: String): ContextEntry {
        context[key] = ContextEntry(TextNode(value))
        return context[key]!!
    }

    fun replacePlaceholders(string: String): String {
        logger.debug { "Replacing placeholders in [$string]." }
        logger.trace { "Current context map: $context" }
        var working = string
        val matcher = PLACEHOLDER.matcher(string)
        val missingKeys = mutableListOf<String>()
        while (matcher.find()) {
            val fullPlaceholder = matcher.group("FullPlaceholder")
            val placeholderKey = matcher.group("Key")
            logger.debug { "Next placeholder to replace: $fullPlaceholder. Resolving key $placeholderKey." }
            val resolved = resolveKey(placeholderKey)
            if (resolved == null) {
                missingKeys.add(placeholderKey)
            } else {
            logger.debug { "Found key $placeholderKey in context. Value is: $resolved." }
               working = working.replace(fullPlaceholder, resolved.resolve())
            }
        }
        if (missingKeys.isNotEmpty()) {
            logger.warn { "Couldn't find $string in context!" }
            throw IllegalArgumentException("Could not replace placeholders with context. The following keys were not found in the current context: $missingKeys")
        }
        logger.debug { "Successfully replaced placeholders. Started with: [$string], ended with [$working]." }
        return working
    }

    private fun resolveKey(key: ContextKey): ContextEntry? {
        val parts = key.split("#")
        val root = context[parts.first()]
        if (root == null) {
            return null
        }
        if (parts.size == 1) {
            return root
        }
        var parent: ContextEntry = root
        parts.subList(1, parts.size).forEach {
            val next = parent.child(it)
            if (next == null) {
                return@resolveKey null
            } else {
                parent = next
            }
        }
        return parent
    }
}

data class ContextEntry(private val value: JsonNode) {

    fun resolve(): String {
        return if (value.fields().hasNext()) {
            MAPPER.writeValueAsString(value)
        } else {
            value.asText()
        }
    }

    fun child(key: String): ContextEntry? = value.findValue(key)?.let { ContextEntry(it) }
}