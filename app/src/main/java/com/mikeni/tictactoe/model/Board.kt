package com.mikeni.tictactoe.model

data class Board(
    var move: MutableList<String> = mutableListOf(
        "", "", "",
        "", "", "",
        "", "", ""
    )
    //var _00: String = "", var _01: String = "", var _02: String = "",
    //var _10: String = "", var _11: String = "", var _12: String = "",
    //var _20: String = "", var _21: String = "", var _22: String = ""
)