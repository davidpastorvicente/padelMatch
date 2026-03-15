package com.padelgroup.padelMatch.di

import android.content.Context
import androidx.room.Room
import com.padelgroup.padelMatch.data.db.AppDatabase
import com.padelgroup.padelMatch.data.db.dao.GameDao
import com.padelgroup.padelMatch.data.db.dao.PlayerDao
import com.padelgroup.padelMatch.data.db.dao.SessionDao
import com.padelgroup.padelMatch.data.db.MIGRATION_1_2
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "padel_match.db")
            .addMigrations(MIGRATION_1_2)
            .build()

    @Provides fun providePlayerDao(db: AppDatabase): PlayerDao = db.playerDao()
    @Provides fun provideSessionDao(db: AppDatabase): SessionDao = db.sessionDao()
    @Provides fun provideGameDao(db: AppDatabase): GameDao = db.gameDao()

    @Provides
    @Singleton
    fun provideContext(@ApplicationContext context: Context): Context = context
}
