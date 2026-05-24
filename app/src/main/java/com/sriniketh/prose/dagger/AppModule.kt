package com.sriniketh.prose.dagger

import com.sriniketh.core_platform.FileSource
import com.sriniketh.prose.BuildConfig
import com.sriniketh.prose.files.FileSourceImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    abstract fun fileSource(impl: FileSourceImpl): FileSource

    companion object {

        @Provides
        @Named("userAgent")
        fun providesUserAgent(): String = "Prose/${BuildConfig.VERSION_NAME}"
    }
}
