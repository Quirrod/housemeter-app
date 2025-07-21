# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Retrofit
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

-dontwarn okhttp3.internal.platform.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**

# MINIMAL rules - only what's absolutely necessary for Gson + Retrofit generics
-keepattributes Signature
-keepattributes *Annotation*

# Essential Gson protection
-keep class com.google.gson.** { *; }
-dontwarn com.google.gson.**

# Critical: Prevent type erasure for reflection
-keep class java.lang.reflect.Type { *; }
-keep class java.lang.reflect.ParameterizedType { *; }
-keep class com.google.gson.reflect.TypeToken { *; }

# Retrofit Response wrapper protection  
-keepnames class retrofit2.Response
-keepclassmembers class retrofit2.Response { *; }

# Data models - Keep all fields and methods with signatures
-keep class com.jarrod.house.data.model.** { *; }
-keepclassmembers class com.jarrod.house.data.model.** {
    <fields>;
    <methods>;
}

# Retrofit response types
-keep class retrofit2.Response
-keep class retrofit2.Call

# API Service interfaces
-keep interface com.jarrod.house.data.api.ApiService { *; }
-keepclassmembers interface com.jarrod.house.data.api.ApiService {
    *;
}

# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.google.android.gms.** { *; }
-keep class com.google.firebase.provider.FirebaseInitProvider { *; }
-keep class com.google.firebase.components.** { *; }
-dontwarn com.google.firebase.**
-dontwarn com.google.android.gms.**

# Coroutines
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# DataStore
-keep class androidx.datastore.*.** { *; }

# Compose
-keep class androidx.compose.runtime.** { *; }
-keep class androidx.compose.ui.** { *; }
-keep class androidx.compose.material3.** { *; }

# Navigation
-keep class androidx.navigation.** { *; }

# Keep enum values
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}