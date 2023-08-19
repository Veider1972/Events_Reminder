package ru.veider.eventsreminder.presentation.ui

/**
 * Класс для вывода числительных с правильными окончаниями в зависимости от количества
 * */
data class RusIntPlural(
    val name: String,
    val number: Int,
    val singleEnding: String = "",
    val twoToFourEnding: String = "",
    val fiveToTenEnding: String = ""
) {
    override fun toString(): String {
        val suffix =
        if ((number / 10) % 10 != 1) {
            when (number % 10) {
                1 -> singleEnding
                in 2..4 -> twoToFourEnding
                else -> fiveToTenEnding
            }
        } else fiveToTenEnding
        return "$number $name$suffix"
    }
}