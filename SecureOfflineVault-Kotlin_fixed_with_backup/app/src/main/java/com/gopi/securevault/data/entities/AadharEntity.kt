package com.gopi.securevault.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "aadhar")
data class AadharEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String?,
    val number: String?
)
