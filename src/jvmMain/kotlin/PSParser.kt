import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun processPS(ps: String): AnnotatedString {
    val processed = ProcessBuilder().command("bash", "-c", "printf \"%s\\n\" \"\${PROMPT@P}\"")
    processed.environment()["PROMPT"] = ps

    val process = processed.start()
    val output = process.inputReader().readText()

    return parseColor(output)
}
