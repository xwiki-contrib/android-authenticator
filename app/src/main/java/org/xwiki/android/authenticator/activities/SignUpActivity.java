package org.xwiki.android.authenticator.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.Image;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.xwiki.android.authenticator.AppContext;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.rest.HttpConnector;
import org.xwiki.android.authenticator.rest.HttpRequest;
import org.xwiki.android.authenticator.rest.HttpResponse;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.SharedPrefsUtil;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;
import org.xwiki.android.authenticator.utils.StringUtils;

import java.io.IOException;


/**
 * A sign up screen that offers email/password/username/lastname/firstname/
 * or maybe need captcha.
 */
public class SignUpActivity extends AppCompatActivity {

    // UI references.
    private EditText mFirstNameView;
    private EditText mEmailView;
    private EditText mCellPhoneView;
    private EditText mLastNameView;
    private ImageView mCaptchaImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_sign_up);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));

        // init view
        mEmailView = (EditText) findViewById(R.id.email);
        mFirstNameView = (EditText) findViewById(R.id.first_name);
        mCellPhoneView = (EditText) findViewById(R.id.cell_phone);
        mLastNameView = (EditText) findViewById(R.id.last_name);
        mCaptchaImageView = (ImageView) findViewById(R.id.captcha_image);
        initData();
    }

    String formToken = null;

    void initData(){
        new AsyncTask<String, String, byte[]>(){
            @Override
            protected byte[] doInBackground(String... params) {
                String registerUrl = "http://192.168.56.1:8080/xwiki/bin/view/XWiki/Registration";
                HttpRequest request = new HttpRequest(registerUrl);
                HttpConnector httpConnector = new HttpConnector();
                HttpResponse response = null;
                try {
                    response = httpConnector.performRequest(request);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                if(response == null) return null;
                int statusCode = response.getResponseCode();
                if (statusCode < 200 || statusCode > 299) {
                    SharedPrefsUtil.putValue(AppContext.getInstance().getApplicationContext(), "Cookie", response.getHeaders().get("Set-Cookie"));
                }
                byte[] contentData = response.getContentData();
                Document document = Jsoup.parse(new String(contentData));
                formToken = document.select("input[name=form_token]").val();

                String url = "http://www.xwiki.org/xwiki/bin/imagecaptcha/XWiki/RealRegistration";
                byte[] img = XWikiHttp.downloadAvatar(url);
                return img;
            }

            @Override
            protected void onPostExecute(byte[] bytes) {
                Bitmap captchaBitmap = getPicFromBytes(bytes, null);
                mCaptchaImageView.setImageBitmap(captchaBitmap);
                super.onPostExecute(bytes);
            }
        }.execute();
    }


    public static Bitmap getPicFromBytes(byte[] bytes,
                                         BitmapFactory.Options opts) {
        if (bytes != null)
            if (opts != null)
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length,
                        opts);
            else
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        return null;
    }

    public void signUp(){
        new AsyncTask<String, String, Boolean>(){
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    XWikiHttp.signUp("asdf", "hahhaha", formToken);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(Boolean aBoolean) {
                super.onPostExecute(aBoolean);
            }
        }.execute();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId()==R.id.action_save){

            signUp();

            /*
            Bundle data = new Bundle();
            data.putString(AccountManager.KEY_ACCOUNT_NAME, "fitz");
            data.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
            data.putString(AccountManager.KEY_AUTHTOKEN, "tokentoken");
            data.putString(AuthenticatorActivity.PARAM_USER_SERVER, "www.xwiki.org");
            data.putString(AuthenticatorActivity.PARAM_USER_PASS, "fitz2xwiki");
            Intent intent = new Intent();
            intent.putExtras(data);
            setResult(RESULT_OK, intent);
            finish();
            */
            //Toast.makeText(SignUpActivity.this,"please check again",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}

