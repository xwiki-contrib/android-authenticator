# TODO List
What's left and things to improve:
* android permission request and check 6.0 (TODO)
* security issues (TODO)
* show login process dialog (TODO)
* large volume test (TODO)
* optimize query performance (TODO)
* json/xml or custom http/okhttp/volley (TODO)
* admin create users and delete users. (TODO)
* Security issues (TODO)
*Large capacity performance (TODO)


# DONE Function List

###UI Design (DONE)
* material design(>=4.4), normal(4.1~4.3)
* activities
```
AuthenticatorActivity for login and account adding.
SignUpActivity for register an account.
EditContactActivity for edit and modify contact if permissions allowed
    getRawContactId -> getXWikiUser -> fill in the blank textview -> when saving, check permission and admin according to updating user's response.
```

### Restful Api (DONE)
* A HttpRequest and HttpResponse Api (DONE)
* sign in and reuse global http cookies (DONE)
* edit and update the contacts in server and local database (DONE)
* solr search for example: (Doing)
```
* Get All Users:(DONE)
http://www.xwiki.org/xwiki/rest/wikis/query?q=object:XWiki.XWikiUsers
default number<=10
http://www.xwiki.org/xwiki/rest/wikis/query?q=object:XWiki.XWikiUsers&number=100
* Get User Information(DONE)
for example XWiki.LudovicDubost
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/LudovicDubost
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/LudovicDubost/objects/XWiki.XWikiUsers/0/properties
* Get Groups(DONE)
http://www.xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiGroups&number=20
* Get User From Group(DONE)
for example XWikiAdminGroup:
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects
http://www.xwiki.org/xwiki/rest/wikis/contrib/spaces/XWiki/pages/XWikiAdminGroup/objects/XWiki.XWikiGroups/
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects/XWiki.XWikiGroups
```

### Service Synchronization SyncAdapter  (DONE)
* OnPerfermSync

```
(1)
Get All Users[A] from xwiki-server. Or we will never know which one has been removed.
The Contacts in the phone are [B].
Update or add [B] according [A].
[B-A] are the contacts that should be removed in the phone.
(2)
Get users [A] that have been modified. (Add Update)
The Contacts in the phone are [B].
[B-A] represents the contacts that have not been modified and that have been removed.
One by One checking which should be deleted in [B-A].
(3)
Delete All Users.
Get all users from the server, insert into the phone.
(4)
Get users that have been modified from the lastModifiedDate. (Add or Update)
Get All users that are [A].   The Contacts in the phone are [B].
[B-A] are the contacts that should be removed.

Now (4) is my choice to update,add and delete contacts. Need to be improved!

```


### Service Authenticator (DONE)
```
1.session id
It may be the best and simple choice because user app just gets sessionid
from the authenticator and doesn't need to know username and passwd.

2.Basic Auth
the user app use the BASIC auth header(Base64) to authenticate with
XWiki-server.

3.A preconfigured httpclient/httpurlconnection


Now (1) is my choice. to be tested and improved!

```


### Local contact manager (DONE)
* ContactManager
```
addNewCotact
updateContact
getXWikiUser(rawContactId)
lookupRawContact(serverId)
```

### ideas for third-party apps
* authenticator ideas

```
http://blog.udinic.com/2013/04/24/write-your-own-android-authenticator/
http://www.slideshare.net/freesamael/inside-the-android-accountmanager
http://stackoverflow.com/questions/14437096/shouldnt-android-accountmanager-store-oauth-tokens-on-a-per-app-uid-basis
http://stackoverflow.com/questions/31915642/android-login-account-authenticator-vs-manual-authentication
http://developer.android.com/reference/android/accounts/AccountManager.html
http://stackoverflow.com/questions/6852256/how-do-you-force-accountmanager-to-show-the-access-request-screen-after-a-user
http://stackoverflow.com/questions/3965126/android-google-calendar-permission-issue
http://stackoverflow.com/questions/14147588/android-custom-authenticator-allow-access-screen
http://stackoverflow.com/questions/20527528/show-fullscreen-access-request-dialog-instead-of-notification-when-using-getau
https://garbagecollected.org/2014/09/14/how-google-authenticator-works/
Allow Access screen
```


