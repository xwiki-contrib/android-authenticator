package org.xwiki.android.authenticator.activities.editcontact;

import org.xwiki.android.authenticator.activities.base.MVPView;

import okhttp3.ResponseBody;
import retrofit2.Response;


public interface EditContactMvpView extends MVPView {

    void showContactUpdateSuccessfully();

    void showErrorOnUpdatingContact();

    void showLoginSuccessfully(Response<ResponseBody> response);

    void showErrorLogin();

    void onProgressDialogCancel();
}
