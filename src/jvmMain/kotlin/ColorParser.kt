import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import java.util.regex.Pattern

const val ESCAPE_CHAR = "\u001B"

fun parseColor(text: String): AnnotatedString = buildAnnotatedString {
    val interleaved = splitColorMetadata(text.replace("\\e", ESCAPE_CHAR)
        .replace("\\033", ESCAPE_CHAR))

    var foregroundColor = Color.Unspecified
    var backgroundColor = Color.Unspecified
    var style = FontStyle.Normal
    var weight = FontWeight.Normal
    var decoration = TextDecoration.None

    for(part in interleaved) {
        if(part.startsWith(ESCAPE_CHAR)) {
            if(isColor(part)) {
                val modifiers = part.removeSuffix("m").removePrefix("${ESCAPE_CHAR}[").split(";")
                val foregroundColorString = modifiers.firstOrNull { it.startsWith("3") && it.length == 2 && it[1].isDigit() }
                val backgroundColorString = modifiers.firstOrNull { it.startsWith("4") && it.length == 2 && it[1].isDigit() }
                if(foregroundColorString != null) foregroundColor = getColor(foregroundColorString[1])
                if(backgroundColorString != null) backgroundColor = getColor(backgroundColorString[1])

                if(modifiers.any { it == "1" }) weight = FontWeight.Bold
                if(modifiers.any { it == "2" }) style = FontStyle.Italic
                if(modifiers.any { it == "4" }) decoration = TextDecoration.Underline

                if(modifiers.any { it == "22" }) weight = FontWeight.Normal
                if(modifiers.any { it == "23" }) style = FontStyle.Normal
                if(modifiers.any { it == "24" }) decoration = TextDecoration.None

                if(modifiers.any { it == "0" }) {
                    weight = FontWeight.Normal
                    style = FontStyle.Normal
                    decoration = TextDecoration.None
                    foregroundColor = Color.Unspecified
                    backgroundColor = Color.Unspecified
                }
            }
        } else {
            append(part)
            addStyle(SpanStyle(color = foregroundColor,
                background = backgroundColor,
                fontStyle = style, fontWeight = weight, textDecoration = decoration),
                length - part.length, length)
        }
    }
}

fun splitColorMetadata(text: String): List<String> {
    val pattern = Pattern.compile("$ESCAPE_CHAR\\[([0-9]+;?)+m")
    val m = pattern.matcher(text)

    val colors = mutableListOf<String>()
    while(m.find()) {
        colors.add(m.group())
    }

    val splitColors = text.split(Regex("$ESCAPE_CHAR\\[([0-9]+;?)+m"))

    val interleaved = mutableListOf<String>()
    val colorIterator = colors.iterator()
    val splitColorsIterator = splitColors.iterator()
    while(interleaved.size < colors.size + splitColors.size) {
        if(splitColorsIterator.hasNext()) interleaved.add(splitColorsIterator.next())
        if(colorIterator.hasNext()) interleaved.add(colorIterator.next())
    }

    return interleaved
}

private fun isColor(escaped: String): Boolean =
    escaped.startsWith("$ESCAPE_CHAR[") && escaped.last() == 'm'

private fun getColor(code: Char) = when(code) {
    '0' -> Color.Black
    '1' -> Color.Red
    '2' -> Color.Green
    '3' -> Color(150, 75, 0)
    '4' -> Color.Blue
    '5' -> Color(128, 0, 128)
    '6' -> Color.Cyan
    '7' -> Color.White
    else -> Color.Unspecified
}

data class ColorCode(val foreground: Color, val background: Color)