* exception:

```
SecurityException: Activity to be started with KEY_INTENT must share Authenticator's signatures
android:customTokens="true" in authenticator.xml

http://androidxref.com/6.0.1_r10/xref/frameworks/base/services/core/java/com/android/server/accounts/AccountManagerService.java#1858

/MainActivity: accountType=org.xwiki.android.authenticator, authTokenType=Full accesscn.dacas.leef.authdemo2
05-01 14:26:30.793 1694-2459/? D/audio_hw_primary: out_set_parameters: enter: usecase(1: low-latency-playback) kvpairs: routing=2
05-01 14:26:30.800 2105-2939/? W/Binder: Caught a RuntimeException from the binder stub implementation.
                                         java.lang.SecurityException: Activity to be started with KEY_INTENT must share Authenticator's signatures
                                             at com.android.server.accounts.AccountManagerService$Session.onResult(AccountManagerService.java:3104)
                                             at com.android.server.accounts.AccountManagerService$6.onResult(AccountManagerService.java:2008)
                                             at com.android.server.accounts.AccountManagerService$6.onResult(AccountManagerService.java:1966)
                                             at android.accounts.IAccountAuthenticatorResponse$Stub.onTransact(IAccountAuthenticatorResponse.java:59)
                                             at android.os.Binder.execTransact(Binder.java:453)
```



* how do third-party apps access user authenticator to getCredential or getAuthToken?
* how do the authenticator authenticate third-party apps and check if they have the permission to getAuthToken?

```
1. getAuthToken At first time
Bundle option should include app_id and app_key and so on. Then the authenticator return AuthenticatorActivity
to the third-party app and record if this app get the permission. But Maybe it's not safe enough because the
app_id and app_key can possibly be forged.

2. if not getting the permission or signing in successfully, go to 1 steps again or go to 3.

3. getAuthToken after getting the permission.
Bundle option in third-party apps also should include app_id and app_key and so on. The authenticator return
the token directly when receiving the request. if the app finds that the token getting from the authenticator
has been expired, invalidateAuthToken and getAuthToken again.

```

* advantages for third-party apps

```
1. no need to store the username and password
2. no need to manage the account
3. just login once, never login again.
```

* why does token request at first time fail
```
Your first request for an auth token might fail for several reasons:
An error in the device or network caused AccountManager to fail.
The user decided not to grant your app access to the account.
The stored account credentials aren't sufficient to gain access to the account.
The cached auth token has expired.
https://stuff.mit.edu/afs/sipb/project/android/docs/training/id-auth/authenticate.html
https://stuff.mit.edu/afs/sipb/project/android/docs/training/id-auth/authenticate.html#RequestAgain
https://github.com/Udinic/AccountAuthenticator/blob/master/ExampleApp2/src/com/udinic/accounts_example2/Main2.java
https://support.google.com/accounts/answer/3466521?hl=en
```


* Test automation. (DONE)
```
http://evgenii.com/blog/testing-activity-in-android-studio-tutorial-part-1/
http://evgenii.com/blog/testing-activity-in-android-studio-tutorial-part-2/
Espresso library Test
http://www.jianshu.com/p/03118c11c199
```
* Maven or Gradle in Jenkins (DONE)

```
try to master Maven, JUnit, Selenium, component driven development
Continue fixing a few small issues, chosen so that they are related to
your project.
Pick from the (non-comprehensive) list of easy issues
https://jenkins.io/solutions/android/
http://ainoya.io/docker-android-walter
https://www.digitalocean.com/community/tutorials/how-to-build-android-apps-with-jenkins
http://blog.saymagic.cn/2016/01/25/docker-image-for-android.html?utm_source=tuicool&utm_medium=referral
```

* sign up (DONE)
* group select (DONE)
* Test Test Test...
* Code Comment (DONE)
* DOC (DONE)