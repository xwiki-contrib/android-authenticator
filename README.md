# android-authenticator

An Android authenticator (i.e. listed in the Android Accounts Manager in the android settings) to be used by other andoid application and with integrated synchronisation of contacts

# TODO

In it's currently state it's just experimentation, did not had time to no much more.

What's left and things to improve:

* contact synchronisation is not finished

Things to improve before being reused by other Android applications:

* right now the authenticator store and provide the password in clear because that's what REST access need since XWiki itself does not provide any token based access by default


# feature
###ui design (DONE)
material design(>=4.4), normal(4.1~4.3)

###restful api 
sign in/up

solr search for example:

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


### local contact manage(TODO)
### synAdapter(TODO)
### session(token) based access(TODO)
