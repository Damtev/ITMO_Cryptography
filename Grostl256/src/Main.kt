fun main() {
    val grostl = Grostl()
    val tests = arrayOf("",
        "The quick brown fox jumps over the lazy dog",
        "The quick brown fox jumps over the lazy dog.")
    for (test in tests) {
        grostl.run(test)
        println("$test : ${grostl.outputData.toHexString()}")
    }
}

fun UByteArray.toHexString() = joinToString("") { it.toString(16).padStart(2, '0') }