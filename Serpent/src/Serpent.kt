import java.io.File
import java.nio.ByteBuffer
import kotlin.experimental.and
import kotlin.experimental.xor

class Serpent {

    private var key: ByteArray? = null
    private var prekeys: IntArray = IntArray(140)

    private val s0 = byteArrayOf(3, 8, 15, 1, 10, 6, 5, 11, 14, 13, 4, 2, 7, 0, 9, 12)
    private val s1 = byteArrayOf(15, 12, 2, 7, 9, 0, 5, 10, 1, 11, 14, 8, 6, 13, 3, 4)
    private val s2 = byteArrayOf(8, 6, 7, 9, 3, 12, 10, 15, 13, 1, 14, 4, 0, 11, 5, 2)
    private val s3 = byteArrayOf(0, 15, 11, 8, 12, 9, 6, 3, 13, 1, 2, 4, 10, 7, 5, 14)
    private val s4 = byteArrayOf(1, 15, 8, 3, 12, 0, 11, 6, 2, 5, 4, 10, 9, 14, 7, 13)
    private val s5 = byteArrayOf(15, 5, 2, 11, 4, 10, 9, 12, 0, 3, 14, 8, 13, 6, 7, 1)
    private val s6 = byteArrayOf(7, 2, 12, 5, 8, 4, 6, 11, 14, 9, 1, 15, 13, 3, 10, 0)
    private val s7 = byteArrayOf(1, 13, 15, 0, 14, 8, 2, 11, 7, 4, 12, 10, 9, 3, 5, 6)
    private val sBoxes = arrayOf(s0, s1, s2, s3, s4, s5, s6, s7)

    private val is0 = byteArrayOf(13, 3, 11, 0, 10, 6, 5, 12, 1, 14, 4, 7, 15, 9, 8, 2)
    private val is1 = byteArrayOf(5, 8, 2, 14, 15, 6, 12, 3, 11, 4, 7, 9, 1, 13, 10, 0)
    private val is2 = byteArrayOf(12, 9, 15, 4, 11, 14, 1, 2, 0, 3, 6, 13, 5, 8, 10, 7)
    private val is3 = byteArrayOf(0, 9, 10, 7, 11, 14, 6, 13, 3, 5, 12, 2, 4, 8, 15, 1)
    private val is4 = byteArrayOf(5, 0, 8, 3, 10, 9, 7, 14, 2, 12, 11, 6, 4, 15, 13, 1)
    private val is5 = byteArrayOf(8, 15, 2, 9, 4, 1, 13, 14, 11, 6, 5, 3, 7, 12, 10, 0)
    private val is6 = byteArrayOf(15, 10, 1, 13, 5, 3, 6, 0, 4, 9, 14, 7, 2, 12, 8, 11)
    private val is7 = byteArrayOf(3, 0, 6, 13, 9, 14, 15, 8, 5, 12, 11, 7, 10, 1, 4, 2)
    private val isBoxes = arrayOf(is0, is1, is2, is3, is4, is5, is6, is7)


    private fun blockSize() = 16

    private fun keySize() = 32

    fun setKey(key: ByteArray) {
        if (key.size < keySize()) {
            this.key = ByteArray(keySize())
            for (i in key.indices) {
                this.key!![i] = key[i]
            }
            for (i in key.size until keySize()) {
                if (i == key.size) {
                    this.key!![i] = 0x80.toByte()
                } else {
                    this.key!![i] = 0x00.toByte()
                }
            }
        } else {
            this.key = key
        }

        for (i in 0 until 8) {
            val bytes = byteArrayOf(
                this.key!![4 * i],
                this.key!![4 * i + 1],
                this.key!![4 * i + 2],
                this.key!![4 * i + 3]
            )
            val wrap = ByteBuffer.wrap(bytes)
            prekeys[i] = wrap.int
        }
        for (i in 8 until prekeys.size) {
            val phi = (0x9e3779b9).toInt()
            val tmp: Int
            tmp = prekeys[i - 8] xor prekeys[i - 5] xor prekeys[i - 3] xor prekeys[i - 1] xor
                    (i - 8) xor phi
            prekeys[i] = tmp shl 11 or tmp.ushr(21)
        }
    }

