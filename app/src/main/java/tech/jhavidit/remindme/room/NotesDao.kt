package tech.jhavidit.remindme.room

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import tech.jhavidit.remindme.model.NotesModel


@Dao
interface NotesDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addNotes(user: NotesModel)

    @Update
    suspend fun updateNotes(user: NotesModel)

    @Delete
    suspend fun deleteNotes(user: NotesModel)

    @Query("DELETE FROM notes_table")
    suspend fun deleteAllNotes()

    @Query("SELECT * FROM notes_table ORDER BY  isPinned DESC , id DESC ")
    fun readAllData(): LiveData<List<NotesModel>>

    @Query("SELECT COUNT(id) FROM notes_table")
    fun notesCount(): LiveData<Int>

    @Query("SELECT id FROM notes_table ORDER BY id DESC LIMIT 1")
    fun createdId(): LiveData<Int>

    @Query("SELECT * from notes_table where id = :id")
    fun selectedNote(id: Int): Flow<List<NotesModel>>

    @Query("SELECT * from notes_table where title like :searchQuery or description like :searchQuery")
    fun searchNotes(searchQuery: String?): Flow<List<NotesModel>>

}