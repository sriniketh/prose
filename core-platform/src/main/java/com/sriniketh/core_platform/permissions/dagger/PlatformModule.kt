package com.sriniketh.core_platform.permissions.dagger

import com.sriniketh.core_platform.permissions.CameraPermissionChecker
import com.sriniketh.core_platform.permissions.CameraPermissionCheckerImpl
import com.sriniketh.core_platform.permissions.DateTimeSource
import com.sriniketh.core_platform.permissions.DateTimeSourceImpl
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
