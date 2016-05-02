# Android-authenticator

The idea of this project is to integrate a wiki instance in Android accounts, mainly including the synchronization of contacts and the XWiki authenticator. By synchronizing contacts of your company on your phone, it's easy to communicate and collaborate with each other. And the authenticator can also be used to provide credentials for other android apps. 

#Features
There're mainly the following key tasks, such as restful http connector, authenticator service, synchronization service, large capacity performance and security issues. 
![](https://raw.githubusercontent.com/fitzlee/androidsync/master/doc/process.jpg)

### Synchronization of contacts
* Server-to-Client (download)

```
SyncAdapter.onPerformSync()
1 XWiki.getUserList.  get all users that have been modified after lastmodifiedtime from server. 
2 ContactManager.UpdateContacts. (add update delete the local database)
    2.1 add users if lookupRawContact return false;
    2.2 update users if lookupRawContact return true;
    2.3 delete users
          2.3.1  XWikiHttp.getAllUserMap from server as A.
          2.3.2  getAllContactsIdMap from local database as B.
          2.3.3 delete the user [B-A] that is in B but not in A. 
```
          
* Client-to-Server (upload)

```
EditContactActivity.updateContact()
1 check if input values if valid
2 Request to update contact in server
   2.1 If having no permission, give up.
   2.2 If having permission, update local database 
3 NOTE: Update server first, if the response is ok, then update local database.
```


### Providing credentials for other android apps
Things to improve before being reused by other Android applications:
* right now the authenticator store and provide the password in clear because that's what REST access need since XWiki itself does not provide any token based access by default

* Main classes and functions

```
Authenticator
AuthenticatorActivity
XWikiAuthenticator
        getAuthToken()
        invalideAuthToken
        addNewAccount
XWikiAuthenticatorService
AccountManager
AccountManagerService   
```

* Process

```
1. ask for Permission Granting.  If permission granted, store in preference.
2. after passing 1 step,  peekAuthToken to get an cached token for different AuthTokenType(FULL_ACESS+PackageName).  If get a token that is not null, return.
3. if step 2 fails,  ask XWiki-server for a new token. If failed return network error.
```

* Note: for third-party apps, there are mainly the following apis:

```
getAuthToken/getAuthTokenByFeatures
    if AccountManager cached the token(using mAccountManager.setAuthToken(, authTokenType, authToken)), then 
    it will directly return the cached token and XWikiAuthenticator.getAuthToken have no use.
    if the third-party app use invalideAuthToken(, AuthTokenType, authToken), then cache will be clear and 
    XWikiAuthenticator.getAuthToken will be called to retrieve a new token from server.
    AuthTokenType = (AUTHTOKEN_TYPE_FULL_ACCESS + packageName) because if all the authTokenTypes are the same,
    other apps's calling getAuthToken will be bypassed because of the cached <AuthTokenType,token>.  To avoid 
    forgery of AuthTokenType(using other's packaName), we can check the packageName in getAuthToken using 
    options.getInt(AccountManager.KEY_CALLER_UID).

invalideAuthToken
    Generally, only when we find the authToken getting from getAuthToken function was expired, we will invalide the token and let go to authenticator.getAuthToken

AddNewContact/confirmCredentials

```    

* Note: for the authenticator, there are mainly the following tasks:

```
     how to refresh token-cookie-sessionId when users login again and the token is expired. 
     how to grant permission, server request or local comparing ? 
     how to store password securely, encrypt? but where the key stores?
```     
     
#####Security issues

#####Large capacity performance


# TODO List
What's left and things to improve:
* android permission request and check 6.0 (TODO)
* Test automation. (TODO)
```
http://evgenii.com/blog/testing-activity-in-android-studio-tutorial-part-1/
http://evgenii.com/blog/testing-activity-in-android-studio-tutorial-part-2/
Espresso library Test
http://www.jianshu.com/p/03118c11c199  
```
* Maven or Gradle in Jenkins (TODO)

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

* sign up (TODO)
* admin create users and delete users. (TODO)
* group select (TODO)
* security issues (TODO)
* show login process dialog (TODO)
* large volume test (TODO)
* optimize query performance (TODO)
* json/xml or custom http/okhttp/volley (TODO)
* Test Test Test... 
* Code Comment (TODO)
* DOC (TODO)
* Questions & Ideas
```
The function, XWikiHttp.getUserList is too slow. Maybe json is faster than xml. To be optimized and tested.
```

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