    fun encrypt(text: ByteArray) {
        var data = initPermutation(text)
        var roundKey: ByteArray
        for (i in 0 until 32) {
            roundKey = getRoundKey(i)
            for (n in 0 until 16) {
                data[n] = (data[n] xor roundKey[n])
            }
            data = box(sBoxes, data, i)

            if (i == 31) {
                roundKey = getRoundKey(32)
                for (n in 0 until 16) {
                    data[n] = (data[n] xor roundKey[n])
                }
            } else {
                data = linearTransform(data)
            }
        }
        File("egeg").appendText("egege")
        data = finalPermutation(data)
        System.arraycopy(data, 0, text, 0, 16)
    }

    fun decrypt(text: ByteArray) {
        var data = initPermutation(text)
        var roundKey = getRoundKey(32)
        for (n in 0 until 16) {
            data[n] = (data[n] xor roundKey[n])
        }
        for (i in 31 downTo 0) {
            if (i != 31) {
                data = invLinearTransform(data)
            }
            data = box(isBoxes, data, i)
            roundKey = getRoundKey(i)
            for (n in 0 until 16) {
                data[n] = (data[n] xor roundKey[n])
            }
        }
        data = finalPermutation(data)
        System.arraycopy(data, 0, text, 0, 16)
    }

    private fun initPermutation(input: ByteArray): ByteArray {
        val output = ByteArray(16)
        for (i in 15 downTo 0) {
            val rightShift = (3 - (i % 4)) * 2
            output[i] =
                        (
                                (input[(15 - i) / 4].toInt().ushr(rightShift) and 0x01) or
                                ((input[(15 - i) / 4 + 4].toInt().ushr(rightShift) and 0x01) shl 1) or
                                ((input[(15 - i) / 4 + 8].toInt().ushr(rightShift) and 0x01) shl 2) or
                                ((input[(15 - i) / 4 + 12].toInt().ushr(rightShift) and 0x01) shl 3) or
                                ((input[(15 - i) / 4].toInt().ushr(rightShift + 1) and 0x01) shl 4) or
                                ((input[(15 - i) / 4 + 4].toInt().ushr(rightShift + 1) and 0x01) shl 5) or
                                ((input[(15 - i) / 4 + 8].toInt().ushr(rightShift + 1) and 0x01) shl 6) or
                                ((input[(15 - i) / 4 + 12].toInt().ushr(rightShift + 1) and 0x01) shl 7)
                        ).toByte()
        }

        return output
    }


    private fun finalPermutation(input: ByteArray): ByteArray {
        val output = ByteArray(16)
        var j: Int
        for (i in 0 until 16) {
            j = i % 4
            val shiftRight = i / 4
            output[i] =
                        (
                                (input[15 - j * 4].toInt().ushr(shiftRight) and 0x01) or
                                ((input[15 - j * 4].toInt().ushr(shiftRight + 4) and 0x01) shl 1) or
                                ((input[14 - j * 4].toInt().ushr(shiftRight) and 0x01) shl 2) or
                                ((input[14 - j * 4].toInt().ushr(shiftRight + 4) and 0x01) shl 3) or
                                ((input[13 - j * 4].toInt().ushr(shiftRight) and 0x01) shl 4) or
                                ((input[13 - j * 4].toInt().ushr(shiftRight + 4) and 0x01) shl 5) or
                                ((input[12 - j * 4].toInt().ushr(shiftRight) and 0x01) shl 6) or
                                ((input[12 - j * 4].toInt().ushr(shiftRight + 4) and 0x01) shl 7)
                        ).toByte()
        }

        return output
    }

    private fun box(boxes: Array<ByteArray>, data: ByteArray, round: Int): ByteArray {
        val toUse = boxes[round % 8]
        val output = ByteArray(blockSize())
        for (i in 0 until blockSize()) {
            val curr = data[i].toInt() and 0xFF
            val low4 = curr.ushr(4).toByte()
            val high4 = (curr and 0x0F).toByte()
            output[i] = (toUse[low4 % Byte.MAX_VALUE].toInt() shl 4 xor toUse[high4 % Byte.MAX_VALUE].toInt()).toByte()
        }
        return output
    }

