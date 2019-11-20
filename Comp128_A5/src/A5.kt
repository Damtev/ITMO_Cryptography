@ExperimentalUnsignedTypes
class A5(private val kc: UByteArray) {

    private val r1 = LFSR(19, 8, arrayOf(13, 16, 17, 18))
    private val r2 = LFSR(22, 10, arrayOf(20, 21))
    private val r3 = LFSR(23, 10, arrayOf(7, 20, 21, 22))

    private fun controlShifts(): Int {
        val c1 = r1.getSyncBit()
        val c2 = r2.getSyncBit()
        val c3 = r3.getSyncBit()
        val c = (c1 and c2) or (c1 and c3) or (c2 and c3)

        var out = -1

        if (c == c1) out = if (out == -1) r1.shift() else out xor r1.shift()
        if (c == c2) out = if (out == -1) r2.shift() else out xor r2.shift()
        if (c == c3) out = if (out == -1) r3.shift() else out xor r3.shift()

        return out
    }

    fun init(frame: Int) {
        for (i in 0 until 8) {
            for (j in 0 until 8) {
                val bit = kc[i].getBit(j)
                r1.shiftXor(bit)
                r2.shiftXor(bit)
                r3.shiftXor(bit)
            }
        }

        for (i in 0 until 22) {
            val bit = frame.getBit(i)
            r1.shiftXor(bit)
            r2.shiftXor(bit)
            r3.shiftXor(bit)
        }

        repeat(100) {
            controlShifts()
        }
    }

    private fun getKey(): UByteArray {
        val arr = UByteArray(15)
        for (i in 0 until 114) {
            arr[i / 8] = arr[i / 8].setBit(i % 8, controlShifts())
        }
        return arr
    }

    fun encodeAndDecode(): Pair<UByteArray, UByteArray> {
        return Pair(getKey(), getKey())
    }

    private class LFSR(private val length: Int, private val syncBit: Int, private val feedbackBits: Array<Int>) {

        private var reg = 0UL

        fun getSyncBit() = reg.getBit(syncBit)

        fun shiftWith(bit: Int): Int {
            reg = (reg shl 1) or (bit and 0x1).toULong()
            return reg.getBit(length)
        }

        fun shiftXor(x: Int): Int {
            var bit = -1
            feedbackBits.forEach {
                bit = if (bit == -1) {
                    reg.getBit(it)
                } else {
                    (bit xor reg.getBit(it)) and 0x1
                }
            }
            return shiftWith(bit xor x)
        }

        fun shift(): Int {
            var bit = -1
            feedbackBits.forEach {
                bit = if (bit == -1) {
                    reg.getBit(it)
                } else {
                    (bit xor reg.getBit(it)) and 0x1
                }
            }
            return shiftWith(bit)
        }
    }
}

@ExperimentalUnsignedTypes
fun UByte.setBit(i: Int, v: Int): UByte {
    return if (v == 1) this or (1 shl i).toUByte() else ((this or (1 shl i).toUByte()) xor (1 shl i).toUByte())
}

@ExperimentalUnsignedTypes
fun ULong.getBit(i: Int): Int {
    return ((this shr i) and 1UL).toInt()
}

@ExperimentalUnsignedTypes
fun UByte.getBit(i: Int): Int {
    return ((this.toUInt() shr i) and 1U).toInt()
}

fun Int.getBit(i: Int): Int {
    return (this shr i) and 1
}