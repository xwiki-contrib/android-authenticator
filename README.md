# Android-authenticator

The idea of this project is to integrate a wiki instance in Android accounts, mainly including the synchronization of contacts and the XWiki authenticator. By synchronizing contacts of your company on your phone, it's easy to communicate and collaborate with each other. And the authenticator can also be used to provide credentials for other android apps. 

#Features
There're mainly the following key tasks, such as restful http connector, authenticator service, synchronization service, large capacity performance and security issues. 

#####Synchronization of contacts 

#####Providing credentials for other android apps
Things to improve before being reused by other Android applications:
* right now the authenticator store and provide the password in clear because that's what REST access need since XWiki itself does not provide any token based access by default

#####Security issues

#####Large capacity performance


#TODO List
What's left and things to improve:

###UI Design (DONE)
* material design(>=4.4), normal(4.1~4.3)
* activities
```
AuthenticatorActivity for login and account adding.
SignUpActivity for register an account.
EditContactActivity for edit and modify contact if permissions allowed
```

###Restful Api (Doing)
* sign in (DONE)
* sign up (TODO)
* admin priority to edit the contacts and create users. (TODO)
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
* Get Groups(TODO)   
http://www.xwiki.org/xwiki/rest/wikis/query?q=wiki:xwiki%20and%20object:XWiki.XWikiGroups&number=20   
* Get User From Group(TODO)   
for example XWikiAdminGroup:   
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup  
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects   
http://www.xwiki.org/xwiki/rest/wikis/contrib/spaces/XWiki/pages/XWikiAdminGroup/objects/XWiki.XWikiGroups/    
http://www.xwiki.org/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/XWikiAdminGroup/objects/XWiki.XWikiGroups
```

### Service Synchronization SyncAdapter  (Doing)
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

### Service Authenticator  (TODO) 
```
1.session id    
It may be the best and simple choice because user app just gets sessionid
from the authenticator and doesn't need to know username and passwd.

2.Basic Auth   
the user app use the BASIC auth header(Base64) to authenticate with
XWiki-server.   

3.A preconfigured httpclient/httpurlconnection

```
### Local contact manager (Doing)

### Code Comment (TODO)

### Test AutoMation (TODO)
```
try to master Maven, JUnit, Selenium, component driven development
Continue fixing a few small issues, chosen so that they are related to
your project. 
Pick from the (non-comprehensive) list of easy issues 
```
### Maven or Gradle in Jenkins (TODO)
https://www.digitalocean.com/community/tutorials/how-to-build-android-apps-with-jenkins

### DOC (TODO)


# Question
The function, XWikiHttp.getUserList is too slow. Maybe json is faster than xml. To be optimized and tested.