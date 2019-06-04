/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.xwiki.android.sync.utils

import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.BitmapFactory
import android.util.Log

import java.io.ByteArrayOutputStream


/**
 * Static class with utils for images.
 *
 * @version $Id: 6cc29eab422fe64d903efff3ad167ae091da96c8 $
 */
class ImageUtils {

    companion object{
        /**
         * Logs tag
         */
        private val TAG = "ImageUtils"

        /**
         * compress bitmap by quality.
         *
         * @param bitmap Source bitmap
         * @param maxSize the maxsize after compressing, KB
         * @return bitmap
         */
        fun compressByQuality(bitmap: Bitmap, maxSize: Int): Bitmap {
            val baos = ByteArrayOutputStream()
            var quality = 100
            bitmap.compress(CompressFormat.JPEG, quality, baos)
            Log.d(TAG, "the size before compressing：" + baos.toByteArray().size + "byte")
            var isCompressed = false
            while (baos.toByteArray().size / 1024 > maxSize) {
                quality -= 10
                baos.reset()
                bitmap.compress(CompressFormat.JPEG, quality, baos)
                Log.d(TAG, "quality:" + quality + "%, size："
                        + baos.toByteArray().size + "byte")
                isCompressed = true
            }
            Log.d(TAG, "the size after compressing：" + baos.toByteArray().size + "byte")
            if (isCompressed) {
                val compressedBitmap = BitmapFactory.decodeByteArray(
                        baos.toByteArray(), 0, baos.toByteArray().size)
                bitmap.recycle()
                return compressedBitmap
            } else {
                return bitmap
            }
        }
    }
}
