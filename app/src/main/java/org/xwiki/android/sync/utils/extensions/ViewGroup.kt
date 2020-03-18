package org.xwiki.android.sync.utils.extensions

import android.view.ViewGroup

var ViewGroup.enabled: Boolean
    get() = isEnabled
    set(value) {
        isEnabled = value
        for (i in (0 until childCount)) {
            when (val child = getChildAt(i)) {
                is ViewGroup -> child.enabled = value
                else -> child.isEnabled = value
            }
        }
    }
