package com.github.danieledwardknudsen.httpadaptor

import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import java.util.stream.Stream
import com.fasterxml.jackson.databind.node.TextNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.JsonNode

private val MAPPER = ObjectMapper()

private const val SIMPLE_VALUE_KEY = "SIMPLE_TEST_KEY"
private const val SIMPLE_VALUE_VALUE = "SIMPLE_TEST_VALUE"
private const val ANOTHER_SIMPLE_VALUE_KEY = "ANOTHER_SIMPLE_TEST_KEY"
private const val ANOTHER_SIMPLE_VALUE_VALUE = "ANOTHER_SIMPLE_TEST_VALUE"

private val SIMPLE_CONTEXT_INPUTS = mapOf(
    SIMPLE_VALUE_KEY to SIMPLE_VALUE_VALUE,
    ANOTHER_SIMPLE_VALUE_KEY to ANOTHER_SIMPLE_VALUE_VALUE
)

private const val COMPLEX_VALUE_KEY = "COMPLEX_VALUE"
private const val COMPLEX_VALUE_MEMBER_KEY = "MEMBER"
private const val ANOTHER_COMPLEX_VALUE_MEMBER_KEY = "ANOTHER_MEMBER"
private const val COMPLEX_VALUE_MEMBER_VALUE = "COMPLEX_VALUE_MEMBER"
private const val ANOTHER_COMPLEX_VALUE_MEMBER_VALUE = "COMPLEX_VALUE_ANOTHER_MEMBER"
private val COMPLEX_VALUE_VALUE: JsonNode = mapOf(
    COMPLEX_VALUE_MEMBER_KEY to COMPLEX_VALUE_MEMBER_VALUE,
    ANOTHER_COMPLEX_VALUE_MEMBER_KEY to ANOTHER_COMPLEX_VALUE_MEMBER_VALUE,
).let { MAPPER.valueToTree(it) }

class ContextTest {

    private companion object {
        @JvmStatic
        fun createContext() = Stream.of(
                Arguments.of(Context().apply { 
                    SIMPLE_CONTEXT_INPUTS.forEach{
                        put(it.key, it.value)
                    }
                 }),
                 Arguments.of(Context().apply { 
                    SIMPLE_CONTEXT_INPUTS.forEach{
                        put(it.key, TextNode(it.value))
                    }
                 }),
                 Arguments.of(Context().apply { 
                    SIMPLE_CONTEXT_INPUTS.forEach{
                        put(it.key, ContextEntry(TextNode(it.value)))
                    }
                 })
            )
    }

    @ParameterizedTest
    @MethodSource("createContext")
    fun `test simple key`(ctx: Context) {
        val result = ctx.replacePlaceholders("some padding \${$SIMPLE_VALUE_KEY} more padding.")
        assertEquals("some padding $SIMPLE_VALUE_VALUE more padding.", result)
    }

    @ParameterizedTest
    @MethodSource("createContext")
    fun `test duplicated placeholder key`(ctx: Context) {
        val result = ctx.replacePlaceholders("some padding \${$SIMPLE_VALUE_KEY} more padding \${$SIMPLE_VALUE_KEY} more padding.")
        assertEquals("some padding $SIMPLE_VALUE_VALUE more padding $SIMPLE_VALUE_VALUE more padding.", result)
    }

    @ParameterizedTest
    @MethodSource("createContext")
    fun `test different placeholders`(ctx: Context) {
        val result = ctx.replacePlaceholders("some padding \${$SIMPLE_VALUE_KEY} more padding \${$ANOTHER_SIMPLE_VALUE_KEY} more padding.")
        assertEquals("some padding $SIMPLE_VALUE_VALUE more padding $ANOTHER_SIMPLE_VALUE_VALUE more padding.", result)
    }

    @Test
    fun `test complex placeholders are replaced via JSON`() {
        val ctx = Context()
        ctx.put(COMPLEX_VALUE_KEY, COMPLEX_VALUE_VALUE)
        val result = ctx.replacePlaceholders("some padding \${$COMPLEX_VALUE_KEY} more padding.")
        assertEquals("some padding {\"$COMPLEX_VALUE_MEMBER_KEY\":\"$COMPLEX_VALUE_MEMBER_VALUE\",\"$ANOTHER_COMPLEX_VALUE_MEMBER_KEY\":\"$ANOTHER_COMPLEX_VALUE_MEMBER_VALUE\"} more padding.", result)
    }

    @Test
    fun `test complex placeholder members are replaced individually`() {
        val ctx = Context()
        ctx.put(COMPLEX_VALUE_KEY, COMPLEX_VALUE_VALUE)
        val result = ctx.replacePlaceholders("some padding \${$COMPLEX_VALUE_KEY#$COMPLEX_VALUE_MEMBER_KEY} more padding \${$COMPLEX_VALUE_KEY#$ANOTHER_COMPLEX_VALUE_MEMBER_KEY} more padding.")
        assertEquals("some padding $COMPLEX_VALUE_MEMBER_VALUE more padding $ANOTHER_COMPLEX_VALUE_MEMBER_VALUE more padding.", result)
    }
}
