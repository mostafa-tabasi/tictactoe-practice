package com.canvasexercise.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.canvasexercise.android.Cell
import com.canvasexercise.android.CheckOption

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TicTacToe()
        }
    }


}

@Preview
@Composable
fun Preview() {
    TicTacToe()
}

@Composable
fun TicTacToe() {
    val cellCount = 3
    val hMargin = 50.dp
    val checkIconMargin = 20.dp
    BoxWithConstraints {
        val density = LocalDensity.current
        val hMarginPx = with(density) { hMargin.toPx() }
        val checkIconMarginPx = with(density) { checkIconMargin.toPx() }
        val pageWidth = with(density) { maxWidth.toPx() - 2 * hMarginPx }
        val vMarginPx = with(density) { (maxHeight.toPx() - pageWidth) / 2 }
        val cellSize = with(pageWidth / cellCount) { Size(this, this) }
        var isCircleTurn by remember { mutableStateOf(true) }
        val cells = remember { mutableStateListOf<Cell>() }
        var winner by remember { mutableStateOf<CheckOption?>(null) }

        /*
        val game by remember { mutableStateOf(Game(cellsRowCount = 3)) }
        for (i in 0 until game.cellsRowCount) for (j in 0 until game.cellsRowCount) {
            game.cells.add(
                Cell(
                    i + 1,
                    j + 1,
                    Rect(
                        offset = Offset(
                            x = hMarginPx + j * cellSize.width,
                            y = vMarginPx + i * cellSize.height
                        ), cellSize
                    )
                )
            )
        }
        */

        for (i in 0 until cellCount) for (j in 0 until cellCount) {
            cells.add(
                Cell(
                    i + 1,
                    j + 1,
                    Rect(
                        offset = Offset(
                            x = hMarginPx + j * cellSize.width,
                            y = vMarginPx + i * cellSize.height
                        ), cellSize
                    )
                )
            )
        }
        Column(Modifier.fillMaxWidth()) {
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(White)
                    .pointerInput(true) {
                        detectTapGestures {
                            if (winner != null) return@detectTapGestures
                            run loop@{
                                cells.forEach { cell ->
                                    if (cell.bound.contains(it) && cell.checkOption == null) {
                                        println("CELL ${cell.row}${cell.column} IS TAPPED")
                                        val cellIndex = cells.indexOf(cell)
                                        val updatedCell =
                                            cell.copy(checkOption = if (isCircleTurn) CheckOption.Circle else CheckOption.Cross)
                                        cells[cellIndex] = updatedCell
                                        isCircleTurn = !isCircleTurn

                                        winner = checkIfGameIsFinished(cells, cellCount)
                                        if (winner != null) println("CELL/ WINNER IS: $winner")

                                        return@loop
                                    }
                                }
                            }
                        }
                    }
            ) {
                drawCells(hMarginPx, vMarginPx, cellSize, cellCount)
                cells.forEach {
                    when (it.checkOption) {
                        CheckOption.Circle -> drawCircle(
                            color = Red,
                            radius = cellSize.minDimension / 2 - checkIconMarginPx,
                            center = it.bound.center,
                            style = Stroke(width = 3.dp.toPx())
                        )
                        CheckOption.Cross -> {
                            drawLine(
                                color = Blue,
                                start = Offset(
                                    it.bound.topLeft.x + checkIconMarginPx,
                                    it.bound.topLeft.y + checkIconMarginPx,
                                ),
                                end = Offset(
                                    it.bound.bottomRight.x - checkIconMarginPx,
                                    it.bound.bottomRight.y - checkIconMarginPx,
                                ),
                                strokeWidth = 3.dp.toPx()
                            )
                            drawLine(
                                color = Blue,
                                start = Offset(
                                    it.bound.bottomLeft.x + checkIconMarginPx,
                                    it.bound.bottomLeft.y - checkIconMarginPx,
                                ),
                                end = Offset(
                                    it.bound.topRight.x - checkIconMarginPx,
                                    it.bound.topRight.y + checkIconMarginPx,
                                ),
                                strokeWidth = 3.dp.toPx()
                            )
                        }
                        null -> {
                        }
                    }
                }
            }
            if (winner != null) Text(
                text = "${winner.toString()} is winner",
                Modifier
                    .fillMaxWidth()
                    .padding(5.dp),
                textAlign = TextAlign.Center,
                style = TextStyle(
                    fontSize = 20.sp, color = if (winner is CheckOption.Circle) Red else Blue
                )
            )
        }
    }

}

fun checkIfGameIsFinished(cells: List<Cell>, cellCount: Int): CheckOption? {
    val leftDiagonal = arrayListOf<Cell>()
    val rightDiagonal = arrayListOf<Cell>()
    for (i in 1..cellCount) {
        val rowCells = arrayListOf<Cell>()
        val columnCells = arrayListOf<Cell>()
        for (j in 1..cellCount) {
            rowCells.add(cells[(i - 1) * cellCount + j - 1])
            columnCells.add(cells[(j - 1) * cellCount + i - 1])
            val diagonalCell = cells[(i - 1) * cellCount + j - 1]
            if (i == j) leftDiagonal.add(diagonalCell)
            if (i + j - 1 == cellCount) rightDiagonal.add(diagonalCell)
        }
        // check row and column
        whoIsWinner(rowCells)?.let { return it }
        whoIsWinner(columnCells)?.let { return it }
    }
    // check diagonals
    whoIsWinner(rightDiagonal)?.let { return it }
    whoIsWinner(leftDiagonal)?.let { return it }
    return null
}

private fun whoIsWinner(cells: List<Cell>): CheckOption? {
    var cellsControl: CheckOption? = null
    cells.forEach {
        if (it.checkOption == null) return null
        if (cellsControl == null) cellsControl = it.checkOption
        if (it.checkOption != cellsControl) return null
    }
    return cellsControl
}

fun DrawScope.drawCells(hMarginPx: Float, vMarginPx: Float, cellSize: Size, cellCount: Int) {
    val strokeWidth = 2.dp
    for (i in 1 until cellCount) {
        drawLine(
            Black,
            start = Offset(hMarginPx, vMarginPx + i * cellSize.height),
            end = Offset(
                hMarginPx + cellCount * cellSize.width,
                vMarginPx + i * cellSize.height
            ),
            strokeWidth = strokeWidth.toPx()
        )
        drawLine(
            Black,
            start = Offset(hMarginPx + i * cellSize.width, vMarginPx),
            end = Offset(
                hMarginPx + i * cellSize.width,
                vMarginPx + cellCount * cellSize.height
            ),
            strokeWidth = strokeWidth.toPx()
        )
    }
}