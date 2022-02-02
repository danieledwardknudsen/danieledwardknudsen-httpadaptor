package com.github.danieledwardknudsen.httpadaptor

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

class RequestMappingTest {
    private val MAPPER = ObjectMapper().registerModule(KotlinModule())
    @Test
    fun `test deserialization`() {
        val raw = """
        {
            "endpoint": "https://google.com/foo",
            "headers": {
                "Foo": "Bar"
            },
            "body": {
                "Bar": "Baz"
            },
            "method": "POST"
        }
        """
        val deserialized = MAPPER.readValue(raw, RequestMapping::class.java)
        Assertions.assertEquals("https://google.com/foo", deserialized.endpoint)
        Assertions.assertEquals(mapOf("Foo" to "Bar"), deserialized.headers)
        Assertions.assertEquals(mapOf("Bar" to "Baz"), deserialized.body)
        Assertions.assertEquals("POST", deserialized.method)
    }
}