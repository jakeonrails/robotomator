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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Keep AccessibilityService - required for Android system binding
-keep class com.robotomator.app.RobotomatorAccessibilityService {
    *;
}

# Keep accessibility service methods that are called by reflection
-keepclassmembers class com.robotomator.app.RobotomatorAccessibilityService {
    public <methods>;
}

# Preserve line numbers for debugging accessibility issues
-keepattributes SourceFile,LineNumberTable
