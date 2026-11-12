package com.codelabs.gardahijau.utils.enumClass

enum class MediaTelurType {
    DEDAK,
    KOTORAN_TERNAK,
    LIMBAH_ORGANIK,
    CAMPURAN;

    override fun toString(): String {
        return when (this) {
            DEDAK -> "Dedak"
            KOTORAN_TERNAK -> "Kotoran Ternak"
            LIMBAH_ORGANIK -> "Limbah Organik"
            CAMPURAN -> "Campuran"
        }
    }
}
