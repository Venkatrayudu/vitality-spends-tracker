package com.vmeduri.fintrack.util

import java.time.DayOfWeek
import java.time.LocalDate

/**
 * All week math assumes a Monday–Sunday week (the ISO-8601 convention, and the common
 * South African one), so the "weekly goal" resets every Monday and the Friday-evening
 * reminder is checking Monday-through-Friday-so-far against it.
 */
object DateUtils {

    fun startOfWeek(date: LocalDate): LocalDate = date.with(DayOfWeek.MONDAY)

    fun endOfWeek(date: LocalDate): LocalDate = startOfWeek(date).plusDays(6)

    fun startOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(1)

    fun endOfMonth(date: LocalDate): LocalDate = date.withDayOfMonth(date.lengthOfMonth())

    fun isLastDayOfMonth(date: LocalDate): Boolean = date.dayOfMonth == date.lengthOfMonth()
}
