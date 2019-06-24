package org.xwiki.android.sync.utils

import androidx.test.espresso.IdlingResource

private val RESOURCE = "GLOBAL"

private val mCountingIdlingResource = SimpleCountingIdlingResource(RESOURCE)

val idlingResource: IdlingResource
    get() = mCountingIdlingResource

fun increment() {
    mCountingIdlingResource.increment()
}

fun decrement() {
    mCountingIdlingResource.decrement()
}