package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "users",
    indices = [Index(value = ["email"], unique = true)]
)
data class UserEntity(
    @PrimaryKey
    val id: String,
    val email: String,
    val displayName: String,
    val passwordHash: String,
    val saltHex: String,
    val createdAt: Long = System.currentTimeMillis(),
    val tier: String = "Triton Pro",
    val avatarInitials: String = "TR"
)
