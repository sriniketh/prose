package com.sriniketh.feature_addhighlight.dagger

import com.sriniketh.feature_addhighlight.TextAnalyzer
import com.sriniketh.feature_addhighlight.TextAnalyzerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.FragmentComponent
import dagger.hilt.android.scopes.FragmentScoped

@Module
@InstallIn(FragmentComponent::class)
abstract class AddHighlightModule {

    @Binds
    @FragmentScoped
    abstract fun textAnalyzer(impl: TextAnalyzerImpl): TextAnalyzer
}
