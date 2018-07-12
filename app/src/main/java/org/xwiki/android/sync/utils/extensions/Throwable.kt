package org.xwiki.android.sync.utils.extensions

import retrofit2.HttpException

val Throwable.unauthorized: Boolean
    get() = this is HttpException && code() == 401
