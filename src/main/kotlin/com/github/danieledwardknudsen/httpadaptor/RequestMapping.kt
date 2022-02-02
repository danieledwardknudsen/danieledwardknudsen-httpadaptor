package com.github.danieledwardknudsen.httpadaptor

data class RequestMapping(val endpoint: String, val method: String, val headers: Map<String, String>, val body: Map<String, String>)