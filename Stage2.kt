package indigo

fun main() {
    val deck = Deck()
    while (true) {
        println("Choose an action (reset, shuffle, get, exit):")
        print("> ")
        when (readLine()!!) {
            "reset" -> {
                deck.reset()
            }
            "shuffle" -> {
                deck.shuffle()
            }
            "get" -> {
                println("Number of cards:")
                print("> ")
                deck.get(readLine()!!)
            }
            "exit" -> {
                deck.exit()
                break
            }
            else -> {
                println("Wrong action.")
            }
        }
    }
}

class Deck {
    private val deck = mutableListOf<Card>()

    private companion object {
        private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        private val suits = listOf("♦", "♥", "♠", "♣")
        private var initialDeck = mutableListOf<Card>()

        init {
            for (s in suits) {
                for (r in ranks) {
                    initialDeck.add(Card(r, s))
                }
            }
        }
    }

    init {
        create()
    }

    private fun create() {
        deck.addAll(initialDeck)
    }

    fun reset() {
        deck.clear()
        create()
        println("Card deck is reset.")
    }

    fun shuffle() {
        deck.shuffle()
        println("Card deck is shuffled.")
    }

    fun get(input: String) {
        try {
            val n = input.toInt()
            if (n !in 1..52) throw InvalidRangeException()
            if (n > deck.size) throw InsufficientCardsException()
            repeat(n) {
                print("${deck[0]} ")
                deck.removeAt(0)
            }
            println()
        } catch (e: Exception) {
            when (e) {
                is InvalidRangeException, is NumberFormatException -> println("Invalid number of cards.")
                is InsufficientCardsException -> println("The remaining cards are insufficient to meet the request.")
            }
        }
    }

    fun exit() {
        println("Bye")
    }
}

data class Card(val rank: String, val suit: String) {
    override fun toString() = "$rank$suit"
}

class InvalidRangeException : Exception()
class InsufficientCardsException : Exception()