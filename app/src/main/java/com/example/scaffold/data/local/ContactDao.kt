package com.example.scaffold.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactDao {
    @Insert
    suspend fun insert(contact: ContactEntity): Long

    @Query("SELECT * FROM contacts ORDER BY id DESC")
    fun observeContacts(): Flow<List<ContactEntity>>
}
