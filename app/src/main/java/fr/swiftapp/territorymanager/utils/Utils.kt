package fr.swiftapp.territorymanager.utils

import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Locale

fun formatDate(textDate: String): String {
    if (textDate.length != 6) {
        return "Invalid date"
    }

    val jour = textDate.substring(0, 2)
    val mois = textDate.substring(2, 4)
    val annee = textDate.substring(4, 6)

    return "$jour/$mois/$annee"
}

fun convertDate(jjmmaa: String): String {
    return try {
        val inputFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val date = inputFormat.parse(jjmmaa)
        outputFormat.format(date!!)
    } catch (e: ParseException) {
        e.printStackTrace()
        jjmmaa
    }
}

fun reverseDate(yyyyMmDd: String): String {
    return try {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("ddMMyy", Locale.getDefault())

        val date = inputFormat.parse(yyyyMmDd)
        outputFormat.format(date!!)
    } catch (e: ParseException) {
        e.printStackTrace()
        yyyyMmDd
    }
}
