# ================================================
# GENERAL ANDROID RULES
# ================================================
-keepattributes Signature, InnerClasses, EnclosingMethod
-keepattributes *Annotation*
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View

# ================================================
# PLAY SERVICES
# ================================================
-keep class com.google.android.gms.** { *; }
-keep interface com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**
-keep class com.google.api.client.** { *; }
-keep class com.google.auth.** { *; }

# ================================================
# NETWORKING (Retrofit + OkHttp + Gson)
# ================================================
# Retrofit
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * { @retrofit2.http.* <methods>; }

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Gson
-keep class com.google.gson.** { *; }
-keep class com.google.gson.examples.android.model.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ================================================
# HILT (DI)
# ================================================
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.internal.AndroidInjector { *; }
-keep class * extends dagger.hilt.android.internal.modules.HiltWrapper_ActivityModule { *; }

# ================================================
# MODEL CLASSES
# ================================================
# -keep class net.ticherhaz.pokdexclone.model.** { *; }
-keepclassmembers class net.ticherhaz.pokdexclone.model.** {*;}
-keepclassmembers class net.ticherhaz.pokdexclone.model.** {
    public <fields>;
    public <methods>;
    public <init>(...);
}

# ================================================
# PARCELABLE
# ================================================
-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

# ================================================
# GLIDE (Image Loading)
# ================================================
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
  <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}