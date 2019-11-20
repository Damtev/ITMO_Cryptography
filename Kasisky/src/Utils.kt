fun findGcd(numbers: List<Int>): Int {
    var gcd = numbers.last()
    for (i in numbers) {
        gcd = gcd(i, gcd)
    }
    return gcd
}

fun gcd(a: Int, b: Int): Int {
    var first = a
    var second = b
    var c: Int
    while (first != 0) {
        c = first
        first = second % first
        second = c
    }
    return second
}