package com.canvasexercise.tictactoe

sealed class CheckOption {

  object Circle : CheckOption() {
    override fun toString(): String = "Circle"
  }

  object Cross : CheckOption() {
    override fun toString(): String = "Cross"
  }

  abstract override fun toString(): String
}
