package com.canvasexercise.tictactoe

data class Game(
  val cellsRowCount: Int = 3,
  var cells: ArrayList<Cell> = arrayListOf(),
  var turn: CheckOption = CheckOption.Circle,
  var isFinished: Boolean = false,
  var winner: CheckOption? = null
)
