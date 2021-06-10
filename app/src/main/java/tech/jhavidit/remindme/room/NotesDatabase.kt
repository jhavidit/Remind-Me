package tech.jhavidit.remindme.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import tech.jhavidit.remindme.model.LocationModel
import tech.jhavidit.remindme.model.NotesModel
import java.util.concurrent.Executors

@Database(entities = [NotesModel::class,LocationModel::class], version = 1, exportSchema = false)
abstract class NotesDatabase : RoomDatabase() {

    abstract fun userDao(): NotesDao
    abstract fun locationDao() : LocationDao

    companion object {
        @Volatile
        private var INSTANCE: NotesDatabase? = null

        fun getDatabase(context: Context): NotesDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                        context.applicationContext,
                        NotesDatabase::class.java,
                        "notes_database"
                )
//                    .addCallback(object : RoomDatabase.Callback(){
//                    override fun onCreate(db: SupportSQLiteDatabase) {
//                        super.onCreate(db)
//                        Executors.newSingleThreadScheduledExecutor().execute(object : Runnable{
//                            override fun run(){
//                                getDatabase(context).userDao().addNotes(NotesModel())
//                            }
//                        })
//                    }
//                })
                    .build()
                INSTANCE = instance
                return instance
            }
        }
    }

}