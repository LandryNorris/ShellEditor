import androidx.compose.ui.text.AnnotatedString
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

fun processPS(ps: String): AnnotatedString {
    val hostname = ProcessBuilder("hostname").start().inputReader().readText().trimEnd()
    val dir = File(".").name
    val workingDir = System.getenv("PWD").replace(System.getenv("HOME"), "~")
    val username = ProcessBuilder("whoami").start().inputReader().readText().trimEnd()
    val time = SimpleDateFormat("HH:mm a").format(Date())
    val replacedText = ps.replace("\\u", username)
        .replace("\\h", hostname)
        .replace("\\W", dir).replace("\\w", workingDir)
        .replace("\\@", time)
        .replace("\\\$", if(username == "root") "#" else "$")

    return parseColor(replacedText)
}
