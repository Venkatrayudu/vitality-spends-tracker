package com.vmeduri.fintrack.util

import java.util.Locale
import kotlin.math.abs

/**
 * Deliberately hand-rolled instead of java.text.NumberFormat.getCurrencyInstance —
 * that API's output for "ZAR" varies by Android version/locale data (sometimes "R",
 * sometimes "ZAR", sometimes a different grouping character), which is exactly the kind
 * of inconsistency you don't want in a budget app. This is simple and predictable.
 */
object CurrencyUtils {

    /** e.g. 123456 cents -> "R 1,234.56" */
    fun formatRands(cents: Long): String {
        val negative = cents < 0
        val absCents = abs(cents)
        val whole = absCents / 100
        val frac = absCents % 100
        val wholeFormatted = String.format(Locale.US, "%,d", whole)
        return buildString {
            if (negative) append("-")
            append("R ")
            append(wholeFormatted)
            append(".")
            append(frac.toString().padStart(2, '0'))
        }
    }

    /** Plain "1234.56" form (no grouping), suitable as the default text in an input field. */
    fun centsToPlainString(cents: Long): String {
        val whole = cents / 100
        val frac = abs(cents % 100)
        return "$whole.${frac.toString().padStart(2, '0')}"
    }

    /**
     * Parses user-typed input like "1234.56", "1,234.56" or "1234" into cents.
     * Returns null if the text isn't a valid non-negative amount.
     */
    fun parseRandsToCents(input: String): Long? {
        val cleaned = input.trim().replace(",", "").removePrefix("R").trim()
        if (cleaned.isEmpty()) return null
        val value = cleaned.toDoubleOrNull() ?: return null
        if (value < 0) return null
        return Math.round(value * 100)
    }
}
