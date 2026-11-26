# Keep classes and methods that have the @UsedForTesting annotation
# TODO(b/373579455): Evaluate if <init> needs to be kept.
-keep @org.prauga.keyboard.annotations.UsedForTesting class * {
    void <init>();
}
-keepclassmembers class * {
    @org.prauga.keyboard.annotations.UsedForTesting *;
}

# Keep classes and methods that have the @ExternallyReferenced annotation
# TODO(b/373579455): Evaluate if <init> needs to be kept.
-keep @org.prauga.keyboard.annotations.ExternallyReferenced class * {
    void <init>();
}
-keepclassmembers class * {
    @org.prauga.keyboard.annotations.ExternallyReferenced *;
}

# Keep native methods
-keepclassmembers class * {
    native <methods>;
}

# Keep classes that are used as a parameter type of methods that are also marked as keep
# to preserve changing those methods' signature.
# TODO(b/373579455): Evaluate if <init> needs to be kept.
-keep class org.prauga.keyboard.AssetFileAddress {
    void <init>();
}
# TODO(b/373579455): Evaluate if <init> needs to be kept.
-keep class org.prauga.keyboard.Dictionary {
    void <init>();
}
# TODO(b/373579455): Evaluate if <init> needs to be kept.
-keep class org.prauga.keyboard.NgramContext {
    void <init>();
}
# TODO(b/373579455): Evaluate if <init> needs to be kept.
-keep class org.prauga.keyboard.makedict.ProbabilityInfo {
    void <init>();
}
# TODO(b/373579455): Evaluate if <init> needs to be kept.
-keep class org.prauga.keyboard.utils.LanguageModelParam {
    void <init>();
}

# TODO: remove once used in code.
-keep class org.prauga.keyboard.keyboard.KeyboardLayout { *; }

# Native code registers methods on these classes by name and reads these fields by name.
-dontobfuscate
-keepclassmembers class org.prauga.keyboard.utils.WordInputEventForPersonalization {
    <fields>;
}
-printusage build/outputs/r8-usage.txt
