package indigo

import kotlin.system.exitProcess

fun main() {
    val game = CardGame()
    game.launch()
}

class CardGame {
    private val deck = Deck()
    private val human = Human(deck)
    private val computer = Computer(deck)
    private val tableCards = mutableListOf<Card>()
    private var state = State.NEW_GAME

    private companion object {
        private const val GAME_NAME = "Indigo Card Game"
        private const val INITIAL_TABLE_CARDS = 4
    }

    enum class State(val message: String) {
        NEW_GAME("Play first?"),
        HUMAN_PLAY("Choose a card to play (1-"),
        COMPUTER_PLAY("Computer plays"),
        END_GAME("Game Over")
    }

    private fun setupNewGame(nextState: State) {
        tableCards.addAll(deck.get(INITIAL_TABLE_CARDS))
        human.refill()
        computer.refill()
        println("Initial cards on the table: ${tableCards.joinToString(" ")}")
        println()
        state = nextState
    }

    private fun validTable(): Boolean {
        println("${tableCards.size} cards on the table, and the top card is ${tableCards.last()}")
        if (tableCards.size == Deck.MAX_SIZE) {
            state = State.END_GAME
            return false
        }
        return true
    }

    fun launch() {
        println(GAME_NAME)
        while (true) {
            when (state) {
                State.NEW_GAME -> {
                    println(state.message)
                    print("> ")
                    when (readLine()!!.lowercase()) {
                        "yes" -> setupNewGame(State.HUMAN_PLAY)
                        "no" -> setupNewGame(State.COMPUTER_PLAY)
                    }
                }
                State.HUMAN_PLAY -> {
                    if (validTable()) {
                        val card = human.play()
                        state = if (card != null) {
                            tableCards.add(card)
                            State.COMPUTER_PLAY
                        } else {
                            State.END_GAME
                        }
                    }
                }
                State.COMPUTER_PLAY -> {
                    if (validTable()) {
                        val card = computer.play()
                        tableCards.add(card)
                        state = State.HUMAN_PLAY
                    }
                }
                State.END_GAME -> {
                    println(state.message)
                    exitProcess(0)
                }
            }
        }
    }
}

class Deck {
    private val cards = mutableListOf<Card>()

    companion object {
        const val MAX_SIZE = 52
        private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        private val suits = listOf("♦", "♥", "♠", "♣")
        private val initialCards = mutableListOf<Card>()

        init {
            for (s in suits) {
                for (r in ranks) {
                    initialCards.add(Card(r, s))
                }
            }
        }
    }

    init {
        create()
        shuffle()
    }

    private fun create() {
        cards.addAll(initialCards)
    }

    private fun shuffle() {
        cards.shuffle()
    }

    fun get(numberOfCards: Int): MutableList<Card> {
        val result = mutableListOf<Card>()
        if (numberOfCards > cards.size) return result
        repeat(numberOfCards) {
            val card = cards.removeAt(0)
            result.add(card)
        }
        return result
    }
}

abstract class Player(private val deck: Deck) {
    protected val hands = mutableListOf<Card>()

    private companion object {
        private const val MAX_CARDS = 6
    }

    abstract fun play(): Card?

    fun refill() {
        hands.addAll(deck.get(MAX_CARDS))
    }
}

class Human(deck: Deck) : Player(deck) {
    override fun play(): Card? {
        print("Cards in hand:")
        for (i in 1..hands.size) {
            print(" $i)${hands[i - 1]}")
        }
        println()
        while (true) {
            println("${CardGame.State.HUMAN_PLAY.message}${hands.size}):")
            print("> ")
            val input = readLine()
            if (input == "exit") break
            val cardNumber = input?.toIntOrNull()
            if (cardNumber != null && cardNumber in 1..hands.size) {
                val card = hands.removeAt(cardNumber - 1)
                if (hands.isEmpty()) refill()
                println()
                return card
            }
        }
        return null
    }
}

class Computer(deck: Deck) : Player(deck) {
    override fun play(): Card {
        println("${CardGame.State.COMPUTER_PLAY.message} ${hands.first()}")
        println()
        val card = hands.removeAt(0)
        if (hands.isEmpty()) refill()
        return card
    }
}

data class Card(val rank: String, val suit: String) {
    override fun toString() = "$rank$suit"
}