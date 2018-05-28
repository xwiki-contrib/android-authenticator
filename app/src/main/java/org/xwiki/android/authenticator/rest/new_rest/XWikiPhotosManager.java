package org.xwiki.android.authenticator.rest.new_rest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.xwiki.android.authenticator.utils.ImageUtils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import rx.Observable;
import rx.subjects.PublishSubject;

public class XWikiPhotosManager {
    private final OkHttpClient client;
    private final String baseUrl;

    public XWikiPhotosManager(OkHttpClient client, String baseUrl) {
        this.client = client;
        this.baseUrl = baseUrl;
    }

    public Observable<byte[]> downloadAvatar(
        String name,
        String avatarName
    ) {
        Request request = new Request.Builder()
            .url(baseUrl + "bin/download/XWiki/" + name + "/" + avatarName)
            .build();

        final PublishSubject<byte[]> subject = PublishSubject.create();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call request, IOException e) {
                System.out.println("request failed: " + e.getMessage());

                subject.onError(e);
                subject.onCompleted();
            }

            @Override
            public void onResponse(Call request, Response response) throws IOException {

                if (response.code() < 200 || response.code() > 209) {
                    throw new IOException("Response with error: " + response.toString());
                }

                byte[] avatarBytes = prepareAvatar(
                    response.body().bytes()
                );

                subject.onNext(avatarBytes);
                subject.onCompleted();
            }
        });

        return subject;
    }

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
                subject.onCompleted();
            }

            @Override
            public void onResponse(Call request, Response response) throws IOException {

                if (response.code() < 200 || response.code() > 209) {
                    throw new IOException("Response with error: " + response.toString());
                }

                subject.onNext(response.body().bytes());
                subject.onCompleted();
            }
        });

        return subject;
    }

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
