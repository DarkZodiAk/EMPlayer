package com.example.musicplayer.data

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class ConcurrentExecutor(
    private val concurrency: Int,
    private val awaitDelay: Long,
    private val maxAwaits: Int
) {
    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val taskChannel = Channel<suspend () -> Unit>(Channel.UNLIMITED)

    private val isWorking = AtomicBoolean(false)
    private val activeTasks = AtomicInteger(0)

    suspend fun addTask(task: suspend () -> Unit) {
        ensureWorkersStarted()
        taskChannel.send(task)
    }

    private fun ensureWorkersStarted() {
        if(!isWorking.compareAndSet(false, true)) return

        repeat(concurrency) {
            scope.launch {
                while(true) {
                    val task = taskChannel.receive()
                    activeTasks.incrementAndGet()
                    try {
                        task()
                    } catch(e: CancellationException) {
                        throw e
                    } finally {
                        activeTasks.decrementAndGet()
                    }
                }
            }
        }
        startShutdownTimer()

    }

    private fun startShutdownTimer() {
        scope.launch {
            var awaitsLeft = maxAwaits
            while (awaitsLeft-- > 0) {
                delay(awaitDelay)
                if (activeTasks.get() > 0) {
                    awaitsLeft = maxAwaits
                }
            }
            shutdown()
        }
    }

    private fun shutdown() {
        scope.coroutineContext.cancelChildren()
    }
}