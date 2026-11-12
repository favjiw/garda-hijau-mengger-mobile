package com.codelabs.gardahijau.utils

import android.app.DatePickerDialog
import android.content.Context
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateTimeUtils {
    fun showDatePicker(
        context: Context,
        onDateSelected: (String) -> Unit
    ) {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        DatePickerDialog(
            context,
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedCalendar = Calendar.getInstance()
                selectedCalendar.set(selectedYear, selectedMonth, selectedDay)

                val dateFormat = SimpleDateFormat("dd MMMM yyyy", Locale("id"))
                val formattedDate = dateFormat.format(selectedCalendar.time)

                onDateSelected(formattedDate)
            },
            year,
            month,
            day
        ).show()
    }

     fun getDayName(dateString: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val date = sdf.parse(dateString)

            val dayFormat = SimpleDateFormat("EEE", Locale("id", "ID"))
            dayFormat.format(date!!) // contoh: Sen, Sel, Rab
        } catch (e: Exception) {
            ""
        }
    }

}
