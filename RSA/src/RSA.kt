import java.math.BigInteger
import java.util.*

val prime1 = BigInteger("14068170352086685561103034723263937585995417040456831227783808663011153097738765448284588533")
val prime2 = BigInteger("13085757037599427203660600817896963395312879396723446226427815225708616287815136615605343963")

val bigExp: BigInteger = BigInteger.valueOf(65537)
val smallExp: BigInteger = BigInteger.valueOf(257)

class RSA {

    var openKey: Pair<BigInteger, BigInteger>
    var closeKey: Pair<BigInteger, BigInteger>

    init {
        while (true) {
            var p: BigInteger?
            var q: BigInteger?
            if (!MEMORY) {
//                p = BigInteger.probablePrime(1024, Random())!!
//                q = BigInteger.probablePrime(1024, Random())!!
                p = getRandomBigPrime()
                q = getRandomBigPrime()
            } else {
                p = prime1
                q = prime2
            }

            println(p)
            println(q)

            val n = mul(p, q)

            val phi = mul(p - one, q - one)

            val e = if (bigExp >= phi) smallExp else bigExp

            val x = Wrapper(zero)
            val y = Wrapper(zero)
            val g = gcd(e, phi, x, y)
            if (g != one) {
                continue
            } else {
                val d = (x.value % phi + phi) % phi
                openKey = Pair(e, n)
                closeKey = Pair(d, n)
                break
            }
        }
    }

    fun encrypt(m: BigInteger) = pow(m, openKey.first, openKey.second)

    fun decrypt(c: BigInteger) = pow(c, closeKey.first, closeKey.second)
}