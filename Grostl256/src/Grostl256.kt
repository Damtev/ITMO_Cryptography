import java.nio.ByteBuffer


const val BUFFER_SIZE = 64
const val l = BUFFER_SIZE * 8

class Grostl {

    private val IV = ubyteArrayOf(
        0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
        0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
        0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
        0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
        0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
        0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
        0u, 0u, 0u, 0u, 0u, 0u, 0u, 0u,
        0u, 0u, 0u, 0u, 0u, 0u, 1u, 0u
    )

    private val sBox = intArrayOf(
        0x63,
        0x7c,
        0x77,
        0x7b,
        0xf2,
        0x6b,
        0x6f,
        0xc5,
        0x30,
        0x01,
        0x67,
        0x2b,
        0xfe,
        0xd7,
        0xab,
        0x76,
        0xca,
        0x82,
        0xc9,
        0x7d,
        0xfa,
        0x59,
        0x47,
        0xf0,
        0xad,
        0xd4,
        0xa2,
        0xaf,
        0x9c,
        0xa4,
        0x72,
        0xc0,
        0xb7,
        0xfd,
        0x93,
        0x26,
        0x36,
        0x3f,
        0xf7,
        0xcc,
        0x34,
        0xa5,
        0xe5,
        0xf1,
        0x71,
        0xd8,
        0x31,
        0x15,
        0x04,
        0xc7,
        0x23,
        0xc3,
        0x18,
        0x96,
        0x05,
        0x9a,
        0x07,
        0x12,
        0x80,
        0xe2,
        0xeb,
        0x27,
        0xb2,
        0x75,
        0x09,
        0x83,
        0x2c,
        0x1a,
        0x1b,
        0x6e,
        0x5a,
        0xa0,
        0x52,
        0x3b,
        0xd6,
        0xb3,
        0x29,
        0xe3,
        0x2f,
        0x84,
        0x53,
        0xd1,
        0x00,
        0xed,
        0x20,
        0xfc,
        0xb1,
        0x5b,
        0x6a,
        0xcb,
        0xbe,
        0x39,
        0x4a,
        0x4c,
        0x58,
        0xcf,
        0xd0,
        0xef,
        0xaa,
        0xfb,
        0x43,
        0x4d,
        0x33,
        0x85,
        0x45,
        0xf9,
        0x02,
        0x7f,
        0x50,
        0x3c,
        0x9f,
        0xa8,
        0x51,
        0xa3,
        0x40,
        0x8f,
        0x92,
        0x9d,
        0x38,
        0xf5,
        0xbc,
        0xb6,
        0xda,
        0x21,
        0x10,
        0xff,
        0xf3,
        0xd2,
        0xcd,
        0x0c,
        0x13,
        0xec,
        0x5f,
        0x97,
        0x44,
        0x17,
        0xc4,
        0xa7,
        0x7e,
        0x3d,
        0x64,
        0x5d,
        0x19,
        0x73,
        0x60,
        0x81,
        0x4f,
        0xdc,
        0x22,
        0x2a,
        0x90,
        0x88,
        0x46,
        0xee,
        0xb8,
        0x14,
        0xde,
        0x5e,
        0x0b,
        0xdb,
        0xe0,
        0x32,
        0x3a,
        0x0a,
        0x49,
        0x06,
        0x24,
        0x5c,
        0xc2,
        0xd3,
        0xac,
        0x62,
        0x91,
        0x95,
        0xe4,
        0x79,
        0xe7,
        0xc8,
        0x37,
        0x6d,
        0x8d,
        0xd5,
        0x4e,
        0xa9,
        0x6c,
        0x56,
        0xf4,
        0xea,
        0x65,
        0x7a,
        0xae,
        0x08,
        0xba,
        0x78,
        0x25,
        0x2e,
        0x1c,
        0xa6,
        0xb4,
        0xc6,
        0xe8,
        0xdd,
        0x74,
        0x1f,
        0x4b,
        0xbd,
        0x8b,
        0x8a,
        0x70,
        0x3e,
        0xb5,
        0x66,
        0x48,
        0x03,
        0xf6,
        0x0e,
        0x61,
        0x35,
        0x57,
        0xb9,
        0x86,
        0xc1,
        0x1d,
        0x9e,
        0xe1,
        0xf8,
        0x98,
        0x11,
        0x69,
        0xd9,
        0x8e,
        0x94,
        0x9b,
        0x1e,
        0x87,
        0xe9,
        0xce,
        0x55,
        0x28,
        0xdf,
        0x8c,
        0xa1,
        0x89,
        0x0d,
        0xbf,
        0xe6,
        0x42,
        0x68,
        0x41,
        0x99,
        0x2d,
        0x0f,
        0xb0,
        0x54,
        0xbb,
        0x16
    ).map { int -> int.toUByte() }.toUByteArray()

