import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.awtEventOrNull
import androidx.compose.ui.focus.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun ShellEditor(state: ShellState, colors: ShellColors, onEnterPressed: () -> Unit) {
    val focusRequester = remember { FocusRequester() }
    Column(modifier = Modifier
        .fillMaxSize().focusable().focusRequester(focusRequester).clickable (
            interactionSource = remember { MutableInteractionSource() },
            indication = null // To disable the ripple effect
        ) {
            focusRequester.requestFocus()
        }
        .background(colors.background).onKeyEvent {
            val char = it.awtEventOrNull?.keyChar
            if(it.key == Key.DirectionLeft && it.type == KeyEventType.KeyDown) {
                if(state.cursorIndex > 0) state.cursorIndex--
            } else if(it.key == Key.DirectionRight && it.type == KeyEventType.KeyDown) {
                if(state.cursorIndex < state.currentCommand.length) state.cursorIndex++
            } else if(it.key == Key.Backspace && it.type == KeyEventType.KeyDown) {
                if(state.cursorIndex > 0) {
                    state.currentCommand = buildString {
                        append(state.currentCommand)
                        deleteCharAt(--state.cursorIndex)
                    }
                }
            } else if(it.key == Key.Enter && it.type == KeyEventType.KeyDown) {
                onEnterPressed()
            } else if(char != null && char.isPrintable && it.type == KeyEventType.KeyDown) {
                println("Pressed $char")

                state.currentCommand = buildString {
                    append(state.currentCommand)
                    insert(state.cursorIndex, char)
                }
                state.cursorIndex++
            }
            false
        }) {
        LazyColumn {
            items(state.pastCommands) {
                CommandView(it)
            }
            item {
                Prompt(state.ps1, state.currentCommand, true,
                    1000, state.cursorIndex)
            }
        }
    }
}

@Composable
fun CommandView(state: CommandState) {
    Column {
        Prompt(state.prompt, state.command)
        Text(state.output, modifier = Modifier.fillMaxWidth())
    }
}

@Composable
fun Prompt(prompt: String, command: String,
           blinkCursor: Boolean = false, blinkDuration: Int = 1000,
           cursorIndex: Int = -1, colors: ShellColors = darkThemeShellColors) {
    Prompt(buildAnnotatedString { append(prompt) }, command, blinkCursor, blinkDuration,
        cursorIndex, colors)
}

@Composable
fun Prompt(prompt: AnnotatedString, command: String,
           blinkCursor: Boolean = false, blinkDuration: Int = 1000,
           cursorIndex: Int = -1, colors: ShellColors = darkThemeShellColors) {
    val transition = rememberInfiniteTransition()

    val showCursorFloat by transition.animateFloat(
        initialValue = 0f, targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(blinkDuration),
            repeatMode = RepeatMode.Reverse
        )
    )

    val showCursor = showCursorFloat < 0.5f

    Row {
        Text(prompt, color = colors.prompt)

        val annotatedString = buildAnnotatedString {
            append(command)
            append(" ")
            //we need the invisible period, or the styling doesn't show up for ending whitespace
            append(".")
            if((showCursor || !blinkCursor))
                addStyle(
                    SpanStyle(
                        background = colors.cursor,
                        color = colors.behindCursor),
                    start = cursorIndex,
                    end = cursorIndex+1)
            addStyle(SpanStyle(color = Color.Transparent), start = length-1, end = length)
        }
        Text(annotatedString, color = colors.command)
    }
}

val darkThemeShellColors = ShellColors(Color.White, Color.White, Color.White, Color.Black, Color.DarkGray)

data class ShellColors(val prompt: Color, val command: Color,
                       val cursor: Color, val behindCursor: Color,
                       val background: Color)

data class CommandState(val prompt: AnnotatedString, val command: String, val output: String)

class ShellState(ps1: String, pastCommands: List<CommandState>,
                 currentCommand: String, cursorIndex: Int) {
    var ps1 by mutableStateOf(ps1)
    var pastCommands by mutableStateOf(pastCommands)
    var currentCommand by mutableStateOf(currentCommand)
    var cursorIndex by mutableStateOf(cursorIndex)
}

@Composable
fun rememberShellState(ps1: String) = remember {
    ShellState(ps1, listOf(), "", 0)
}

val Char.isPrintable get() = code in 32..126
