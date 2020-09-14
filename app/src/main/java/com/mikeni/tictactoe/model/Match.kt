package com.mikeni.tictactoe.model

data class Match(
    var id: String = "",
    val host: Player = Player(),
    val guest: Player? = null,
    var board: Board = Board(),
    var isPlaying: Boolean = false,
    var isX: Boolean = true,
    var winner: String = ""
)