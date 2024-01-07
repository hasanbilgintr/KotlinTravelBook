package com.hasanbilgin.kotlinmaps.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hasanbilgin.kotlinmaps.model.Place

//@Database(entities = arrayOf(Place::class), version = 1) yada
//modelde değişiklik yaparsanız versiyon 1 değiştirilmeli
@Database(entities = [Place::class], version = 1)
abstract class  PlaceDatabase : RoomDatabase() {
    abstract fun placeDao(): PlaceDao
}