    var outputData = ubyteArrayOf()

    fun run(s: String) {
        var t = 0uL
        var T: ULong
        var N = 0uL
        var w: ULong

        var h = IV.copyOf()
        val reader = s.byteInputStream()
        reader.use {
            var buffer: UByteArray
            do {
                buffer = reader.readNBytes(BUFFER_SIZE).toUByteArray()
                if (buffer.size == BUFFER_SIZE) {
                    N += (buffer.size * 8).toUInt()
                    h = compression(h, buffer)
                    t++
                }
            } while (buffer.size == BUFFER_SIZE)
            w = ((((-N.toLong() - 65) % l) + l) % l).toULong()
            T = (N + w + 65u) / l.toUInt()

            val blocksLeft = (T - t).toInt()
            var newBuffer = UByteArray(BUFFER_SIZE) { i -> if (i < buffer.size) buffer[i] else 0u }
            newBuffer[buffer.size] = 0x80.toUByte()
            for (i in 0 until blocksLeft) {
                if (i == blocksLeft - 1) {
                    val bytes = ByteBuffer.allocate(ULong.SIZE_BYTES).putLong(T.toLong()).array().toUByteArray()
                    bytes.copyTo(0, newBuffer, BUFFER_SIZE - 8, 8)
                }
                h = compression(h, newBuffer)
                newBuffer = UByteArray(BUFFER_SIZE)
            }
            h = truncation(xor(P(h), h))
            outputData = h
        }
    }

    private fun compression(h: UByteArray, m: UByteArray): UByteArray {
        return xor(xor(P(xor(h, m)), Q(m)), h)
    }

    private fun xor(op1: UByteArray, op2: UByteArray): UByteArray {
        val result = UByteArray(op1.size)
        for (i in result.indices) {
            result[i] = (op1[i] xor op2[i])
        }
        return result
    }

    private fun P(input: UByteArray): UByteArray {
        val R = 10
        val state: Array<UByteArray> = bytesToMatrix(input)
        for (r in 0 until R) {
            addRoundConstant(state, r.toUByte(), 0.toUByte())
            subBytes(state)
            shiftBytes(state, 0.toUByte())
            mixBytes(state)
        }
        return matrixToBytes(state)
    }

    private fun Q(input: UByteArray): UByteArray {
        val R = 10
        val state: Array<UByteArray> = bytesToMatrix(input)
        for (r in 0 until R) {
            addRoundConstant(state, r.toUByte(), 1.toUByte())
            subBytes(state)
            shiftBytes(state, 1.toUByte())
            mixBytes(state)
        }
        return matrixToBytes(state)
    }

    private fun truncation(x: UByteArray): UByteArray {
        val nBytes = 32
        val result = UByteArray(nBytes)
        x.copyTo(x.size - nBytes, result, 0, nBytes)
        return result
    }

    private fun bytesToMatrix(input: UByteArray): Array<UByteArray> {
        val nColumns = 8
        var k = 0
        val result = arrayOf(
            UByteArray(nColumns),
            UByteArray(nColumns),
            UByteArray(nColumns),
            UByteArray(nColumns),
            UByteArray(nColumns),
            UByteArray(nColumns),
            UByteArray(nColumns),
            UByteArray(nColumns)
        )
        for (j in 0 until nColumns) {
            for (i in 0..7) {
                result[i][j] = input[k]
                k++
            }
        }
        return result
    }

