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
                        colorHex = "#2E7D32" // Verde Escuro (Trabalho Principal)
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_prolabore),
                        type = TransactionType.ENTRY,
                        colorHex = "#4CAF50" // Verde Padrão
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_sales),
                        type = TransactionType.ENTRY,
                        colorHex = "#00ACC1" // Ciano / Azul Turquesa
                    ),

                    CategoryEntity(
                        name = context.getString(R.string.text_category_services),
                        type = TransactionType.ENTRY,
                        colorHex = "##7CB342" //Verde Lima
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_extra),
                        type = TransactionType.ENTRY,
                        colorHex = "#7CB342" // Verde Lima
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_transfer), // "Transferência / Ajuste de Saldo"
                        type = TransactionType.ENTRY,
                        colorHex = "#0288D1" // Azul Claro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_bolsa_familia), // "Bolsa Família"
                        type = TransactionType.ENTRY,
                        colorHex = "#F57C00" // Laranja
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_retirement), // "Aposentadoria"
                        type = TransactionType.ENTRY,
                        colorHex = "#00897B" // Verde Petróleo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_pension), // "Pensão"
                        type = TransactionType.ENTRY,
                        colorHex = "#3F51B5" // Índigo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_other_entries), // "Outras entradas"
                        type = TransactionType.ENTRY,
                        colorHex = "#8E24AA" // Roxo Claro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_freelance),
                        type = TransactionType.ENTRY,
                        colorHex = "#7CB342" // Verde Lima
                    ),

                    // --- SAÍDAS ---
                    CategoryEntity(
                        name = context.getString(R.string.text_category_housing),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("1200.00"),
                        colorHex = "#6A1B9A" // Roxo Vivo
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_supermarket), // Mercado
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("1000.00"),
                        colorHex = "#FF8F00" // Laranja Âmbar
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_food), // Delivery / Restaurante
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("200.00"),
                        colorHex = "#FF3D00" // Vermelho Alaranjado
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_transport),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("350.00"),
                        colorHex = "#1E88E5" // Azul Vibrante
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_health),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("350.00"),
                        colorHex = "#E53935" // Vermelho
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_debts),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("150.00"),
                        colorHex = "#6D4C41" // Marrom
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_leisure),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("650.00"),
                        colorHex = "#D81B60" // Rosa / Magenta
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_transfer_bill),
                        type = TransactionType.EXIT,
                        colorHex = "#546E7A" // Azul Acinzentado
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_category_pet),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("400.00"),
                        colorHex = "#FB8C00" // Laranja Quente
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_subscriptions),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("120.00"),
                        colorHex = "#8E24AA" // Violeta
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_taxes),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("0.00"),
                        colorHex = "#37474F" // Grafite / Escuro
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_education),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("350.00"),
                        colorHex = "#00ACC1" // Ciano / Turquesa
                    ),
                    CategoryEntity(
                        name = context.getString(R.string.text_other_expenses),
                        type = TransactionType.EXIT,
                        budgetLimit = BigDecimal("0.00"),
                        colorHex = "#757575" // Cinza Neutro
                    )
                )

                categoryDao.insert(defaultCategories)
            }
        }
    }
}
