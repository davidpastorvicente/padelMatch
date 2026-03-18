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

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("CREATE INDEX IF NOT EXISTS index_session_players_playerId ON session_players(playerId)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_games_sessionId ON games(sessionId)")
    }
}

@Database(
    entities = [PlayerEntity::class, SessionEntity::class, SessionPlayerEntity::class, GameEntity::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playerDao(): PlayerDao
    abstract fun sessionDao(): SessionDao
    abstract fun gameDao(): GameDao
}
