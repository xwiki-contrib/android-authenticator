package org.xwiki.android.sync.utils.extensions

import android.view.View
import android.view.ViewGroup

var View.enabled: Boolean
    get() = isEnabled
    set(value) {
        when (this) {
            is ViewGroup -> this.enabled = value
            else -> isEnabled = value
        }
    }