    private fun linearTransform(outData: ByteArray): ByteArray {
        var data = outData
        data = finalPermutation(data)
        val buffer = ByteBuffer.wrap(data)
        var x0 = buffer.int
        var x1 = buffer.int
        var x2 = buffer.int
        var x3 = buffer.int
        x0 = (x0 shl 13) or x0.ushr(32 - 13)
        x2 = (x2 shl 3) or x2.ushr(32 - 3)
        x1 = x1 xor x0 xor x2
        x3 = x3 xor x2 xor (x0 shl 3)
        x1 = (x1 shl 1) or x1.ushr(32 - 1)
        x3 = (x3 shl 7) or x3.ushr(32 - 7)
        x0 = x0 xor x1 xor x3
        x2 = x2 xor x3 xor (x1 shl 7)
        x0 = (x0 shl 5) or x0.ushr(32 - 5)
        x2 = (x2 shl 22) or x2.ushr(32 - 22)
        buffer.clear()
        buffer.putInt(x0)
        buffer.putInt(x1)
        buffer.putInt(x2)
        buffer.putInt(x3)

        data = buffer.array()
        data = initPermutation(data)

        return data
    }

    private fun invLinearTransform(outData: ByteArray): ByteArray {
        var data = outData
        data = finalPermutation(data)
        val buffer = ByteBuffer.wrap(data)
        var x0 = buffer.int
        var x1 = buffer.int
        var x2 = buffer.int
        var x3 = buffer.int

        x2 = x2.ushr(22) or (x2 shl 32 - 22)
        x0 = x0.ushr(5) or (x0 shl 32 - 5)
        x2 = x2 xor x3 xor (x1 shl 7)
        x0 = x0 xor x1 xor x3
        x3 = x3.ushr(7) or (x3 shl 32 - 7)
        x1 = x1.ushr(1) or (x1 shl 32 - 1)
        x3 = x3 xor x2 xor (x0 shl 3)
        x1 = x1 xor x0 xor x2
        x2 = x2.ushr(3) or (x2 shl 32 - 3)
        x0 = x0.ushr(13) or (x0 shl 32 - 13)

        buffer.clear()
        buffer.putInt(x0)
        buffer.putInt(x1)
        buffer.putInt(x2)
        buffer.putInt(x3)

        data = buffer.array()
        data = initPermutation(data)

        return data
    }

    private fun getRoundKey(round: Int): ByteArray {
        val k0 = prekeys[4 * round + 8]
        val k1 = prekeys[4 * round + 9]
        val k2 = prekeys[4 * round + 10]
        val k3 = prekeys[4 * round + 11]
        val box = ((3 - round) % 8 + 8) % 8
        val data = ByteArray(16)
        for (j in 0 until 32 step 2) {
            data[j / 2] =
                        (
                                (k0.ushr(j) and 0x01) or
                                ((k1.ushr(j) and 0x01) shl 1) or
                                ((k2.ushr(j) and 0x01) shl 2) or
                                ((k3.ushr(j) and 0x01) shl 3) or
                                ((k0.ushr(j + 1) and 0x01) shl 4) or
                                ((k1.ushr(j + 1) and 0x01) shl 5) or
                                ((k2.ushr(j + 1) and 0x01) shl 6) or
                                ((k3.ushr(j + 1) and 0x01) shl 7)
                        ).toByte()
        }
        val out = box(sBoxes, data, box)
        val key = ByteArray(16)
        for (i in 3 downTo 0) {
            for (j in 0 until 4) {
                key[3 - i] =
                            (
                                    key[3 - i].toInt() or ((out[i * 4 + j] and 0x01).toInt() shl (j * 2)) or
                                    ((out[i * 4 + j].toInt().ushr(4) and 0x01) shl (j * 2 + 1))
                            ).toByte()
                key[7 - i] =
                            (
                                    key[7 - i].toInt() or ((out[i * 4 + j].toInt().ushr(1) and 0x01) shl (j * 2)) or
                                    ((out[i * 4 + j].toInt().ushr(5) and 0x01) shl (j * 2 + 1))
                            ).toByte()
                key[11 - i] =
                            (
                                    key[11 - i].toInt() or ((out[i * 4 + j].toInt().ushr(2) and 0x01) shl (j * 2)) or
                                    ((out[i * 4 + j].toInt().ushr(6) and 0x01) shl (j * 2 + 1))
                            ).toByte()
                key[15 - i] =
                            (
                                    key[15 - i].toInt() or ((out[i * 4 + j].toInt().ushr(3) and 0x01) shl (j * 2)) or
                                    ((out[i * 4 + j].toInt().ushr(7) and 0x01) shl (j * 2 + 1))
                            ).toByte()
            }
        }
        return initPermutation(key)
    }
}