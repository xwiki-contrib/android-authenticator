package org.xwiki.android.sync.utils

import android.content.Context
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import org.xwiki.android.sync.R

fun Context.showDialog(message: String, title: String? = null) {
    val builder = AlertDialog.Builder(this, R.style.AlertDialogTheme)
    title ?.also { builder.setTitle(it) }
    builder.setMessage(message)
    builder.setCancelable(true)
    builder.setPositiveButton(android.R.string.ok) { dialog, _ ->
        dialog?.cancel()
    }
    builder.show()
}

fun Context.showDialog(
    @StringRes message: Int,
    title: String? = null
) = showDialog(getString(message), title)

fun Context.showDialog(
    message: String,
    title: Int
) = showDialog(message, getString(title))

fun Context.showDialog(
    message: Int,
    title: Int
) = showDialog(getString(message), getString(title))
