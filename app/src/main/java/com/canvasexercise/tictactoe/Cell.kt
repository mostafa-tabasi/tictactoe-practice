package com.canvasexercise.tictactoe

import androidx.compose.ui.geometry.Rect

data class Cell(
  val row: Int,
  val column: Int,
  val bound: Rect,
  var checkOption: CheckOption? = null,
)