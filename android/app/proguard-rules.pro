# ProGuard rules for Premium VPN

# Keep Go native library bindings
-keep class go.** { *; }
-keep class mobileproxy.** { *; }
-keep class premium_vpn_go.** { *; }

# Keep JNI methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Hilt generated code
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.managers.ViewComponentManager$FragmentContextWrapper { *; }

# Keep Room entities
-keep class com.premiumvpn.app.data.local.** { *; }

# Keep Retrofit API interfaces
-keep class com.premiumvpn.app.data.remote.** { *; }
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Keep Gson serialized classes
-keep class com.premiumvpn.app.data.remote.dto.** { *; }
-keepclassmembers class com.premiumvpn.app.data.remote.dto.** { *; }

# Don't warn about missing classes
-dontwarn javax.annotation.**
-dontwarn org.codehaus.mojo.animal_sniffer.**
-dontwarn okio.**
-dontwarn okhttp3.**
-dontwarn retrofit2.**
