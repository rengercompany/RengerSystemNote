package com.renger.system.note.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [Note::class, Folder::class],
    version = 2,
    exportSchema = false
)
abstract class NoteDatabase : RoomDatabase() {
    abstract fun noteDao(): NoteDao
    abstract fun folderDao(): FolderDao

    companion object {
        @Volatile private var INSTANCE: NoteDatabase? = null

        // Миграция с версии 1 (старая БД с заметками) на версию 2
        // Добавляет: таблицу folders, колонки folderId и tags в notes
        // Данные заметок сохраняются!
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Создаём таблицу folders
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `folders` (
                        `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        `name` TEXT NOT NULL,
                        `color` INTEGER NOT NULL
                    )
                """.trimIndent())

                // Добавляем колонки в notes (nullable folderId, default tags = '')
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `folderId` INTEGER")
                db.execSQL("ALTER TABLE `notes` ADD COLUMN `tags` TEXT NOT NULL DEFAULT ''")
            }
        }

        fun getDatabase(context: Context): NoteDatabase =
            INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    NoteDatabase::class.java,
                    "renger_note_db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .build()
                    .also { INSTANCE = it }
            }
    }
}
