package com.canvasexercise.tictactoe

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color.Companion.Black
import androidx.compose.ui.graphics.Color.Companion.Blue
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
  val checkIconMargin = 10.dp
  val checkIconWidth = 5.dp
  BoxWithConstraints {
    val density = LocalDensity.current
    val hMarginPx = with(density) { hMargin.toPx() }
    val checkIconMarginPx = with(density) { checkIconMargin.toPx() }
    val checkIconWidthPx = with(density) { checkIconWidth.toPx() }
    val pageWidth = with(density) { maxWidth.toPx() - 2 * hMarginPx }
    val vMarginPx = with(density) { (maxHeight.toPx() - pageWidth) / 2 }
    val cellSize = with(pageWidth / cellCount) { Size(this, this) }
    var resetTrigger by remember { mutableStateOf(0) }
    var isCircleTurn by remember(resetTrigger) { mutableStateOf(true) }
    val cells = remember(resetTrigger) { mutableListOf<Cell>() }
    var winner by remember(resetTrigger) { mutableStateOf<CheckOption?>(null) }
    var gameIsTied by remember(resetTrigger) { mutableStateOf(false) }

    var currentPlayingCellIndex by remember(resetTrigger) { mutableStateOf(-1) }
    val pathPortion = remember(currentPlayingCellIndex) { Animatable(initialValue = 0f) }
    LaunchedEffect(key1 = currentPlayingCellIndex) {
      pathPortion.animateTo(1f, animationSpec = tween(500))
    }

    LaunchedEffect(key1 = resetTrigger) {
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
    }
    Column(Modifier.fillMaxWidth()) {
      Canvas(
        modifier = Modifier
          .fillMaxWidth()
          .weight(1f)
          .background(White)
          .pointerInput(resetTrigger) {
            detectTapGestures {
              // if game has a winner, it isn't playable anymore
              if (winner != null) return@detectTapGestures
              run loop@{
                cells.forEach { cell ->
                  // Check if the touch position belongs to a specific cell and has not been played before
                  if (cell.bound.contains(it) && cell.checkOption == null) {
                    // update current playing cell with checked option
                    val cellIndex = cells.indexOf(cell)
                    cells[cellIndex].checkOption =
                      if (isCircleTurn) CheckOption.Circle else CheckOption.Cross
                    // set the currently played cell index
                    currentPlayingCellIndex = cellIndex
                    // change the turn
                    isCircleTurn = !isCircleTurn
                    // check if we have a winner
                    winner = checkIfGameIsFinished(cells, cellCount)
                    // Check if the game has been tied or not
                    if (winner == null && isGameTied(cells)) gameIsTied = true
                    return@loop
                  }
                }
              }
            }
          }
      ) {
        drawCells(hMarginPx, vMarginPx, cellSize, cellCount)

        cells.forEachIndexed { index, cell ->
          when (cell.checkOption) {
            CheckOption.Circle -> {
              val path = Path().apply { addOval(cell.bound.deflate(checkIconMarginPx)) }
              // if this cell is the currently playing cell, draw it with animation
              if (currentPlayingCellIndex == index) {
                val outPath = Path()
                PathMeasure().apply {
                  setPath(path, false)
                  getSegment(0f, pathPortion.value * length, outPath)
                }
                drawPath(outPath, color = Red, style = Stroke(width = checkIconWidthPx))
              }
              // else draw it without animation
              else drawPath(path, color = Red, style = Stroke(width = checkIconWidthPx))
            }
            CheckOption.Cross -> {
              val firstLinePath = Path().apply {
                moveTo(
                  cell.bound.topLeft.x + checkIconMarginPx,
                  cell.bound.topLeft.y + checkIconMarginPx
                )
                lineTo(
                  cell.bound.bottomRight.x - checkIconMarginPx,
                  cell.bound.bottomRight.y - checkIconMarginPx,
                )
              }
              val secondLinePath = Path().apply {
                moveTo(
                  cell.bound.bottomLeft.x + checkIconMarginPx,
                  cell.bound.bottomLeft.y - checkIconMarginPx,
                )
                lineTo(
                  cell.bound.topRight.x - checkIconMarginPx,
                  cell.bound.topRight.y + checkIconMarginPx,
                )
              }
              // if this cell is the currently playing cell, draw it with animation
              if (currentPlayingCellIndex == index) {
                val outPath1 = Path()
                PathMeasure().apply {
                  setPath(firstLinePath, false)
                  getSegment(0f, pathPortion.value * length, outPath1)
                }
                drawPath(outPath1, color = Blue, style = Stroke(width = checkIconWidthPx))
                val outPath2 = Path()
                PathMeasure().apply {
                  setPath(secondLinePath, false)
                  getSegment(0f, pathPortion.value * length, outPath2)
                }
                drawPath(outPath2, color = Blue, style = Stroke(width = checkIconWidthPx))
              }
              // else draw it without animation
              else {
                drawPath(firstLinePath, color = Blue, style = Stroke(width = checkIconWidthPx))
                drawPath(secondLinePath, color = Blue, style = Stroke(width = checkIconWidthPx))
              }
            }
            null -> {
            }
          }
        }
      }
      if (winner != null || gameIsTied) {
        Row(
          Modifier
            .fillMaxWidth()
            .padding(vertical = 30.dp),
          horizontalArrangement = Arrangement.Center,
          verticalAlignment = Alignment.CenterVertically
        ) {
          IconButton(
            onClick = { resetTrigger++ },
            Modifier.padding(horizontal = 10.dp)
          ) {
            Icon(Icons.Rounded.Refresh, contentDescription = null)
          }
          Text(
            text = if (gameIsTied) "Game Tied" else "${winner.toString()} is winner",
            textAlign = TextAlign.Center,
            style = TextStyle(
              fontSize = 20.sp,
              color = when {
                gameIsTied -> Black
                winner is CheckOption.Circle -> Red
                else -> Blue
              },
              fontWeight = FontWeight.Bold
            )
          )
        }
      }
    }
  }
}

/**
 * check if the game is tied or not
 *
 * if all items in the cell list have any check option but no one has been won, the game is tied
 */
fun isGameTied(cells: List<Cell>): Boolean {
  cells.forEach { if (it.checkOption == null) return false }
  return true
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