package com.sriniketh.core_platform.dagger

import com.sriniketh.core_platform.DateTimeSource
import com.sriniketh.core_platform.DateTimeSourceImpl
import com.sriniketh.core_platform.permissions.CameraPermissionChecker
import com.sriniketh.core_platform.permissions.CameraPermissionCheckerImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class PlatformModule {

    @Binds
    abstract fun cameraPermissionChecker(impl: CameraPermissionCheckerImpl): CameraPermissionChecker

    @Binds
    abstract fun dateTimeSource(impl: DateTimeSourceImpl): DateTimeSource
}
