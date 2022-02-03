package com.github.danieledwardknudsen.httpadaptor

data class HttpConfiguration(
    val globals: Map<String, String>,
    val inputs: List<String>,
    val requests: List<HttpMapping>,
    val output: Map<String, String>
)