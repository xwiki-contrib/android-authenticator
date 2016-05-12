package org.xwiki.android.authenticator.activities;

import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xwiki.android.authenticator.Constants;
import org.xwiki.android.authenticator.R;
import org.xwiki.android.authenticator.auth.AuthenticatorActivity;
import org.xwiki.android.authenticator.utils.StatusBarColorCompat;
import org.xwiki.android.authenticator.utils.StringUtils;


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
            //Toast.makeText(SignUpActivity.this,"please check again",Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

}

