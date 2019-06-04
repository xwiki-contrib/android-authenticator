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
package org.xwiki.android.sync.rest

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log

import org.xwiki.android.sync.utils.ImageUtils

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.nio.charset.Charset

import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import rx.Observable
import rx.subjects.PublishSubject

/**
 * This manager can be used for downloading and managing photos
 *
 * @version $Id: 7189ad448a42e0b6afb12511234adb1f1213682a $
 */
class XWikiPhotosManager
/**
 * @param client will be set to [.client]
 * @param baseUrl will be set to [.baseUrl]
 */
(
        /**
         * Client context
         */
        private val client: OkHttpClient,
        /**
         * Requests base url
         */
        private val baseUrl: String) {

    /**
     * Download avatar from XWiki and prepare it by [.prepareAvatar].
     *
     * @param name username
     * @param avatarName user avatar name (identifier)
     * @return Object which can be used for subscribe to get avatar bytes
     */
    fun downloadAvatar(
            name: String,
            avatarName: String
    ): Observable<ByteArray>? {
        var request: Request? = null
        try {
            request = Request.Builder()
                    .url(baseUrl + "bin/download/XWiki/"
                            + URLEncoder.encode(name, "UTF-8") + "/"
                            + URLEncoder.encode(avatarName, "UTF-8"))
                    .build()
        } catch (e: UnsupportedEncodingException) {
            Log.e(
                    XWikiPhotosManager::class.java.simpleName,
                    "Can't encode user data for getting his avatar",
                    e
            )
            return null
        }

        val subject = PublishSubject.create<ByteArray>()

        client.newCall(request!!).enqueue(object : Callback {
            override fun onFailure(request: Call, e: IOException) {
                println("request failed: " + e.message)

                subject.onError(e)
            }

            @Throws(IOException::class)
            override fun onResponse(request: Call, response: Response) {

                if (response.code() < 200 || response.code() > 209) {
                    if (response.code() == 404) {
                        return
                    }
                    throw IOException("Response with error: $response")
                }

                val avatarBytes = prepareAvatar(
                        response.body()!!.bytes()
                )

                subject.onNext(avatarBytes)
            }
        })

        return subject
    }

    /**
     * Provide downloading of captcha and sending on returned observable
     *
     * @return Object which can be used for subscribe to get avatar bytes
     */
    fun downloadCaptcha(): Observable<ByteArray> {
        var captchaUrl = baseUrl + "bin/imagecaptcha/XWiki/Registration"
        if (captchaUrl.contains("www.xwiki.org")) {
            captchaUrl = baseUrl + "bin/imagecaptcha/XWiki/RealRegistration"
        }

        val request = Request.Builder()
                .url(captchaUrl)
                .build()


        val subject = PublishSubject.create<ByteArray>()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(request: Call, e: IOException) {
                println("request failed: " + e.message)

                subject.onError(e)
            }

            @Throws(IOException::class)
            override fun onResponse(request: Call, response: Response) {

                if (response.code() < 200 || response.code() > 209) {
                    throw IOException("Response with error: $response")
                }

                subject.onNext(response.body()!!.bytes())
            }
        })

        return subject
    }

    /**
     * Prepare downloaded avatar, resize and scale it to use less image.
     *
     * @param bytes bytes of downloaded avatar
     * @return resized and scaled avatar
     *
     * @since 0.4
     */
    private fun prepareAvatar(bytes: ByteArray): ByteArray? {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        var avatar: Bitmap? = null
        //calc if the avatar bitmap memory are more than 4096 * 1024 B = 4MB
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        options.inJustDecodeBounds = false
        options.inPreferredConfig = Bitmap.Config.RGB_565
        options.inSampleSize = 1
        //if the memory size of the image are more than 4M options.inSampleSize>1.
        val size = height * width * 2 //2 is RGB_565
        val reqSize = 4096 * 1024
        if (size > reqSize) {
            val halfSize = size / 2
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while (halfSize / inSampleSize > reqSize) {
                inSampleSize *= 2
            }
            options.inSampleSize = inSampleSize
        }
        avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
        //ensure < 1M.  avoid transactionException when storing in local database.
        avatar = ImageUtils.compressByQuality(avatar!!, 960)
        // Take the image we received from the server, whatever format it
        // happens to be in, and convert it to a JPEG image. Note: we're
        // not resizing the avatar - we assume that the image we get from
        // the server is a reasonable size...
        //return byte[] value
        if (avatar == null) return null
        val convertStream = ByteArrayOutputStream()
        avatar.compress(Bitmap.CompressFormat.PNG, 100, convertStream)
        try {
            convertStream.flush()
        } catch (e: IOException) {
            Log.e(javaClass.simpleName, "Can't flush input stream", e)
        }

        //it's important to call recycle on bitmaps
        avatar.recycle()
        return convertStream.toByteArray()
    }
}
