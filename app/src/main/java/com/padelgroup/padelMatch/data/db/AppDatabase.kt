package com.padelgroup.padelMatch.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.db.entity.GameEntity
import com.padelgroup.padelMatch.data.db.entity.PlayerEntity
import com.padelgroup.padelMatch.data.db.entity.SessionEntity
import com.padelgroup.padelMatch.data.db.entity.SessionPlayerEntity

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE games ADD COLUMN winningPair INTEGER")
    }
}

@Database(
    entities = [PlayerEntity::class, SessionEntity::class, SessionPlayerEntity::class, GameEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun sessionDao(): SessionDao
    abstract fun gameDao(): GameDao
}
