package org.xwiki.android.authenticator.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.xwiki.android.authenticator.R;


/**
 * A login screen that offers login via email/password.
 */
public class SignUpActivity extends AppCompatActivity {

    // UI references.
    private EditText mFullNameView;
    private EditText mEmailView;
    private EditText mCellPhoneView;
    private EditText mWorkPhoneView;

    private TextView mContactInfoTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_edit_contact);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        StatusBarColorCompat.compat(this, Color.parseColor("#0077D9"));

        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        mFullNameView = (EditText) findViewById(R.id.fullname);
        mCellPhoneView = (EditText) findViewById(R.id.cellphone);
        mWorkPhoneView = (EditText) findViewById(R.id.workphone);

//        final Uri mUri = getIntent().getData();
        mContactInfoTextView = (TextView) findViewById(R.id.contactinfo);

//        fetchContacts(mUri);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.sign_up, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId()==android.R.id.home){
            finish();
        }else if(item.getItemId()==R.id.action_save){
            boolean flag=save();
            if(flag==true) {
                finish();
            }else{
                Toast.makeText(SignUpActivity.this,"please check again",Toast.LENGTH_SHORT).show();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private boolean save(){
        return true;
    }


}

