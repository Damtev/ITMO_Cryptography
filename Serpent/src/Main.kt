import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest

const val USAGE = "<inputFileName> <outputFileName> <key> <option: {e, d}>"
const val ENCRYPT = "e"
const val DECRYPT = "d"

private fun runSerpent(serpent: Serpent, inputName: String, outputName: String, option: String) {
    val inputStream = FileInputStream(inputName)
    inputStream.use { input ->
        do {
            val bytes = input.readNBytes(16)
            if (bytes.isEmpty()) {
                break
            } else {
                val block = ByteArray(16)
                System.arraycopy(bytes, 0, block, 0, bytes.size)
                if (option == ENCRYPT) {
                    serpent.encrypt(block)
                    File(outputName).appendBytes(block)
                } else {
                    serpent.decrypt(block)
                    File(outputName).appendText(String(block))
                }
            }
        } while (true)
    }
}

fun main(args: Array<String>) {
    if (args.size < 4) {
        println("Not enough arguments, usage: $USAGE")
        return
    }
    val inputName = args[0]
    val outputName = args[1]
    val key = args[2]
    val option = args[3]
    val serpent = Serpent()
    serpent.setKey(MessageDigest.getInstance("SHA-512").digest(key.toByteArray()))
    File(outputName).delete()
    when (option) {
        ENCRYPT, DECRYPT -> {
            runSerpent(serpent, inputName, outputName, option)
        }
        else -> {
            println("Unknown option, usage: $USAGE")
        }
    }
}