package org.synergy.barrier

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import org.synergy.barrier.base.EventQueue

@Module
@InstallIn(SingletonComponent::class)
object BarrierModule {
    @Provides
    fun provideEventQueue() = EventQueue()
}