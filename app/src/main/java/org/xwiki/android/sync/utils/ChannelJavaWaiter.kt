package org.xwiki.android.sync.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

fun <T> Deferred<T>.awaitBlocking(scope: CoroutineScope): T? {
    val channel = Channel<Boolean>(1)
    var result: T? = null

    val waiter = ChannelJavaWaiter(
        scope,
        channel
    )

    scope.launch {
        try {
            val awaited = await()
            result = awaited
        } finally {
            channel.send(true)
        }
    }

    waiter.lock()
    return result
}

class ChannelJavaWaiter(
    scope: CoroutineScope,
    private val channelToWait: ReceiveChannel<Boolean>
) {
    private val lockObject = Object()
    private var lockChecker: Boolean = false

    init {
        scope.launch {
            while (isActive && !lockChecker) {
                if (channelToWait.receive()) {
                    synchronized(lockObject) {
                        lockChecker = true
                        lockObject.notifyAll()
                    }
                }
            }
        }
    }

    @Throws(InterruptedException::class)
    fun lock() {
        synchronized(lockObject) {
            while (!lockChecker) {
                lockObject.wait()
            }
        }
    }
}
