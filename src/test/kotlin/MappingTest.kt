package com.github.danieledwardknudsen.httpadaptor

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

private const val ENDPOINT = "https://google.com/foo"
private const val METHOD = "POST"
private const val REQUEST_HEADER_KEY = "Foo"
private const val REQUEST_HEADER_VALUE = "Bar"
private const val REQUEST_BODY_KEY = "Bob"
private const val REQUEST_BODY_VALUE = "Alice"
private const val REQUEST_MAPPING =
        """
{
    "endpoint": "$ENDPOINT",
    "headers": { "$REQUEST_HEADER_KEY": "$REQUEST_HEADER_VALUE" },
    "body": { "$REQUEST_BODY_KEY": "$REQUEST_BODY_VALUE" },
    "method": "$METHOD"
}
"""

private const val RESPONSE_HEADER_KEY = "Color"
private const val RESPONSE_HEADER_VALUE = "COLOR"
private const val RESPONSE_BODY_KEY = "Number"
private const val RESPONSE_BODY_VALUE = "NUMBER"
private const val RESPONSE_MAPPING =
        """
{
    "headers": { "$RESPONSE_HEADER_KEY": "$RESPONSE_HEADER_VALUE" },
    "body": { "$RESPONSE_BODY_KEY": "$RESPONSE_BODY_VALUE" }
}
"""

private const val NAME = "UNIT"

private const val HTTP_MAPPING =
        """
{
    "name": "$NAME",
    "requestMapping": $REQUEST_MAPPING,
    "responseMapping": $RESPONSE_MAPPING
}
"""

private const val GLOBAL_KEY = "GLOBAL_KEY"
private const val GLOBAL_VALUE = "GLOBAL_VALUE"

private const val INPUT = "INPUT"

private const val OUTPUT_KEY = "OUTPUT_KEY"
private const val OUTPUT_VALUE = "OUTPUT_VALUE"

private const val HTTP_CONFIGURATION =
        """
{
    "globals": { "$GLOBAL_KEY": "$GLOBAL_VALUE" },
    "inputs": ["$INPUT"],
    "requests": [ $HTTP_MAPPING ],
    "output": { "$OUTPUT_KEY": "$OUTPUT_VALUE" }
}

"""

class RequestMappingTest {
    private val MAPPER = ObjectMapper().registerModule(KotlinModule())

    private fun validateRequestMapping(mapping: RequestMapping) {
        assertEquals(METHOD, mapping.method)
        assertEquals(ENDPOINT, mapping.endpoint)
        assertEquals(mapOf(REQUEST_HEADER_KEY to REQUEST_HEADER_VALUE), mapping.headers)
        assertEquals(mapOf(REQUEST_BODY_KEY to REQUEST_BODY_VALUE), mapping.body)
    }

    private fun validateResponseMapping(mapping: ResponseMapping) {
        assertEquals(mapOf(RESPONSE_HEADER_KEY to RESPONSE_HEADER_VALUE), mapping.headers)
        assertEquals(mapOf(RESPONSE_BODY_KEY to RESPONSE_BODY_VALUE), mapping.body)
    }

    @Test
    fun `test valid request mapping`() {
        validateRequestMapping(MAPPER.readValue(REQUEST_MAPPING, RequestMapping::class.java))
    }

    @Test
    fun `test valid response mapping`() {
        validateResponseMapping(MAPPER.readValue(RESPONSE_MAPPING, ResponseMapping::class.java))
    }

    private fun validateHttpMapping(mapping: HttpMapping) {
        validateRequestMapping(mapping.requestMapping)
        validateResponseMapping(mapping.responseMapping)
        assertEquals(NAME, mapping.name)
    }

    @Test
    fun `test valid http mapping`() {
        validateHttpMapping(MAPPER.readValue(HTTP_MAPPING, HttpMapping::class.java))
    }

    @Test
    fun `test valid http configuration`() {
        val config = MAPPER.readValue(HTTP_CONFIGURATION, HttpConfiguration::class.java)
        validateHttpMapping(config.requests.first())
        assertEquals(mapOf(GLOBAL_KEY to GLOBAL_VALUE), config.globals)
        assertEquals(mapOf(OUTPUT_KEY to OUTPUT_VALUE), config.output)
        assertEquals(listOf(INPUT), config.inputs)
    }
}
