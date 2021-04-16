# What Are Enums

What Are Enums, a traditional developer's worst nightmare™
(and also a library for kotlin minecraft mod developers)

## Usage

In `build.gradle`:

```groovy
repositories {
    maven {
        url 'https://maven.concern.i.ng'
    }
}

dependencies {
    implementation 'sschr15.fabricmods.concern:what-are-enums:VERSION'
}
```

In a file:

```kotlin
object MoreSoundCategories {
    @JvmStatic val CUSTOM_CATEGORY by enum(SoundCategory::class, "custom")
}
```

Load it at the proper time (in `fabric.mod.json`):

```json
{
    "custom": {
        "whatareenums": {
            "package": "my.package.with.enum.extenders",
            "enums": {
                "sound.SoundCategory": "MoreSoundCategories",
                "util.Direction": "direction.ConcernDirectionChange"
            }
        }
    }
}
```
