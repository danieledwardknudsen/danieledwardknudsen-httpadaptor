package com.github.danieledwardknudsen.httpadaptor

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper

import com.fasterxml.jackson.databind.MappingIterator

private val MAPPER = ObjectMapper()

typealias ContextKey = String

val placeholderPattern = "?<EntirePlacheholder>(\$\\{?<Key>A-Za-z0-9\\#+)?".toPattern()

class Context {
    val context: Map<ContextKey, ContextEntry> = mutableMapOf()

    fun replacePlaceholders(string: String): String {
        var working = string
        val matcher = placeholderPattern.matcher(string)
        val missingKeys = mutableListOf<String>()
        while (matcher.find()) {
            val fullPlaceholder = matcher.group("EntirePlaceholder")
            val placeholderKey = matcher.group("Key")
            val context = resolveKey(placeholderKey)
            if (context == null) {
                missingKeys.add(placeholderKey)
            } else {
               working = working.replace(fullPlaceholder, context.resolve())
            }
        }
        if (missingKeys.isNotEmpty()) {
            throw IllegalArgumentException("Could not replace placeholders with context. The following keys were not found in the current context: $missingKeys")
        }
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

class ContextEntry() {
    val children: Map<ContextKey, ContextEntry> = mapOf()
    
    fun resolve(): String {
        return MAPPER.writeValueAsString(children)
    }

    fun child(key: String): ContextEntry? = children[key]
}