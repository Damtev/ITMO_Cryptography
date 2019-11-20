private const val key = "yurykamenev"

fun main() {
    var rc4 = RC4(key.toByteArray())
    val message = "Nothing is true, everything is permitted"
    println("Original message: $message")

    val encoded = rc4.encode(message.toByteArray())
    println("Encoded message: ${String(encoded)}")

    rc4 = RC4(key.toByteArray())
    val decoded = rc4.decode(encoded)
    println("Decoded message: ${String(decoded)}")
}