    private fun matrixToBytes(input: Array<UByteArray>): UByteArray {
        val nBytes = 64
        var k = 0
        val result = UByteArray(nBytes)
        for (j in 0 until nBytes / 8) {
            for (i in 0..7) {
                result[k] = input[i][j]
                k++
            }
        }
        return result
    }

    private fun addRoundConstant(
        state: Array<UByteArray>,
        r: UByte,
        permutation: UByte
    ) {
        val nColumns = 8
        //permutation == 0 -> P ^ permutation == 1 -> Q
        if (permutation.toInt() == 0) {
            val C = byteArrayOf(
                (0x00 xor r.toInt()).toByte(), (0x10 xor r.toInt()).toByte(), (0x20 xor r.toInt()).toByte(),
                (0x30 xor r.toInt()).toByte(), (0x40 xor r.toInt()).toByte(), (0x50 xor r.toInt()).toByte(),
                (0x60 xor r.toInt()).toByte(), (0x70 xor r.toInt()).toByte(), (0x80 xor r.toInt()).toByte(),
                (0x90 xor r.toInt()).toByte(), (0xa0 xor r.toInt()).toByte(), (0xb0 xor r.toInt()).toByte(),
                (0xc0 xor r.toInt()).toByte(), (0xd0 xor r.toInt()).toByte(), (0xe0 xor r.toInt()).toByte(),
                (0xf0 xor r.toInt()).toByte()
            ).toUByteArray()
            for (j in 0 until nColumns) {
                state[0][j] = state[0][j] xor C[j]
            }
        } else {
            val C = byteArrayOf(
                (0xff xor r.toInt()).toByte(), (0xef xor r.toInt()).toByte(), (0xdf xor r.toInt()).toByte(),
                (0xcf xor r.toInt()).toByte(), (0xbf xor r.toInt()).toByte(), (0xaf xor r.toInt()).toByte(),
                (0x9f xor r.toInt()).toByte(), (0x8f xor r.toInt()).toByte(), (0x7f xor r.toInt()).toByte(),
                (0x6f xor r.toInt()).toByte(), (0x5f xor r.toInt()).toByte(), (0x4f xor r.toInt()).toByte(),
                (0x3f xor r.toInt()).toByte(), (0x2f xor r.toInt()).toByte(), (0x1f xor r.toInt()).toByte(),
                (0x0f xor r.toInt()).toByte()
            ).toUByteArray()
            for (i in 0..6) {
                for (j in 0 until nColumns) {
                    state[i][j] = state[i][j] xor 0xff.toUByte()
                }
            }
            for (j in 0 until nColumns) {
                state[7][j] = state[7][j] xor C[j]
            }
        }
    }

    private fun subBytes(state: Array<UByteArray>) {
        val nColumns = 8
        for (i in 0..7) {
            for (j in 0 until nColumns) {
                state[i][j] = sBox[state[i][j].toInt()]
            }
        }
    }

    private fun shiftBytes(
        state: Array<UByteArray>,
        permutation: UByte
    ) { //permutation == 0 -> P ^ permutation == 1 -> Q
        val sigma =
            if (permutation.toInt() == 0) byteArrayOf(0, 1, 2, 3, 4, 5, 6, 7) else byteArrayOf(1, 3, 5, 7, 0, 2, 4, 6)
        for (i in 0..7) {
            state[i] = shiftRow(state[i], sigma[i].toInt())
        }
    }

    private fun shiftRow(row: UByteArray, shift: Int): UByteArray {
        val newRow = UByteArray(row.size)
        row.copyTo(shift, newRow, 0, row.size - shift)
        row.copyTo(0, newRow, row.size - shift, shift)
        return newRow
    }

