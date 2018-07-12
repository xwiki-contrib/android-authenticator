package org.xwiki.android.sync.utils.extensions

/**
 * Simple variable to get tag of class instance
 *
 * @since 0.5
 */
val Any.TAG: String
    get() = this::class.java.simpleName

