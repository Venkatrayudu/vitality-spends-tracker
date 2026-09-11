package com.vmeduri.fintrack.util

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * All week math assumes a Saturday–Friday week (the ISO-8601 convention, and the common
 * South African one), so the "weekly goal" resets every Saturday and the Friday-evening
 * reminder is checking Saturday-through-Friday-so-far against it.
 */
object DateUtils {

    fun startOfWeek(date: LocalDate): LocalDate = date.with(DayOfWeek.SATURDAY)

    fun endOfWeek(date: LocalDate): LocalDate = startOfWeek(date).plusDays(6)

    fun startOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(1)

    fun endOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(date.lengthOfMonth())

    fun isLastDayOfMonth(date: LocalDate): Boolean = date.dayOfMonth == date.lengthOfMonth()
}
