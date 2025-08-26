# Keep Room models and generated code
-keep class androidx.room.** { *; }
-keep class com.gopi.securevault.** { *; }

# Ignore warnings from SQLCipher
-dontwarn net.sqlcipher.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.concurrent.GuardedBy
