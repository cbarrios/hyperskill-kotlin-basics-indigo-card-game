package indigo

fun main() {
    val ranks = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
    val suits = listOf("♦", "♥", "♠", "♣")

    val deck = mutableListOf<String>()
    for (s in suits) {
        for (r in ranks) {
            deck.add("$r$s")
        }
    }

    println(ranks.joinToString(" "))
    println(suits.joinToString(" "))
    println(deck.joinToString(" "))
}