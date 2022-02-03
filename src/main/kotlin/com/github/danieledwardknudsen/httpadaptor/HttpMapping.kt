package com.github.danieledwardknudsen.httpadaptor

data class HttpMapping(
    val name: String,
    val requestMapping: RequestMapping,
    val responseMapping: ResponseMapping
)