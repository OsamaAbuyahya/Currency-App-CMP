package org.example.project.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import currencyappcmp.composeapp.generated.resources.Res
import currencyappcmp.composeapp.generated.resources.bebas_neue_regular
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.Font

fun calculateExchangeRate(source: Double, target: Double): Double = target / source

fun convert(amount: Double, exchangeRate: Double): Double = amount * exchangeRate

fun displayCurrentDateTime(): String {
    val currentTimestamp = Clock.System.now()
    val date = currentTimestamp.toLocalDateTime(TimeZone.currentSystemDefault())

    // Format the LocalDate into the desired representation
    val dayOfMonth = date.dayOfMonth
    val month =
        date.month.toString().lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    val year = date.year

    // Determine the suffix for the day of the month
    val suffix = when {
        dayOfMonth in 11..13 -> "th" // Special case for 11th, 12th, and 13th
        dayOfMonth % 10 == 1 -> "st"
        dayOfMonth % 10 == 2 -> "nd"
        dayOfMonth % 10 == 3 -> "rd"
        else -> "th"
    }

    // Format the date in the desired representation
    return "$dayOfMonth$suffix $month, $year."
}
@Composable
fun GetBabesFontFamily() = FontFamily(Font(Res.font.bebas_neue_regular) )