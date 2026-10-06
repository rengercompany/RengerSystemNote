# Renger System | Note — правила ProGuard
-keep class com.renger.system.note.** { *; }
# Room
-keep class androidx.room.** { *; }
-keep @androidx.room.Entity class * { *; }

# Kotlin metadata
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

# Compose
-keep class androidx.compose.** { *; }

# Data классы
-keep class com.renger.system.note.data.** { *; }
