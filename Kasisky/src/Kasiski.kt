import java.io.File

typealias Counting = MutableMap<String, MutableList<Int>>

data class Pair(val frequency: Double, val letter: Char) : Comparable<Pair> {
    override fun compareTo(other: Pair): Int {
        return frequency.compareTo(other.frequency)
    }
}

const val IMPOSSIBLE = -1
const val ALPHABET = 26
const val MAX_KEY_WORD_LEN = 15

val FREQUENCIES = listOf(8.2, 1.5, 2.8, 4.2, 12.7, 2.2, 2.0, 6.1, 7.0, 0.1, 0.8, 4.0, 2.4,
        6.7, 7.5, 1.9, 0.1, 6.0, 6.3, 9.0, 2.8, 1.0, 2.4, 0.1, 2.0, 0.1)

class Kasiski(private val message: String, private val fileName: String) {

    private val sortedEnglishFrequencies = mutableListOf<Pair>()

    private val factors = IntArray(MAX_KEY_WORD_LEN + 1)
    private var keyWordLen = IMPOSSIBLE
    private var keyWord = ""
    private var max: Int = 0

    init {
        for (letter in 0 until ALPHABET) {
            sortedEnglishFrequencies.add(Pair(FREQUENCIES[letter], (letter + 'a'.toInt()).toChar()))
        }
        sortedEnglishFrequencies.sortWith(Comparator(Pair::compareTo).reversed())
    }

    fun test() {
        val text = message.replace("[^a-zA-Z]".toRegex(), "").toLowerCase()
        find3GramsAndBigramsFactors(text)
        findMaxRepetitions()
        printFactorsInfo()
        getGuess(text)
    }

    fun frequencyAnalysis(text: String) {
        keyWord = ""
        for (i in 0 until keyWordLen) {
            val curLen = text.length / keyWordLen
            val letterRepetitions = IntArray(ALPHABET)
            val curFrequency = mutableListOf<Pair>()
            for (j in i until text.length step keyWordLen) {
                letterRepetitions[letterDiff(text[j])]++
            }
            for (letter in 0 until ALPHABET) {
                val repeat = letterRepetitions[letter]
                curFrequency.add(Pair((repeat.toDouble() / curLen) * 100, ('a' + letter)))
            }
            curFrequency.sortWith(Comparator(Pair::compareTo).reversed())
            keyWord += guessKeyWordLetter(curFrequency)
        }
    }

    private fun guessKeyWordLetter(curFrequency: MutableList<Pair>): Char {
        val lettersFrequencies = DoubleArray(ALPHABET)
        for (letter in 0 until ALPHABET) {
            val shift = (curFrequency[letter].letter - sortedEnglishFrequencies[letter].letter + ALPHABET) % ALPHABET
            lettersFrequencies[shift] += sortedEnglishFrequencies[letter].frequency
        }
        val first = lettersFrequencies.mapIndexed { index, d -> Pair(index, d) }.sortedByDescending { it.second }[0].first
//        val classicMax = sortedEnglishFrequencies[0].letter
//        val curMax = curFrequency[0].letter
//        val diff = (curMax - classicMax + ALPHABET) % ALPHABET
//        return 'a' + diff
        return 'a' + first
    }

    private fun letterDiff(letter: Char) = letter - 'a'

    private fun find3GramsAndBigramsFactors(text: String) {
        val positions: Counting = mutableMapOf()
        for (i in 0 until text.length - 2) {
            val biGram = text.substring(i, i + 2)
            val threeGram = text.substring(i, i + 3)
            positions.getOrPut(threeGram) { mutableListOf() }.add(i)
            positions.getOrPut(biGram) { mutableListOf() }.add(i)
        }
        for ((_, pos) in positions) {
            if (pos.size < 2) {
                continue
            }
            for (i in 1 until pos.size) {
                val diff = pos[i] - pos[i - 1]
                addFactors(diff)
            }
        }
    }

    private fun addFactors(n: Int) {
        for (factor in 2..MAX_KEY_WORD_LEN) {
            if (n % factor == 0) {
                ++factors[factor]
            }
        }
    }

    private fun printFactorsInfo() {
        for (factor in 3..MAX_KEY_WORD_LEN) {
            println("Factor $factor repeats ${factors[factor]} times")
        }
        println()
        println("The most probable non-trivial key word length divides by $max")
    }

    private fun findMaxRepetitions() {
        for (i in 4..MAX_KEY_WORD_LEN) {
            if (factors[i] > factors[max]) {
                max = i
            }
        }
    }

    private fun getGuess(text: String) {
        println("Try to guess key word len or print :q to exit: ")
        var line = readLine()
        while (line != ":q") {
            keyWordLen = line!!.toInt()
            frequencyAnalysis(text)
            println("Probaple key word: $keyWord")
            decryptMessage(text)
            line = readLine()
        }
    }

    private fun decryptMessage(text: String) {
        print("Decrypted message: ")
        var encrypted = ""
        for (i in 0 until text.length - keyWordLen step keyWordLen) {
            for (j in 0 until keyWordLen) {
                val diff = (text[i + j] + ALPHABET - keyWord[j]) % ALPHABET
                val letter = 'a' + diff
                encrypted += letter
            }
        }
        val pathName = "decrypted/$fileName.out"
        File(pathName).writeText(encrypted)
        println(encrypted)
    }
}