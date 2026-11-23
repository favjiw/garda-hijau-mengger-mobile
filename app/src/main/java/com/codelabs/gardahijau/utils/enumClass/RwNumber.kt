package com.codelabs.gardahijau.utils.enumClass

enum class RwNumber(val label: String, val value: String) {
    RW_1("RW 1", "RW1"),
    RW_2("RW 2", "RW2"),
    RW_3("RW 3", "RW3"),
    RW_4("RW 4", "RW4"),
    RW_5("RW 5", "RW5");


    companion object {
        fun labels(): List<String> = values().map { it.label }
        fun fromLabel(label: String): RwNumber? = values().firstOrNull { it.label == label }

        fun fromInput(input: String): RwNumber? {
            val normalized = input.trim().uppercase()
            // If user typed exact value like "RW05"
            values().firstOrNull { it.value == normalized }?.let { return it }
            // Try to extract digits and build key "RW<n>"
            val digits = normalized.replace(Regex("\\D+"), "")
            if (digits.isEmpty()) return null
            val num = digits.toIntOrNull() ?: return null
            val key = "RW$num"
            return values().firstOrNull { it.value == key }
        }
    }
}