import java.math.BigInteger
import kotlin.math.abs
import kotlin.random.Random

const val BITS_SIZE = 304

val zero: BigInteger = BigInteger.ZERO
val one: BigInteger = BigInteger.ONE
val two: BigInteger = BigInteger.TWO
val three: BigInteger = BigInteger.valueOf(3)
val four: BigInteger = BigInteger.valueOf(4)

data class Wrapper(var value: BigInteger)

fun mul(a: BigInteger, b: BigInteger): BigInteger {
    var curA = a
    var curB = b
    var r = zero
    while (curB > zero) {
        if (curB % two == one) {
            r += curA
        }
        curA = curA.shiftLeft(1);
        curB = curB.shiftRight(1);
    }

    return r;
}

fun mul(a: BigInteger, n: BigInteger, m: BigInteger): BigInteger {
    var curN = n
    var curA = a
    var r = zero
    while (curN > zero) {
        if (curN % two == one) {
            r = (r + curA) % m
        }
        curA = (curA + curA) % m;
        curN = curN.shiftRight(1);
    }

    return r
}

fun pow(a: BigInteger, n: BigInteger, m: BigInteger): BigInteger {
    var curN = n
    var curA = a
    var r = one
    while (curN > zero) {
        if (curN.and(one) > zero) {
            r = mul(r, curA, m)
        }
        curA = mul(curA, curA, m)
        curN = curN.shiftRight(1)
    }

    return r
}

fun isPrime(n: BigInteger): Boolean {
    if (n == two || n == three) {
        return true
    }

    if (n < two || n % two == zero) {
        return false
    }

    val nMinusOne = n - one
    val nMinusFour = n - four
    var t = nMinusOne
    var s = 0
    while (t % two == zero) {
        ++s
        t = t.shiftRight(1)
    }

    for (i in 0 until BITS_SIZE / 8) {
        val a = (BigInteger.valueOf(abs(Random.nextLong())) % nMinusFour) + two
        var x = pow(a, t, n)
        if (x == one || x == nMinusOne) {
            continue
        }

        for (j in 0 until s - 1) {
            x = pow(x, two, n)
            if (x == one) {
                return false
            }

            if (x == nMinusOne) {
                break
            }
        }

        if (x != nMinusOne) {
            return false
        }
    }

    return true
}

fun gcd(a: BigInteger, b: BigInteger, x: Wrapper, y: Wrapper): BigInteger {
    if (a == zero) {
        x.value = zero
        y.value = one
        return b
    }

    val x1 = Wrapper(zero)
    val y1 = Wrapper(zero)
    val d = gcd(b % a, a, x1, y1)
    x.value = y1.value - (b / a) * x1.value
    y.value = x1.value

    return d
}

fun getRandomBigPrime(): BigInteger {
    val diff = 1e9.toLong()
    while (true) {
        val randomBig = BigInteger(Random.nextBytes(BITS_SIZE / 8)).abs()
        randomBig.nextProbablePrime()
        val left = (randomBig - BigInteger.valueOf(diff)).abs()
        for (i in 0 until diff * 2) {
            val maybePrime = left + BigInteger.valueOf(i)
            if (isPrime(maybePrime)) {
                return maybePrime
            }
        }
    }
}