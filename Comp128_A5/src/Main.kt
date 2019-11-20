import kotlin.random.Random

fun getRandom16Bytes() = ByteArray(16) {
    Random.nextInt(256).toByte()
}

@ExperimentalUnsignedTypes
fun main() {
    val rand = getRandom16Bytes()
    val ki = getRandom16Bytes()
    val comp128 = Comp128(rand, ki)
    val sres = comp128.a3()
    val kc = comp128.a8()

    println("sres: $sres")
    println("kc: $kc\n")

    val a5 = A5(kc)
    for (i in 0 until 5) {
        a5.init(i)
        val (encode, decode) = a5.encodeAndDecode()
        println("$i: $encode ||| $decode")
    }
}