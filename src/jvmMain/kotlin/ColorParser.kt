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

                if(modifiers.any { it == "0" } || modifiers.firstOrNull()?.isBlank() == true) {
                    weight = FontWeight.Normal
                    style = FontStyle.Normal
                    decoration = TextDecoration.None
                    foregroundColor = Color.Unspecified
                    backgroundColor = Color.Unspecified
                }

                if(modifiers.size == 3 && modifiers[0] == "38" && modifiers[1] == "5") {
                    //foreground 256 color
                    val color = modifiers[2].toIntOrNull()

                    if(color != null) {
                        foregroundColor = getColor(color)
                    }
                }

                if(modifiers.size == 3 && modifiers[0] == "48" && modifiers[1] == "5") {
                    //foreground 256 color
                    val color = modifiers[2].toIntOrNull()

                    if(color != null) {
                        backgroundColor = getColor(color)
                    }
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

private fun getColor(color: Int): Color {
    if (color < 16) {
        return listOf(
            Color(0, 0, 0),
            Color(128, 0, 0),
            Color(0, 128, 0),
            Color(128, 128, 0),
            Color(0, 0, 128),
            Color(128, 0, 128),
            Color(0, 128, 128),
            Color(192, 192, 192),
            Color(128, 128, 128),
            Color(255, 0, 0),
            Color(0, 255, 0),
            Color(255, 255, 0),
            Color(0, 0, 255),
            Color(255, 0, 255),
            Color(0, 255, 255),
            Color(255, 255, 255)
        )[color]
    } else if (color in 16..231) {
        val index = color - 16
        val colors = listOf(0, 95, 135, 175, 215, 255)

        val b = colors[index % colors.size]
        val g = colors[(index / colors.size) % colors.size]
        val r = colors[(index / (colors.size * colors.size)) % colors.size]

        return Color(r, g, b)
    } else {
        val index = color - 232
        val c = 8 + index * 10
        return Color(c, c, c)
    }
}

fun splitColorMetadata(text: String): List<String> {
    val pattern = Pattern.compile("$ESCAPE_CHAR\\[([0-9]*;?)+m")
    val m = pattern.matcher(text)

    val colors = mutableListOf<String>()
    while(m.find()) {
        colors.add(m.group())
    }

    val splitColors = text.split(Regex("$ESCAPE_CHAR\\[([0-9]*;?)+m"))

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
