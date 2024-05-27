package com.sriniketh.core_platform.dagger

import com.sriniketh.core_platform.DateTimeSource
import com.sriniketh.core_platform.DateTimeSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {

    @Binds
    abstract fun dateTimeSource(impl: DateTimeSourceImpl): DateTimeSource
}
