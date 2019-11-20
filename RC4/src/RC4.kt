import kotlin.experimental.xor

class RC4(key: ByteArray) {

    private val s: ByteArray
    private var x = 0
    private var y = 0

    init {
        s = ByteArray(256) { i -> i.toByte() }
        val keySize = key.size
        var j = 0
        for (i in 0 until 256) {
            j = (j + s[i] + key[i % keySize] + 256) % 256
            s.swap(i, j)
        }
    }

    private fun ByteArray.swap(i: Int, j: Int) {
        val temp = this[i]
        this[i] = this[j]
        this[j] = temp
    }

    private fun getKeyItem(): Byte {
        x = (x + 1) % 256
        y = (y + s[x] + 256) % 256
        s.swap(x, y)
        return s[(s[x] + s[y] + 256) % 256]
    }

    fun encode(data: ByteArray): ByteArray {
        return ByteArray(data.size) { i ->
            data[i] xor getKeyItem()
        }
    }

    fun decode(data: ByteArray): ByteArray {
        return encode(data)
    }
}