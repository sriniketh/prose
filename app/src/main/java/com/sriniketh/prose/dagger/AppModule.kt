package com.sriniketh.prose.dagger

import com.sriniketh.core_platform.FileSource
import com.sriniketh.prose.files.FileSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun fileSource(impl: FileSourceImpl): FileSource
}
