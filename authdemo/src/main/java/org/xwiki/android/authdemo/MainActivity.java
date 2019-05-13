package org.xwiki.android.authdemo;

import android.accounts.*;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Response;

import java.io.IOException;
import java.util.List;

/**
 * MainActivity
 */
public class MainActivity extends AppCompatActivity {

    private static final String STATE_DIALOG = "state_dialog";
    private static final String STATE_INVALIDATE = "state_invalidate";

    private String TAG = this.getClass().getSimpleName();
    private AccountManager mAccountManager;
    private AlertDialog mAlertDialog;
    private boolean mInvalidate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        mAccountManager = AccountManager.get(this);

        ((TextView) findViewById(R.id.signature_textview)).setText("Signature="+SystemTools.getSign(this, getPackageName())+"\nPackageName="+getPackageName());

        findViewById(R.id.btnAddAccount).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addNewAccount(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        });

        findViewById(R.id.btnGetAuthToken).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountPicker(Constants.AUTHTOKEN_TYPE_FULL_ACCESS, false);
            }
        });

        findViewById(R.id.btnGetAuthTokenConvenient).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTokenForAccountCreateIfNeeded(Constants.ACCOUNT_TYPE, Constants.AUTHTOKEN_TYPE_FULL_ACCESS);
            }
        });
        findViewById(R.id.btnInvalidateAuthToken).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAccountPicker(Constants.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            }
        });
        findViewById(R.id.btnConfirmCredentials).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Account account = new Account("fitz", Constants.ACCOUNT_TYPE);
                confirmCredentials(account);
            }
        });

        if (savedInstanceState != null) {
            boolean showDialog = savedInstanceState.getBoolean(STATE_DIALOG);
            boolean invalidate = savedInstanceState.getBoolean(STATE_INVALIDATE);
            if (showDialog) {
                showAccountPicker(Constants.AUTHTOKEN_TYPE_FULL_ACCESS, invalidate);
            }
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mAlertDialog != null && mAlertDialog.isShowing()) {
            outState.putBoolean(STATE_DIALOG, true);
            outState.putBoolean(STATE_INVALIDATE, mInvalidate);
        }
    }

    /**
     * Add new account to the account manager
     * @param accountType
     * @param authTokenType
     */
    private void addNewAccount(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.addAccount(accountType, authTokenType, null, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    showMessage("Account was created");
                    Log.d(TAG, "AddNewAccount Bundle is " + bnd);

                } catch (Exception e) {
                    e.printStackTrace();
                    if(e.getMessage().equals("bind failure")){
                        showAlertBox();
                    }
                }
            }
        }, null);
    }

    private void showAlertBox(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.app_not_found));
        builder.setMessage(getString(R.string.click_continue_to_install_app));
        builder.setCancelable(true);
        builder.setPositiveButton(getString(R.string.positive_button_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                openAppMarket(MainActivity.this);

            }
        });
        builder.setNegativeButton(getString(R.string.negative_button_text), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();
    }

    /**
     * Show all the accounts registered on the account manager. Request an auth token upon user select.
     * @param authTokenType
     */
    private void showAccountPicker(final String authTokenType, final boolean invalidate) {
        mInvalidate = invalidate;
        final Account availableAccounts[] = mAccountManager.getAccountsByType(Constants.ACCOUNT_TYPE);

        if (availableAccounts.length == 0) {
            Toast.makeText(this, "No accounts", Toast.LENGTH_SHORT).show();
        } else {
            String name[] = new String[availableAccounts.length];
            for (int i = 0; i < availableAccounts.length; i++) {
                name[i] = availableAccounts[i].name;
            }

            // Account picker
            mAlertDialog = new AlertDialog.Builder(this).setTitle("Pick Account").setAdapter(new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, name), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if(invalidate)
                        invalidateAuthToken(availableAccounts[which], authTokenType);
                    else
                        getExistingAccountAuthToken(availableAccounts[which], authTokenType);
                }
            }).create();
            mAlertDialog.show();
        }
    }


    private void confirmCredentials(Account account) {
        final AccountManagerFuture<Bundle> future = mAccountManager.confirmCredentials(account, null, this, new AccountManagerCallback<Bundle>() {
            @Override
            public void run(AccountManagerFuture<Bundle> future) {
                try {
                    Bundle bnd = future.getResult();
                    //showMessage((authtoken != null) ? "SUCCESS!\ntoken: " + authtoken : "FAIL");
                    Log.d(TAG, "GetToken Bundle is " + bnd);
                } catch (OperationCanceledException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (AuthenticatorException e) {
                    e.printStackTrace();
                }
            }
        }, null);
    }

    /**
     * Get the auth token for an existing account on the AccountManager
     * @param account
     * @param authTokenType
     */
    private void getExistingAccountAuthToken(Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null, null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    isValidToken(bnd);
                    Log.d(TAG, "GetToken Bundle is " + bnd);
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Invalidates the auth token for the account
     * @param account
     * @param authTokenType
     */
    private void invalidateAuthToken(final Account account, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthToken(account, authTokenType, null, this, null,null);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Bundle bnd = future.getResult();
                    String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
                    String server = bnd.getString(Constants.SERVER_ADDRESS);
                    mAccountManager.invalidateAuthToken(account.type, authToken);
                    showMessage(account.name + " invalidated");
                } catch (Exception e) {
                    e.printStackTrace();
                    showMessage(e.getMessage());
                }
            }
        }).start();
    }

    /**
     * Get an auth token for the account.
     * If not exist - add it and then return its auth token.
     * If one exist - return its auth token.
     * If more than one exists - show a picker and return the select account's auth token.
     * @param accountType
     * @param authTokenType
     */
    private void getTokenForAccountCreateIfNeeded(String accountType, String authTokenType) {
        final AccountManagerFuture<Bundle> future = mAccountManager.getAuthTokenByFeatures(accountType, authTokenType, null, this, null, null,
                new AccountManagerCallback<Bundle>() {
                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        Bundle bnd = null;
                        try {
                            bnd = future.getResult();
                            isValidToken(bnd);
                            Log.d(TAG, "GetTokenForAccount Bundle is " + bnd);
                        } catch (Exception e) {
                            e.printStackTrace();
                            showMessage(e.getMessage());
                        }
                    }
                }
                , null);
    }

    private void isValidToken(Bundle bnd) throws IOException {
        final String authToken = bnd.getString(AccountManager.KEY_AUTHTOKEN);
        String url = bnd.getString(Constants.SERVER_ADDRESS);
        if(authToken == null || url == null){
            String errorMessage = bnd.getString(AccountManager.KEY_ERROR_MESSAGE);
            showMessage("FAIL! getAuthToken error message=" + errorMessage);
            return;
        }
        XWikiHttp.isValidToken(url, authToken, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }
            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if(response.isSuccessful()){
                    showMessage("valid token! token="+ authToken);
                }else{
                    showMessage("invalid token!!! statusCode="+ response.code() + ", token="+ authToken);
                }
                response.close();
            }
        });
    }

    private void showMessage(final String msg) {
        if (TextUtils.isEmpty(msg))
            return;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getBaseContext(), msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Open market with application page.
     *
     * @param context Context to know where from to open market
     */
    private static void openAppMarket(Context context) {
        Intent rateIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + "org.xwiki.android.sync"));
        boolean marketFound = false;
        // find all applications able to handle our rateIntent
        final List<ResolveInfo> otherApps = context.getPackageManager().queryIntentActivities(rateIntent, 0);
        for (ResolveInfo otherApp: otherApps) {
            // look for Google Play application
            if (otherApp.activityInfo.applicationInfo.packageName.equals("com.android.vending")) {
                ActivityInfo otherAppActivity = otherApp.activityInfo;
                ComponentName componentName = new ComponentName(
                        otherAppActivity.applicationInfo.packageName,
                        otherAppActivity.name
                );
                rateIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                rateIntent.setComponent(componentName);
                context.startActivity(rateIntent);
                marketFound = true;
                break;
            }
        }
        // if GooglePlay not present on device, open web browser
        if (!marketFound) {
            Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id="+context.getPackageName()));
            context.startActivity(webIntent);
        }
    }
}
