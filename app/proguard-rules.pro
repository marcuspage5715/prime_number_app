# R8 configuration for the release build.
#
# The app uses no reflection, no dynamic code loading and no serialisation
# library, so no "keep" rules are required: R8 is free to rename and strip
# everything it can. Full-mode R8 is enabled by proguard-android-optimize.txt.
#
# If you later add a library that needs keeps, add them here with a comment
# explaining why, and re-test the release build. Keep this file as small as
# possible: every keep rule widens the attack surface R8 would otherwise trim.

# Keep line numbers so Play Console crash reports stay readable, while still
# hiding the original source file names.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
