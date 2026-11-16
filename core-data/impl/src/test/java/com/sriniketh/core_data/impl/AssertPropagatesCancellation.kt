package com.sriniketh.core_data.impl

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import org.junit.Assert.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
internal suspend fun TestScope.assertPropagatesCancellation(block: suspend () -> Unit) {
    var thrown: Throwable? = null
    val job = launch {
        try {
            block()
        } catch (throwable: Throwable) {
            thrown = throwable
        }
    }
    runCurrent()
    job.cancel()
    job.join()
    assertTrue("Expected CancellationException but was $thrown", thrown is CancellationException)
}
