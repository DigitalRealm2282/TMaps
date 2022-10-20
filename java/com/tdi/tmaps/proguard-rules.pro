# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.tdi.tmaps.model.* { *; }
-keep class com.tdi.tmaps.utils.Common { *; }
-keep class com.google.android.gms.* { *; }
-keep public class com.google.firebase.** { *; }
-keepclasseswithmembers class com.google.firebase.FirebaseException
-keep class com.tdi.tmaps.remote.** { *; }
-keep class com.tdi.tmaps.service.** { *; }
-keep interface androidx.** { *; }
-keep class androidx.** { *; }
-keep class com.google.** { *; }
-keep class com.android.** { *; }
-keep class com.tdi.tmaps.LoginActivity {<fields>; }

-dontwarn androidx.**
-dontwarn com.google.**
-dontwarn com.android.**
-dontwarn com.google.firebase.auth.internal.**
# Prevent R8 from leaving Data object members always null
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}
-keep,allowobfuscation interface com.google.gson.annotations.SerializedName
#-keep interface com.tdi.tmaps.remote.IFCMService { *; }
#-keep class * implements com.google.android.gms.maps.** { *; }
#-keep class com.google.android.gms.maps.** { *; }
#-keep interface com.google.android.gms.maps.** { *; }
#-keep class com.karumi.dexter.*
