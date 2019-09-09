# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

#dont generate complex names
-dontusemixedcaseclassnames

#dont skip non public classes
-dontskipnonpubliclibraryclasses

#dont verify during compilation as after java6 they are already verified,to reduce process time remove it
-dontpreverify

#Describe more info during processing,for eg,if exception occur,full stack trace will be printed.
-verbose

#optimization criteria set
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*

#all files removed by default,if they are not mentioned in keep,to avoid that using this tag
-dontshrink

#dont optimize the code
-dontoptimize

-keepattributes EnclosingMethod

#to avoid obfuscation of code
#-dontobfuscate

#upgrade version of class files
#-target 1.6
#-ignorewarnings

#For crashlytics
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keepattributes Signature
-keepattributes Exceptions

-ignorewarnings

-keep class * {
    public private *;
}

#Keep classes that are referenced on the AndroidManifest
-keep public class * extends android.app.Activity
-keep public class * extends androidx.appcompat.app.AppCompatActivity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class com.android.vending.licensing.ILicensingService
-keep public class * extends android.app.Application

-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode producerNode;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueConsumerNodeRef {
    rx.internal.util.atomic.LinkedQueueNode consumerNode;
}


#avoid changing name of methods on layout onclick
-keepclassmembers class * {
 public void onClickButton(android.view.View);
}

#avoid native methods(if any)
-keepclasseswithmembernames class * {
    native <methods>;
}

#To maintain custom components names that are used on layouts XML
-keep public class custom.components.package.and.name.**

#To maintain custom components names that are used on layouts XML
-keep public class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
    public void set*(...);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}

-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#To keep parcelable classes (to serialize - deserialize objects to sent through Intents)
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

#keep R file
-keepclassmembers class **.R$* {
    public static <fields>;
}

-keepattributes InnerClasses
-dontoptimize
-dontwarn InnerClasses

-keep class com.google.oauth-client.** { *; }
-keep class com.google.http-client.** { *; }
-ignorewarnings