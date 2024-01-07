package com.hasanbilgin.kotlinmaps.roomdb

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.hasanbilgin.kotlinmaps.model.Place
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Flowable

//eklendi
@Dao
interface PlaceDao {

    //Flowable ve Completable rxjava için eklendi

    @Query("SELECT * FROM Place")
    fun getAll(): Flowable<List<Place>>

    //bölede kulllanılabilir
//    @Query("SELECT * FROM Place WHERE id=:id")
//    fun getAllWhere(id: String): Flowable<List<Place>>

    @Insert
    fun insert(place: Place): Completable

    @Delete
    fun delete(place: Place): Completable
}
//kotlin class/file den interface açıldı