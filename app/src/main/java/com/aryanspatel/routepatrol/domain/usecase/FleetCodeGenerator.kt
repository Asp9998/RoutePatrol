package com.aryanspatel.routepatrol.domain.usecase

object FleetCodeGenerator {

    private const val LETTERS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val DIGITS = "0123456789"

    fun generateFleetCode(): String {
        val lettersPart = (1..3)
            .map { LETTERS.random() }
            .joinToString("")

        val digitsPart = (1..3)
            .map { DIGITS.random() }
            .joinToString("")

        return lettersPart + digitsPart
    }
}