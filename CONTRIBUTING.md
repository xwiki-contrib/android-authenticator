# How to Contribute

First please go through the [Community Contributions at Xwiki](https://dev.xwiki.org/xwiki/bin/view/Community/Contributing)

# Raising an issue:
 This is an Open Source project and we would be happy to see contributors who report bugs and file feature requests submitting pull requests as well.
 Please report issues [here](https://jira.xwiki.org/projects/ANDAUTH/issues).

# Branch Policy

## Sending pull requests:

### Preparation of your repository

Go to the repository on github at https://github.com/xwiki-contrib/android-authenticator and click the `Fork` button at the top right. You’ll now have your own copy of the original Android-Authenticator repository in your github account

If you are using \*Nix system, you cat use tool `git` for getting your repository:

`$ git clone https://github.com/YOUR_USERNAME/android-authenticator.git`

where `YOUR_USERNAME` is your github username. You’ll now have a local copy of your version of the original Xwiki repository.

### Making changes

* Open android-authenticator folder. For example, `$ cd android-authenticator`
* Add a connection to the original owner’s repository: `$ git remote add upstream https://github.com/xwiki-contrib/android-authenticator.git`
* To check this remote add set up: `$ git remote -v`
* Make changes to files

A little git hints:

* Use `git add` inside the project to get changes into index (to make them active for commit). You can use `git add -A` to simply add all changes to index
* Use `git commit -m "message"` to fix changes with your message. After this action all changes, which was indexed with `git add`, will be fixed and will be able to send to your remote repository
* Use `git push` to send maked commits into your repository

Before final creating of pull request, it is strongly recommended to pull changes from remote base (by `$ git pull upstream master --rebase`) (xwiki android app) and check that everything is ok.

When all changes are made - create a PR (Pull Request). For this:

* Go to your version of the repository on github.
* Click the `New pull request` button at the top.
  Note that XWiki's repository will be on the left and your repository will be on the right
* Click the green button `Create pull request`. Give a succinct and informative title, in the comment field give a short explanation of the changes and click the green button `Create pull request` again
