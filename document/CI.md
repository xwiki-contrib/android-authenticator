# Jenkins CI

###1. install jenkins
###2. jenkins plugins

- Git Plugin (for integrating Git with Jenkins)
- Gradle Plugin (for integrating Gradle with Jenkins)
- Android Lint Plugin (for integration Lint with Jenkins)
- [Android Emulator Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Android+Emulator+Plugin) (if you want to use an emulator)

###3. install android sdk(can automatically download)
- [how-to-build-android-apps-with-jenkins](https://www.digitalocean.com/community/tutorials/how-to-build-android-apps-with-jenkins)
- [jenkins-Android+Emulator+Plugin](https://wiki.jenkins-ci.org/display/JENKINS/Android+Emulator+Plugin)
- [android-guides for Installing-Android-SDK-Tools](https://github.com/codepath/android_guides/wiki/Installing-Android-SDK-Tools)
- [automating-continuous](https://dzone.com/articles/automating-continuous)

###4. configure jenkins
- [how-to-build-android-apps-with-jenkins](https://www.digitalocean.com/community/tutorials/how-to-build-android-apps-with-jenkins)
- [Building-Gradle-Projects-with-Jenkins-CI](https://github.com/codepath/android_guides/wiki/Building-Gradle-Projects-with-Jenkins-CI)
- [automating-continuous](https://dzone.com/articles/automating-continuous)

###5. add the build job in jenkins

```
- gradle clean build (This can include all processes, like build, test, and release to play store.)
    -clean(This task cleans the output of the project.)
    -build(This task does both assemble and check)
        -assemble(The task to assemble the output(s) of the project. generate apk.)
        -check( The task to run all the checks.)
                -check
                    -test(This task runs the tests.), lint
                -connectedCheck(Runs checks that requires a connected device or emulator. they will run on all connected devices in parallel.)
                    -connectedAndroidTest
                -deviceCheck(Runs checks using APIs to connect to remote devices. This is used on CI servers.)
                    This depends on tasks created when other plugins implement test extension points.
```

###6. some errors
```
* lint error
    add this in your module build.gradle just like
    android{
        lintOptions {
            abortOnError false
        }
    }

* for multi-projects reports
   apply plugin: 'android-reporting'
```


# Process of android release
###1. test
[android developer test RunTestsCommand](http://developer.android.com/tools/testing/testing_otheride.html#RunTestsCommand)

__Local unit test__:   
./gradlew test   
HTML test result files: <path_to_your_project>/app/build/reports/tests/ directory.    
XML test result files: <path_to_your_project>/app/build/test-results/ directory.    

__Instrumented unit test__:    
./gradlew cAT  (connectedAndroidTest)    
HTML test result files: <path_to_your_project>/app/build/outputs/reports/androidTests/connected/ directory.   
XML test result files: <path_to_your_project>/app/build/outputs/androidTest-results/connected/ directory.  

###2. build
###3. proguard
###4. signature
```
android {
    signingConfigs {
        debug {
            storeFile file("debug.keystore")
        }

        myConfig {
            storeFile file("other.keystore")
            storePassword "android"
            keyAlias "androiddebugkey"
            keyPassword "android"
        }
    }

    buildTypes {
        foo {
            debuggable true
            jniDebugBuild true
            signingConfig signingConfigs.myConfig
        }
    }
}
```
###5. Google play store!
* [Automating-Publishing-to-the-Play-Store#setting-up-jenkins-for-automating-ci-builds](https://github.com/codepath/android_guides/wiki/Automating-Publishing-to-the-Play-Store#setting-up-jenkins-for-automating-ci-builds)
* [http://blog.csdn.net/leeo1010/article/details/49903759](http://blog.csdn.net/leeo1010/article/details/49903759)

# build guides

###1.gradle build flow
http://tools.android.com/tech-docs/new-build-system/user-guide   
http://tools.android.com/tech-docs/new-build-system/build-workflow   
https://docs.gradle.org/current/userguide/userguide_single.html   

###2.ant android
https://wiki.jenkins-ci.org/display/JENKINS/Building+an+Android+app+and+test+project

###3. maven guide
http://maven.apache.org/users/index.html

###4. default value

|Property name |	Default values for debug	| Default values for release   |
| ------   | -----  | ----  |
|debuggable    |	debuggable  |  	false  |
|jniDebugBuild  |  	false	| false  |
|renderscriptDebugBuild |  false | 	false |
|renderscriptOptimLevel | 	3	|3|
|packageNameSuffix   | 	null |	null |
|versionNameSuffix   | 	null |	null |
|signingConfig    |	android.signingConfigs.debug |	null |
|zipAlign    |	false   | 	true |
|runProguard  |  	false |	false |
|proguardFile  |  	N/A (set only) |	N/A (set only) |
|proguardFiles  |  	N/A (set only) |	N/A (set only) |


#Docker
Maybe we can use the configured docker image to build and test the android app.
