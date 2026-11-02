# ML Kit text recognition (unbundled, Google Play services).
# ML Kit builds its internals through reflection (ComponentDiscoveryService reads registrar class names
# from the manifest meta-data and instantiates them). R8 full mode otherwise strips the registrars,
# their constructors or the fields behind MlKitContext, and TextRecognition.getClient() hits a null.
-keep class com.google.mlkit.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_latin.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_japanese.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_korean.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_text_chinese.** { *; }
-keep class com.google.android.gms.internal.mlkit_vision_common.** { *; }
-keep class com.google.android.gms.internal.mlkit_common.** { *; }

# component registrars (instantiated by name through their no-arg constructor)
-keep class * implements com.google.firebase.components.ComponentRegistrar {
    <init>();
}
-keep class * implements com.google.mlkit.common.sdkinternal.** {
    <init>();
}
-keep class com.google.firebase.components.** { *; }

-dontwarn com.google.mlkit.**
-dontwarn com.google.android.gms.internal.mlkit_**
