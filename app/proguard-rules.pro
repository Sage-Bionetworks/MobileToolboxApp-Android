# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
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
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# SpongyCastle
-keep class org.spongycastle.crypto.* { *; }
-keep class org.spongycastle.crypto.agreement.** { *; }
-keep class org.spongycastle.crypto.digests.* { *; }
-keep class org.spongycastle.crypto.ec.* { *; }
-keep class org.spongycastle.crypto.encodings.* { *; }
-keep class org.spongycastle.crypto.engines.* { *; }
-keep class org.spongycastle.crypto.macs.* { *; }
-keep class org.spongycastle.crypto.modes.* { *; }
-keep class org.spongycastle.crypto.paddings.* { *; }
-keep class org.spongycastle.crypto.params.* { *; }
-keep class org.spongycastle.crypto.prng.* { *; }
-keep class org.spongycastle.crypto.signers.* { *; }

-keep class org.spongycastle.jcajce.provider.asymmetric.* { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.dh.* { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.dsa.* { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.ec.* { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.elgamal.* { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.rsa.* { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.util.* { *; }
-keep class org.spongycastle.jcajce.provider.asymmetric.x509.* { *; }

-keep class org.spongycastle.jcajce.provider.digest.** { *; }
-keep class org.spongycastle.jcajce.provider.keystore.** { *; }
-keep class org.spongycastle.jcajce.provider.symmetric.** { *; }
-keep class org.spongycastle.jcajce.spec.* { *; }
-keep class org.spongycastle.jce.** { *; }

-keep class org.spongycastle.x509.** { *; }
-keep class org.spongycastle.bcpg.** { *; }
-keep class org.spongycastle.openpgp.** { *; }

# Logback
-keep class ch.qos.** { *; }
-keep class org.slf4j.** { *; }
-keepattributes *Annotation*
-dontwarn ch.qos.logback.core.net.*

# KotlinX Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt # core serialization annotations

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# Change here com.yourcompany.yourpackage
-keep,includedescriptorclasses class com.yourcompany.yourpackage.**$$serializer { *; } # <-- change package name to your app's
-keepclassmembers class com.yourcompany.yourpackage.** { # <-- change package name to your app's
    *** Companion;
}
-keepclasseswithmembers class com.yourcompany.yourpackage.** { # <-- change package name to your app's
    kotlinx.serialization.KSerializer serializer(...);
}