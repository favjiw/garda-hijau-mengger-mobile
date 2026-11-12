package com.codelabs.gardahijau.utils.enumClass

enum class RwNumber(val label: String, val value: String) {
    RW_4("RW 4", "RW4"),
    RW_5("RW 5", "RW5");


    companion object {
        fun labels(): List<String> = values().map { it.label }
        fun fromLabel(label: String): RwNumber? = values().firstOrNull { it.label == label }

        fun fromInput(input: String): RwNumber? {
            val normalized = input.trim().uppercase()
            // If user typed exact value like "RW05"
            values().firstOrNull { it.value == normalized }?.let { return it }
            // Try to extract digits and build key "RW##"
            val digits = normalized.replace(Regex("\\D+"), "")
            if (digits.isEmpty()) return null
            val num = digits.toIntOrNull() ?: return null
            val key = if (num in 1..9) "RW0${num}" else "RW${num}"
            return values().firstOrNull { it.value == key }
        }
    }
}