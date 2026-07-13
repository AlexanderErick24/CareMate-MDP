# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Preserve Line Numbers and Attributes for Stacktraces
-keepattributes SourceFile,LineNumberTable,Signature,InnerClasses,EnclosingMethod,Deprecated,*Annotation*,*JavascriptInterface*

# ----------------- GSON RULES -----------------
# Keep Gson annotations
-keepattributes *Annotation*,Signature
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }

# Keep DTO classes used by Gson for deserialization
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}

# Keep all DTO / Domain Model classes from being obfuscated or stripped
-keep class com.mdp.caremate.data.model.** { *; }
-keep class com.mdp.caremate.data.sources.remote.** { *; }

# ----------------- MOSHI RULES -----------------
# Keep Moshi generic signature and annotations
-keepattributes Signature, *Annotation*, InnerClasses, EnclosingMethod
# Keep Moshi's generated adapter classes (needed for KSP codegen)
-keep class com.squareup.moshi.** { *; }
-keep class *JsonAdapter { *; }
-keep class *JsonAdapterFactory { *; }
-keepclassmembers class * {
    @com.squareup.moshi.Json <fields>;
    @com.squareup.moshi.JsonClass <fields>;
}
-keep class kotlin.reflect.jvm.internal.** {*;}

# ----------------- RETROFIT RULES -----------------
# Keep Retrofit interface methods and types
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepclassmembers class * {
    @retrofit2.http.** <methods>;
}
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }

# ----------------- OKHTTP RULES -----------------
-keepattributes Signature, InnerClasses, EnclosingMethod
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

# ----------------- ROOM RULES -----------------
-keep class * extends androidx.room.RoomDatabase
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# ----------------- FIREBASE RULES -----------------
# Keep Firebase model classes
-keepattributes *Annotation*,Signature
-keepclassmembers class * {
    @com.google.firebase.firestore.PropertyName <fields>;
    @com.google.firebase.firestore.PropertyName <methods>;
    @com.google.firebase.firestore.DocumentId <fields>;
}
-keep class com.google.firebase.** { *; }