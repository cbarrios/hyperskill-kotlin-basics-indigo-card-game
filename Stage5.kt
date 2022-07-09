package indigo

import kotlin.system.exitProcess

fun main() {
    val game = CardGame()
    game.launch()
}

class CardGame {

    companion object {
        private const val GAME_NAME = "Indigo Card Game"
        private const val INITIAL_TABLE_CARDS = 4

        private val deck = Deck()
        private val human = Human(deck)
        private val computer = Computer(deck)
        private val tableCards = mutableListOf<Card>()
        private var state = State.NEW_GAME
        private var playerThatWonLast: Player? = null
        private var playerThatPlayedFirst: Player? = null

        fun showStats() {
            println("Score: Player ${human.score} - Computer ${computer.score}")
            println("Cards: Player ${human.numberOfCardsWon} - Computer ${computer.numberOfCardsWon}")
        }
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
        playerThatPlayedFirst = if (nextState == State.HUMAN_PLAY) human else computer
    }

    private fun adjustFinalScore() {
        if (tableCards.isNotEmpty()) {
            playerThatWonLast?.addAndScore(tableCards) ?: kotlin.run {
                playerThatPlayedFirst?.addAndScore(tableCards)
            }
        }
        when {
            human.numberOfCardsWon > computer.numberOfCardsWon -> human.addFinalPoints()
            human.numberOfCardsWon < computer.numberOfCardsWon -> computer.addFinalPoints()
            human.numberOfCardsWon == computer.numberOfCardsWon -> playerThatPlayedFirst?.addFinalPoints()
        }
    }

    private fun validTable(): Boolean {
        if (tableCards.isEmpty()) {
            println("No cards on the table")
        } else {
            println("${tableCards.size} cards on the table, and the top card is ${tableCards.last()}")
        }
        if (deck.isEmpty() && human.handsEmpty() && computer.handsEmpty()) {
            state = State.END_GAME
            adjustFinalScore()
            showStats()
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
                        val result = human.play(tableCards)
                        state = if (!result.exiting) {
                            if (result.wonCards) playerThatWonLast = human
                            State.COMPUTER_PLAY
                        } else {
                            State.END_GAME
                        }
                    }
                }
                State.COMPUTER_PLAY -> {
                    if (validTable()) {
                        val result = computer.play(tableCards)
                        if (result.wonCards) {
                            playerThatWonLast = computer
                            println()
                        }
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
        private val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        private val suits = listOf("@", "#", "$", "%") // "♦", "♥", "♠", "♣"
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

    fun isEmpty() = cards.isEmpty()
}

abstract class Player(private val deck: Deck) {
    protected val hands = mutableListOf<Card>()
    private val cardsWon = mutableListOf<Card>()
    val numberOfCardsWon: Int
        get() = cardsWon.size
    var score = 0
        private set

    private companion object {
        private const val MAX_CARDS = 6
    }

    abstract fun play(tableCards: MutableList<Card>): PlayResult

    data class PlayResult(val exiting: Boolean = false, val wonCards: Boolean = false)

    protected fun winCards(tableCards: MutableList<Card>, player: Player) {
        addAndScore(tableCards)
        println(if (player is Human) "Player wins cards" else "Computer wins cards")
        CardGame.showStats()
    }

    fun refill() {
        hands.addAll(deck.get(MAX_CARDS))
    }

    fun addFinalPoints() {
        score += 3
    }

    fun addAndScore(tableCards: MutableList<Card>) {
        for (c in tableCards) {
            if (c.isValuable) score++
            cardsWon.add(c)
        }
        tableCards.clear()
    }

    fun handsEmpty() = hands.isEmpty()

}

class Human(deck: Deck) : Player(deck) {
    override fun play(tableCards: MutableList<Card>): PlayResult {
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
                if (tableCards.isNotEmpty() && tableCards.last().isCandidate(card)) {
                    tableCards.add(card)
                    winCards(tableCards, this)
                    println()
                    return PlayResult(wonCards = true)
                } else {
                    tableCards.add(card)
                }
                println()
                return PlayResult()
            }
        }
        return PlayResult(exiting = true)
    }
}

class Computer(deck: Deck) : Player(deck) {
    override fun play(tableCards: MutableList<Card>): PlayResult {
        val index = pickCardIndex(tableCards.lastOrNull())
        println("${CardGame.State.COMPUTER_PLAY.message} ${hands[index]}")
        val card = hands.removeAt(index)
        if (hands.isEmpty()) refill()
        if (tableCards.isNotEmpty() && tableCards.last().isCandidate(card)) {
            tableCards.add(card)
            winCards(tableCards, this)
            return PlayResult(wonCards = true)
        }
        println()
        tableCards.add(card)
        return PlayResult()
    }

    private fun pickCardIndex(topCard: Card?): Int {
        println(hands.joinToString(" "))
        if (hands.size > 1) {
            if (topCard != null) {
                val candidates = mutableListOf<Pair<Int, Card>>()
                for ((i, c) in hands.withIndex()) {
                    if (c.isCandidate(topCard)) candidates.add(Pair(i, c))
                }
                // 4
                return if (candidates.isEmpty()) {
                    noCandidatesStrategy()
                } else {
                    // 2 and 5
                    candidatesStrategy(candidates)
                }
            } else {
                // 3
                return noCandidatesStrategy()
            }
        }
        // 1
        return 0
    }

    // 3 and 4
    private fun noCandidatesStrategy(): Int {
        val sameSuits = hands.groupBy { it.suit }.values.filter { it.size > 1 }.flatten()
        if (sameSuits.isNotEmpty()) return hands.indexOf(sameSuits.random())
        val sameRanks = hands.groupBy { it.rank }.values.filter { it.size > 1 }.flatten()
        if (sameRanks.isNotEmpty()) return hands.indexOf(sameRanks.random())
        return hands.indexOf(hands.random())
    }

    private fun candidatesStrategy(candidates: MutableList<Pair<Int, Card>>): Int {
        // 2
        if (candidates.size == 1) return candidates.first().first
        // 5
        val sameSuits = candidates.groupBy { it.second.suit }.values.filter { it.size > 1 }.flatten()
        if (sameSuits.isNotEmpty()) return sameSuits.random().first
        val sameRanks = candidates.groupBy { it.second.rank }.values.filter { it.size > 1 }.flatten()
        if (sameRanks.isNotEmpty()) return sameRanks.random().first
        return candidates.random().first
    }
}

data class Card(val rank: String, val suit: String) {
    override fun toString() = "$rank$suit"

    val isValuable: Boolean
        get() = rank in listOf("A", "10", "J", "Q", "K")

    fun isCandidate(card: Card) = this.rank == card.rank || this.suit == card.suit
}