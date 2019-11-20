import java.io.BufferedReader
import java.io.File

fun main(args: Array<String>) {
    val bufferedReader: BufferedReader = File("encrypted/${args[0]}.in").bufferedReader()
    val message = bufferedReader.use { it.readText() }
    val kasiski = Kasiski(message, args[0])
    kasiski.test()
}