    private fun mixBytes(state: Array<UByteArray>) {
        val nColumns = 8
        val x = UByteArray(8)
        val y = UByteArray(8)
        val z = UByteArray(8)
        for (j in 0 until nColumns) {
            x[0] = (state[0][j] xor state[(0 + 1) % 8][j])
            x[1] = (state[1][j] xor state[(1 + 1) % 8][j])
            x[2] = (state[2][j] xor state[(2 + 1) % 8][j])
            x[3] = (state[3][j] xor state[(3 + 1) % 8][j])
            x[4] = (state[4][j] xor state[(4 + 1) % 8][j])
            x[5] = (state[5][j] xor state[(5 + 1) % 8][j])
            x[6] = (state[6][j] xor state[(6 + 1) % 8][j])
            x[7] = (state[7][j] xor state[(7 + 1) % 8][j])

            y[0] = (x[0] xor x[(0 + 3) % 8])
            y[1] = (x[1] xor x[(1 + 3) % 8])
            y[2] = (x[2] xor x[(2 + 3) % 8])
            y[3] = (x[3] xor x[(3 + 3) % 8])
            y[4] = (x[4] xor x[(4 + 3) % 8])
            y[5] = (x[5] xor x[(5 + 3) % 8])
            y[6] = (x[6] xor x[(6 + 3) % 8])
            y[7] = (x[7] xor x[(7 + 3) % 8])

            z[0] = (x[0] xor x[(0 + 2) % 8] xor state[(0 + 6) % 8][j])
            z[1] = (x[1] xor x[(1 + 2) % 8] xor state[(1 + 6) % 8][j])
            z[2] = (x[2] xor x[(2 + 2) % 8] xor state[(2 + 6) % 8][j])
            z[3] = (x[3] xor x[(3 + 2) % 8] xor state[(3 + 6) % 8][j])
            z[4] = (x[4] xor x[(4 + 2) % 8] xor state[(4 + 6) % 8][j])
            z[5] = (x[5] xor x[(5 + 2) % 8] xor state[(5 + 6) % 8][j])
            z[6] = (x[6] xor x[(6 + 2) % 8] xor state[(6 + 6) % 8][j])
            z[7] = (x[7] xor x[(7 + 2) % 8] xor state[(7 + 6) % 8][j])

            state[0][j] =
                (doubling((doubling(y[(0 + 3) % 8]) xor z[(0 + 7) % 8])) xor z[(0 + 4) % 8])
            state[1][j] =
                (doubling((doubling(y[(1 + 3) % 8]) xor z[(1 + 7) % 8])) xor z[(1 + 4) % 8])
            state[2][j] =
                (doubling((doubling(y[(2 + 3) % 8]) xor z[(2 + 7) % 8])) xor z[(2 + 4) % 8])
            state[3][j] =
                (doubling((doubling(y[(3 + 3) % 8]) xor z[(3 + 7) % 8])) xor z[(3 + 4) % 8])
            state[4][j] =
                (doubling((doubling(y[(4 + 3) % 8]) xor z[(4 + 7) % 8])) xor z[(4 + 4) % 8])
            state[5][j] =
                (doubling((doubling(y[(5 + 3) % 8]) xor z[(5 + 7) % 8])) xor z[(5 + 4) % 8])
            state[6][j] =
                (doubling((doubling(y[(6 + 3) % 8]) xor z[(6 + 7) % 8])) xor z[(6 + 4) % 8])
            state[7][j] =
                (doubling((doubling(y[(7 + 3) % 8]) xor z[(7 + 7) % 8])) xor z[(7 + 4) % 8])
        }
    }

    private fun doubling(x: UByte): UByte {
        val int = if (x.toInt() and 0x80 == 0x80) (x.toInt() shl 1) xor 0x1b else x.toInt() shl 1
        return int.toUByte()
    }
}

fun UByteArray.copyOf() = UByteArray(this.size) { i -> this[i] }

fun ByteArray.toUByteArray() = this.map { byte -> byte.toUByte() }.toUByteArray()

fun UByteArray.copyTo(srcPos: Int, dest: UByteArray, destPos: Int, length: Int) {
    for (i in 0 until length) {
        dest[destPos + i] = this[srcPos + i]
    }
}