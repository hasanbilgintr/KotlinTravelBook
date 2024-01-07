package com.hasanbilgin.kotlinmaps.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

//@Entity(tableName = "tabloAdi")
//tablo adı da verilebilir verilmesse Adı Place dir
//eklendi
@Entity
class Place(
    @ColumnInfo(name = "name") val name: String,
    @ColumnInfo(name = "latitude") val latitude: Double,
    @ColumnInfo(name = "longitude") var longitude: Double) :
    Serializable {
    //id olan kolonu true vererek sayıyı kendisi oluşturucak
    @PrimaryKey(autoGenerate = true)
    var id = 0
}