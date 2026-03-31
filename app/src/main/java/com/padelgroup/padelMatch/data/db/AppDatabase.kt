package com.davidpv.padelmatch.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.davidpv.padelmatch.data.db.dao.GameDao
import com.davidpv.padelmatch.data.db.dao.PlayerDao
import com.davidpv.padelmatch.data.db.dao.SessionDao
import com.davidpv.padelmatch.data.db.entity.GameEntity
import com.davidpv.padelmatch.data.db.entity.PlayerEntity
import com.davidpv.padelmatch.data.db.entity.SessionEntity
import com.davidpv.padelmatch.data.db.entity.SessionPlayerEntity

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
