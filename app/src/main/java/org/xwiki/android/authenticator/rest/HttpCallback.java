package org.xwiki.android.authenticator.rest;

import android.os.Handler;
import android.os.Looper;

/**
 * @author fitz
 * @version 1.0
 */
public abstract class HttpCallback {

	public Handler handler;

	public HttpCallback(){
		handler=new Handler(Looper.getMainLooper());
	}

	public void postSuccess(final Object obj){
		handler.post(new Runnable() {
			@Override
			public void run() {
				onSuccess(obj);
			}
		});
	}
	
	public void postFailure(final String msg){
		handler.post(new Runnable() {
			@Override
			public void run() {
				onFailure(msg);
			}
		});
	}

    public void onSuccess(Object obj) {}

    public void onFailure(String msg) {}

}
