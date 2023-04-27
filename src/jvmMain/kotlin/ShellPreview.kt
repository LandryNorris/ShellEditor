import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color

@Preview
@Composable
fun PromptPreview() {
    Box(modifier = Modifier.background(Color.DarkGray)) {
        Prompt("(landry) ~ ",
            "some command I'm typing",
            cursorIndex = 7)
    }
}

@Preview
@Composable
fun PromptPreviewWithoutCursor() {
    Box(modifier = Modifier.background(Color.DarkGray)) {
        Prompt("(landry) ~ ",
            "some command I'm typing",
            cursorIndex = -1)
    }
}

@Preview
@Composable
fun BlinkingCursorPrompt() {
    Box(modifier = Modifier.background(Color.DarkGray)) {
        Prompt("(landry) ~ ",
            "some command I'm typing",
            cursorIndex = 7, blinkCursor = true)
    }
}
