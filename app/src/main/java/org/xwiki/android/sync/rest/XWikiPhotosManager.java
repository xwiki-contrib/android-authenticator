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
package org.xwiki.android.sync.rest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.Nullable;
import android.util.Log;
import androidx.annotation.NonNull;
import okhttp3.*;
import org.xwiki.android.sync.utils.ImageUtils;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

/**
 * This manager can be used for downloading and managing photos
 *
 * @version $Id$
 */
public class XWikiPhotosManager {

    /**
     * Client context
     */
    private final OkHttpClient client;

    /**
     * Requests base url
     */
    private final String baseUrl;

    /**
     * @param client will be set to {@link #client}
     * @param baseUrl will be set to {@link #baseUrl}
     */
    public XWikiPhotosManager(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    /**
     * Download avatar from XWiki and prepare it by {@link #prepareAvatar(byte[])}.
     *
     * @param name username
     * @param avatarName user avatar name (identifier)
     * @return Object which can be used for subscribe to get avatar bytes
     */
    @Nullable
    public Observable<byte[]> downloadAvatar(
        @NonNull String name,
        @NonNull String avatarName
    ) {
        Request request = null;
        try {
            request = new Request.Builder()
                .url(baseUrl + "bin/download/XWiki/"
                    + URLEncoder.encode(name, "UTF-8") + "/"
                    + URLEncoder.encode(avatarName, "UTF-8"))
                .build();
        } catch (UnsupportedEncodingException e) {
            Log.e(
                XWikiPhotosManager.class.getSimpleName(),
                "Can't encode user data for getting his avatar",
                e
            );
            return null;
        }

        final PublishSubject<byte[]> subject = PublishSubject.create();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call request, IOException e) {
                System.out.println("request failed: " + e.getMessage());

                subject.onError(e);
            }

            @Override
            public void onResponse(Call request, Response response) throws IOException {

                if (response.code() < 200 || response.code() > 209) {
                    if (response.code() == 404) {
                        return;
                    }
                    throw new IOException("Response with error: " + response.toString());
                }

                byte[] avatarBytes = prepareAvatar(
                    response.body().bytes()
                );

                subject.onNext(avatarBytes);
            }
        });

        return subject;
    }

    /**
     * Provide downloading of captcha and sending on returned observable
     *
     * @return Object which can be used for subscribe to get avatar bytes
     */
    public Observable<byte[]> downloadCaptcha() {
        String captchaUrl = baseUrl + "bin/imagecaptcha/XWiki/Registration";
        if (captchaUrl.contains("www.xwiki.org")) {
            captchaUrl = baseUrl + "bin/imagecaptcha/XWiki/RealRegistration";
        }

        Request request = new Request.Builder()
            .url(captchaUrl)
            .build();


        final PublishSubject<byte[]> subject = PublishSubject.create();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call request, IOException e) {
                System.out.println("request failed: " + e.getMessage());

                subject.onError(e);
            }

            @Override
            public void onResponse(Call request, Response response) throws IOException {

                if (response.code() < 200 || response.code() > 209) {
                    throw new IOException("Response with error: " + response.toString());
                }

                subject.onNext(response.body().bytes());
            }
        });

        return subject;
    }

    /**
     * Prepare downloaded avatar, resize and scale it to use less image.
     *
     * @param bytes bytes of downloaded avatar
     * @return resized and scaled avatar
     *
     * @since 0.4
     */
    private byte[] prepareAvatar(byte[] bytes) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        Bitmap avatar = null;
        //calc if the avatar bitmap memory are more than 4096 * 1024 B = 4MB
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inSampleSize = 1;
        //if the memory size of the image are more than 4M options.inSampleSize>1.
        int size = height * width * 2; //2 is RGB_565
        int reqSize = 4096 * 1024;
        if (size > reqSize) {
            final int halfSize = size / 2;
            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfSize / inSampleSize) > reqSize) {
                inSampleSize *= 2;
            }
            options.inSampleSize = inSampleSize;
        }
        avatar = BitmapFactory.decodeByteArray(bytes, 0, bytes.length, options);
        //ensure < 1M.  avoid transactionException when storing in local database.
        avatar = ImageUtils.compressByQuality(avatar, 960);
        // Take the image we received from the server, whatever format it
        // happens to be in, and convert it to a JPEG image. Note: we're
        // not resizing the avatar - we assume that the image we get from
        // the server is a reasonable size...
        //return byte[] value
        if (avatar == null) return null;
        ByteArrayOutputStream convertStream = new ByteArrayOutputStream();
        avatar.compress(Bitmap.CompressFormat.PNG, 100, convertStream);
        try {
            convertStream.flush();
        } catch (IOException e) {
            Log.e(getClass().getSimpleName(), "Can't flush input stream", e);
        }
        //it's important to call recycle on bitmaps
        avatar.recycle();
        return convertStream.toByteArray();
    }
}
