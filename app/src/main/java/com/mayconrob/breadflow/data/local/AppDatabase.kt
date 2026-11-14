/*
 * BreadFlow - Gestão Financeira Pessoal
 * Copyright (C) 2026 Maycon Roberto - GitHub: @mayconrob
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 */
package com.mayconrob.breadflow.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mayconrob.breadflow.R
import com.mayconrob.breadflow.domain.enums.TransactionType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigDecimal

@Database(
    entities = [
        CategoryEntity::class,
        TransactionEntity::class,
        TagEntity::class,
        TransactionTagCrossRef::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun transactionDao(): TransactionDao
    abstract fun tagDao(): TagDao

    companion object {
        private const val DATABASE_NAME = "breadflow_database"

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback(context.applicationContext))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val context: Context
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialCategories(database.categoryDao())
                    }
                }
            }

            suspend fun populateInitialCategories(categoryDao: CategoryDao) {
                val defaultCategories = listOf(
                    // --- ENTRADAS ---
                    CategoryEntity(
                        name = context.getString(R.string.text_category_salary),
                        type = TransactionType.ENTRY,
                        colorHex = "#2E7D32" // Verde Floresta
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_prolabore),
                        type = TransactionType.ENTRY,
                        colorHex = "#43A047" // Verde Médio
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_sales),
                        type = TransactionType.ENTRY,
                        colorHex = "#00897B" // Verde Petróleo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_services),
                        type = TransactionType.ENTRY,
                        colorHex = "#00ACC1" // Ciano / Turquesa
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_extra),
                        type = TransactionType.ENTRY,
                        colorHex = "#7CB342" // Verde Lima
                    ),
                    CategoryEntity(
                        name = "Benefícios",
                        type = TransactionType.ENTRY,
                        colorHex = "#FB8C00" // Laranja Âmbar
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_transfer),
                        type = TransactionType.ENTRY,
                        colorHex = "#0288D1" // Azul Claro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_retirement),
                        type = TransactionType.ENTRY,
                        colorHex = "#1E88E5" // Azul Vibrante
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_pension),
                        type = TransactionType.ENTRY,
                        colorHex = "#3F51B5" // Índigo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_other_entries),
                        type = TransactionType.ENTRY,
                        colorHex = "#8E24AA" // Roxo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_freelance),
                        type = TransactionType.ENTRY,
                        colorHex = "#00838F" // Azul Escuro Petróleo
                    ),

                    // --- SAÍDAS ---
                    CategoryEntity(
                        name = context.getString(R.string.text_category_housing),
                        type = TransactionType.EXIT,
                        colorHex = "#6A1B9A" // Roxo Escuro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_supermarket),
                        type = TransactionType.EXIT,
                        colorHex = "#E65100" // Laranja Escuro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_food), // Restaurantes / Delivery
                        type = TransactionType.EXIT,
                        colorHex = "#D81B60" // Rosa / Magenta Vivo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_transport),
                        type = TransactionType.EXIT,
                        colorHex = "#1565C0" // Azul Profundo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_health),
                        type = TransactionType.EXIT,
                        colorHex = "#C62828" // Vermelho Escuro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_leisure),
                        type = TransactionType.EXIT,
                        colorHex = "#AD1457" // Rosa Escuro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_transfer_bill),
                        type = TransactionType.EXIT,
                        colorHex = "#455A64" // Azul Acinzentado
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_pet),
                        type = TransactionType.EXIT,
                        colorHex = "#EF6C00" // Laranja Vivo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_subscriptions),
                        type = TransactionType.EXIT,
                        colorHex = "#5E35B1" // Roxo Profundo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_taxes),
                        type = TransactionType.EXIT,
                        colorHex = "#37474F" // Grafite
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_education),
                        type = TransactionType.EXIT,
                        colorHex = "#00838F" // Azul Ciano Escuro
                    ),
                    // Novas categorias de saída inseridas com harmonia visual:
                    CategoryEntity(
                        name = context.getString(R.string.text_category_utilities),
                        type = TransactionType.EXIT,
                        colorHex = "#00695C" // Verde Água Escuro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_aesthetics),
                        type = TransactionType.EXIT,
                        colorHex = "#C2185B" // Rosa Cerise
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_clothing),
                        type = TransactionType.EXIT,
                        colorHex = "#7B1FA2" // Roxo Ametista
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_other_expenses),
                        type = TransactionType.EXIT,
                        colorHex = "#616161" // Cinza Neutro
                    )
                )

                categoryDao.insert(defaultCategories)
            }
        }
    }
}
