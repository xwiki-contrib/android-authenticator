#Solr Api test

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

* Edit Contact
curl -u username:passwd -X PUT -H "Content-type: application/x-www-form-urlencoded" -H "Accept: application/xml" -d "className=XWiki.XWikiUsers" -d "property#company=iie" http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/username/objects/XWiki.XWikiUsers/0
curl -u username:passwd -X PUT -H "Content-type: application/x-www-form-urlencoded" -d "className=XWiki.XWikiUsers" -d "property#company=iiedacas" http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/XWiki/pages/username/objects/XWiki.XWikiUsers/0

* edit Test Object
$ curl -u Admin:admin
       -X POST -H "Content-type: application/x-www-form-urlencoded"
       -H "Accept: application/xml"
       -d "className=XWiki.TestClass"
       -d "property#test=Whatever you want"
       http://localhost/xwiki/rest/wikis/xwiki/spaces/Test/pages/Test/objects

* create a new page
create page
curl -u Admin:admin -X PUT --data-binary "@newpage.xml" -H "Content-Type: application/xml" http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages/NewPage
newpage.xml:
<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<page xmlns="http://www.xwiki.org">
        <title>Hello world</title>
        <syntax>xwiki/2.0</syntax>
        <content>This is a new page</content>
</page>


* delete
 curl -v -u Admin:admin
       -X DELETE http://localhost:8080/xwiki/rest/wikis/xwiki/spaces/Main/pages/WebHome