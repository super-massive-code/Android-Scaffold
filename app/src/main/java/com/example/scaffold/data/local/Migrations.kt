package com.example.scaffold.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_1_2 =
    object : Migration(1, 2) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `contacts` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `firstName` TEXT NOT NULL,
                    `lastName` TEXT NOT NULL,
                    `addressLine1` TEXT NOT NULL,
                    `addressLine2` TEXT,
                    `city` TEXT NOT NULL,
                    `postcode` TEXT NOT NULL
                )
                """.trimIndent(),
            )
        }
    }
