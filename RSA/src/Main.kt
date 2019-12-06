import java.math.BigInteger
import kotlin.system.measureTimeMillis

const val message = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod...."

const val MEMORY = false

fun main() {
    println("Message: $message (length: ${message.length})")
    var rsa: RSA? = null
    println("Creating open and close keys: ${measureTimeMillis {
        rsa = RSA()
    }.toDouble() / 1000}s")

    var encrypted: BigInteger? = null
    println("Encrypting: ${measureTimeMillis {
        encrypted = rsa!!.encrypt(BigInteger(message.toByteArray()))
    }.toDouble() / 1000}s")
    println("   Encrypted: ${String(encrypted!!.toByteArray())}")

    var decrypted: BigInteger? = null
    println("Decrypting: ${measureTimeMillis {
        decrypted = rsa!!.decrypt(encrypted!!)
    }.toDouble() / 1000}s")
    println("   Decrypted: ${String(decrypted!!.toByteArray())}")
}