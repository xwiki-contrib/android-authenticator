package org.xwiki.android.sync.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

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

    fun lock() {
        synchronized(lockObject) {
            while (!lockChecker) {
                lockObject.wait()
            }
        }
    }
}
