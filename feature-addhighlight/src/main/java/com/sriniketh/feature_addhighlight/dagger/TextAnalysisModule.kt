package com.sriniketh.feature_addhighlight.dagger

import com.sriniketh.feature_addhighlight.PageScanner
import com.sriniketh.feature_addhighlight.PageScannerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class TextAnalysisModule {

    @Binds
    abstract fun pageScanner(impl: PageScannerImpl): PageScanner
}
