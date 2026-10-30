package com.mayconrob.breadflow.data.repository.di

import com.mayconrob.breadflow.data.repository.CategoriaRepositoryImpl
import com.mayconrob.breadflow.data.repository.TagRepositoryImpl
import com.mayconrob.breadflow.data.repository.TransacaoRepositoryImpl
import com.mayconrob.breadflow.domain.repository.ICategoriaRepository
import com.mayconrob.breadflow.domain.repository.ITagRepository
import com.mayconrob.breadflow.domain.repository.ITransacaoRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindCategoriaRepository(
        impl: CategoriaRepositoryImpl
    ): ICategoriaRepository

    @Binds
    @Singleton
    abstract fun bindTransacaoRepository(
        impl: TransacaoRepositoryImpl
    ): ITransacaoRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): ITagRepository
}