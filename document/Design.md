#Design

There're mainly the following key tasks, such as
- restful http connector
- authenticator service
- synchronization service
- large capacity performance
- security issues

![](https://raw.githubusercontent.com/xwiki-contrib/android-authenticator/master/document/_images/process.jpg)

First introduce the design and implementation of the synchronization and authenticator

###Synchronization:

#####1. Server->Client
There are two choices, including synchronizing all users or synchronizing the users of selected groups. We use the same method for these two cases.

Add Update

Each time while calling the method SyncAdapter.onPerformSync, we get data from server that has been modified since the last modified time. The data that we get must be updated or add to the local contact2 database. Also only these data need to be updated.

Delete

What data should be deleted for the local database?  Because the server does not return data that needs to be deleted, or maybe I don not know how to query the deleted objects? ?. Therefor now I just get all the user IDs(HashMap<id,Ojbect>), traverse every user of the local database, find the data that need be deleted, and then delete it.
   
######1.1 Detail:
* For synchronizing all users:

1)	Get all users as List<SearchResult> searchResults

2)	Add Update: SearchResults include the last modified time. So according to the time, we can filter what data should be updated or added. Then call the function ContactManager.UpdateContacts to update local db. 

3)	Delete: searchResults already have user ids, so we can get the IDs(HashMap<id,Ojbect>), then traverse the local database to find what data should be deleted.

* For synchronizing users of selected groups:

1)	Get all selected group ids from local sharepreference xml,  as List<String> groupIds

2)	Get the users' simple information(ObjectSummary) of each group one by one. The ObjectSummary only has the user's id without the last modified time.

3)	Add Update: According to the user ids, we get the last modified time for each user, if before the given last sync time, continue; if after, we update user (but we need first get the detailed information of the user.). 

4)	Delete: at seconde step, we get ObjectSummarys which include all the user ids. So with these ids, we can find the data that should be deleted.

######1.2 Code Detail
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

#####2. Client->Server

For this part, As we will first update the server while editing the contact, Therefore, unnecessary synchronization mechanism is not required. If the server's response is that the editor has no permission, we return; if has been updated in the server, we will update the local database at the same time.

```
EditContactActivity.updateContact()
1 check if input values if valid
2 Request to update contact in server
   2.1 If having no permission, give up.
   2.2 If having permission, update local database
3 NOTE: Update server first, if the response is ok, then update local database.
```

###Authenticator:

#####1. How to grant the permission for third party apps when they calling getAuthToken? (Here, AuthToken is equal to Cookie.JessionId)

Basically, only 3 useful interfaces, like AddNewAccount, getAuthToken, invalideAuthToken, are available for other third party android apps. The most widely used is getAuthToken. How should we grant permission for third-party apps? And when we grant? If adding XWiki account from one app, then we can trust this app and grant the getAuthToken permission. But if not, we should check the permission for every getAuthToken request of third-party apps.  So the checking logic code should be in the function getAuthToken. But XWikiAuthenticator.getAuthToken will never be called if AcountManager has cached the authtoken corresponding to AuthTokenType. Therefore for first granting permission for third-party apps, the app should not pass the same authTokenType when calling getAuthToken function. Or the authToken value will be directly returned by AcountManager according to the same AuthTokenType and the method getAuthToken will never be called.  So different apps should use the different AuthTokenType param to call the function getAuthToken so that the <AuthTokenType, AuthToken> will not be cached before granting permission for this app. AuthTokenType=FULL_ACCESS+PackageName. So in XWikiAuthenticator.getAuthToken function, if we check that the third-party app has not been granted, we startActivity(GrantPermissionAcvivity) to grant permission for this package by checking the user's input password.
And in addition the packageName can't be forged because we can use the options.getInt(AccountManager.KEY_CALLER_UID) to verify the pakageName.

#####2. How to maintain authToken consistency for different third-party apps with different AuthTokenType?

When will appear inconsistent? There are mainly two key cases, the authToken is expired or the third-party app calls the invalideAuthToken function. Now, I use the following solution to these problems.

1)	When the authToken is expired, xwiki authenticator app will login again and refresh all the cached authToken for every AuthTokenType. 

2)	When the third-party app calls the invalidAuthToken function, then corresponding cache will be clear and getAuthToken will be called, then XWikiHttp.login will be called to get a new token and refresh all the cached authToken for every AuthTokenType. 

3)	So if any one finds the authToken has been expired, then login and refresh all the cached authToken to maintain consistency.  In addition, I suggest that if the third-party app find the authToken is expired after a period of time, then first call getAuthToken again. If the token is different, then maybe authenticator has already update the token. If the token is the same, just call invalidAuthToken and getAuthToken again. 


##### Process
```
1. ask for Permission Granting.  If permission granted, store in preference.
2. after passing 1 step,  peekAuthToken to get an cached token for different AuthTokenType(FULL_ACESS+PackageName).  If get a token that is not null, return.
3. if step 2 fails,  ask XWiki-server for a new token. If failed return network error.
```

##### Main classes and functions
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

##### Note: for third-party apps, there are mainly the following apis:
```
1. getAuthToken
2. invalideAuthToken
   Generally, only when we find the authToken getting from getAuthToken function was expired, we will invalide the token and let go to authenticator.getAuthToken
3. addNewContact
4. confirmCredentials
```