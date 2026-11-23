package com.codelabs.gardahijau.model.local.antrean

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface AntreanDao {

    @Insert
    suspend fun tambah(item: AntreanItem): Long

    /** Urut lama -> baru supaya kiriman yang saling bergantung tidak mendahului. */
    @Query("SELECT * FROM antrean_kirim ORDER BY id ASC")
    suspend fun semua(): List<AntreanItem>

    @Query("SELECT COUNT(*) FROM antrean_kirim")
    fun jumlah(): Flow<Int>

    @Delete
    suspend fun hapus(item: AntreanItem)

    @Query("UPDATE antrean_kirim SET percobaan = percobaan + 1 WHERE id = :id")
    suspend fun tambahPercobaan(id: Long)

    @Query("DELETE FROM antrean_kirim")
    suspend fun bersihkan()
}
