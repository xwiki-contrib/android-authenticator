package org.xwiki.android.sync.utils

import android.content.Context
import androidx.appcompat.app.AlertDialog
import org.xwiki.android.sync.R

fun Context.showDialog(title: String?, message: String) {
    val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    if (!title.isNullOrEmpty()) builder.setTitle(title)
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
        dialog?.cancel()
    }
    builder.show()
}

class DialogUtils
