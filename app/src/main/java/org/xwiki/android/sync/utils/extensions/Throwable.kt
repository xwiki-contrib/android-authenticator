package org.xwiki.android.sync.utils.extensions

import retrofit2.HttpException

/**
 * Will be true if this is HttpException and code == 401
 *
 * @since 0.5
 */
val Throwable.unauthorized: Boolean
    get() = this is HttpException && code() == 401
