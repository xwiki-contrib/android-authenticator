package org.xwiki.android.authenticator.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;
import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.bean.XWikiUser;
import org.xwiki.android.authenticator.rest.XWikiHttp;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;

import java.io.IOException;


/**
 * A sign up screen that offers email/password/username/lastname/firstname/
 * or maybe need captcha.
 */
public class SignUpActivity extends AppCompatActivity {

    // UI references.
    private EditText mFirstNameView;
    private EditText mLastNameView;
    private EditText mUserIdEditText;
    private EditText mPasswordEditText;
    private EditText mConfirmPasswordEditText;
    private EditText mEmailView;
    private EditText mCellPhoneView;
    private EditText mCaptchaEditText;
    private ImageView mCaptchaImageView;


    String formToken = null;
    String userId = null;
    String password = null;
    String confirmPassword = null;
    String captcha = null;

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
        mUserIdEditText = (EditText) findViewById(R.id.user_id_edit);
        mPasswordEditText = (EditText) findViewById(R.id.password);
        mConfirmPasswordEditText = (EditText) findViewById(R.id.confirm_password);
        mCaptchaImageView = (ImageView) findViewById(R.id.captcha_image);
        mCaptchaEditText = (EditText) findViewById(R.id.captcha_edit);

        mCaptchaImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initData();
            }
        });

        initData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        } else if (item.getItemId() == R.id.action_save) {
            signUp();
        }
        return super.onOptionsItemSelected(item);
    }



    void initData() {
        new AsyncTask<String, String, Boolean>() {
            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    formToken = XWikiHttp.signUpInitCookieForm();
                    return formToken == null ? false : true;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Boolean flag) {
                if (flag == null) {
                    Toast.makeText(SignUpActivity.this, "init form network error", Toast.LENGTH_SHORT).show();
                } else if (!flag) {
                    Toast.makeText(SignUpActivity.this, "init form error", Toast.LENGTH_SHORT).show();
                }else{
                    refreshCaptcha();
                }
            }
        }.execute();
    }


    //0:false, 1:true, null:network error, 2:the user exists.
    public void signUp() {
        if (!checkInput()) return;

        new AsyncTask<String, String, Integer>() {
            @Override
            protected Integer doInBackground(String... params) {
                //found whether the user exists.
                XWikiUser userFind = null;
                try {
                    userFind = XWikiHttp.getUserDetail("xwiki", "XWiki", userId);
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                } catch (XmlPullParserException e) {
                    e.printStackTrace();
                    return null;
                }
                if (userFind != null) return 2;

                try {
                    boolean signUpFlag = XWikiHttp.signUp(userId, password, formToken, captcha);
                    return signUpFlag ? 1 : 0;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            protected void onPostExecute(Integer status) {
                if (status == null) {
                    Toast.makeText(SignUpActivity.this, "network error", Toast.LENGTH_SHORT).show();
                } else if (status == 2) {
                    Toast.makeText(SignUpActivity.this, "the user exists", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SignUpActivity.this, status == 1 ? "success!" : "fail!", Toast.LENGTH_SHORT).show();
                    if (status == 0) {
                        refreshCaptcha();
                    } else {
                        finishSignUp();
                    }
                }
            }
        }.execute();
    }

    void finishSignUp() {
        Bundle data = new Bundle();
        data.putString(AccountManager.KEY_ACCOUNT_NAME, userId);
        data.putString(AccountManager.KEY_ACCOUNT_TYPE, Constants.ACCOUNT_TYPE);
        //data.putString(AccountManager.KEY_AUTHTOKEN, "tokentoken");
        data.putString(AuthenticatorActivity.PARAM_USER_SERVER, XWikiHttp.getServerRequestUrl());
        data.putString(AuthenticatorActivity.PARAM_USER_PASS, password);
        Intent intent = new Intent();
        intent.putExtras(data);
        setResult(RESULT_OK, intent);
        finish();
    }

    void refreshCaptcha() {
        new AsyncTask<String, String, byte[]>() {
            @Override
            protected byte[] doInBackground(String... params) {
                //String captchaUrl = "http://210.76.192.253:8080/xwiki/bin/imagecaptcha/XWiki/Registration";
                //String url = "http://www.xwiki.org/xwiki/bin/imagecaptcha/XWiki/RealRegistration";
                String captchaUrl = "http://" + XWikiHttp.getServerRequestUrl() + "/xwiki/bin/imagecaptcha/XWiki/Registration";
                if (captchaUrl.contains("www.xwiki.org")) {
                    captchaUrl = "http://" + XWikiHttp.getServerRequestUrl() + "/xwiki/bin/imagecaptcha/XWiki/RealRegistration";
                }
                byte[] img = XWikiHttp.downloadAvatar(captchaUrl);
                return img;
            }

            @Override
            protected void onPostExecute(byte[] bytes) {
                Bitmap captchaBitmap = getPicFromBytes(bytes, null);
                mCaptchaImageView.setImageBitmap(captchaBitmap);
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


    /**
     * Attempts to check input
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no server checking is made.
     */
    private boolean checkInput() {
        // Reset errors.
        mUserIdEditText.setError(null);
        mPasswordEditText.setError(null);
        mConfirmPasswordEditText.setError(null);
        mCaptchaEditText.setError(null);

        // Store values at the time of the login attempt.
        userId = mUserIdEditText.getText().toString();
        password = mPasswordEditText.getText().toString();
        confirmPassword = mConfirmPasswordEditText.getText().toString();
        captcha = mCaptchaEditText.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid email address.
        if (TextUtils.isEmpty(userId)) {
            mUserIdEditText.setError(getString(R.string.error_field_required));
            focusView = mUserIdEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(password) || password.length() < 6) {
            mPasswordEditText.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(confirmPassword) || !confirmPassword.equals(password)) {
            mConfirmPasswordEditText.setError(getString(R.string.error_confirm_password));
            focusView = mConfirmPasswordEditText;
            cancel = true;
        } else if (TextUtils.isEmpty(captcha)) {
            mCaptchaEditText.setError(getString(R.string.error_invalid_captcha));
            focusView = mCaptchaEditText;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
            return false;
        } else {
            //TODO Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            return true;
        }
    }

}

