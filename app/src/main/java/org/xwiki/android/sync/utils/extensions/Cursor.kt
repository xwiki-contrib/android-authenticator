package org.xwiki.android.sync.utils.extensions

import android.database.Cursor
import androidx.core.database.getStringOrNull

/**
 * Retrieve column index and get field value or null
 *
 * @since 0.6
 */
fun Cursor.getStringOrNull(field: String): String? = getStringOrNull(
    getColumnIndex(field)
)

/**
 * Retrieve column index and get field value or throw IllegalArgumentException
 *
 * @since 0.6
 */
fun Cursor.getString(field: String): String = getStringOrNull(
    getColumnIndex(field)
) ?: throw IllegalArgumentException("Field $field was not found in cursor $this")
