# Add project specific ProGuard rules here.
-keepattributes *Annotation*
-keep class kotlinx.serialization.** { *; }
-keepclassmembers class com.waterplant.distribution.** {
    *;
}
