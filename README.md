# jSystemFileDetector
Java library for checking that the particular OS uses dark-theme or not IF 
the os supports this functionality.

It's useful if you want your (GUI) app to respond to the change of the System's Ui-theme.

At this point, the library can only detect this on a Windows 10 system,
but in the future the list of supported operating systems will hopefully expand.
If you have idea how to detect this on a Mac or other systems, don't hesitate to contact me! 

# Basic examples

#### Simple detection
```java
SystemThemeDetector detector = SystemThemeDetector.getDetector();
boolean darkThemeUsed = detector.isDark();
if (darkThemeUsed) {
    // The OS uses Dark Theme
} else {
    // ...
}
```

#### Listening to changes

```java
SystemThemeDetector detector = SystemThemeDetector.getDetector();
detector.registerListener(isDark -> {
    if (isDark) {
        // The OS just switched to Dark Theme
    } else {
        // ...
    }   
});
```

# Using it with Gradle, Maven... etc
It's available on [JitPack](https://jitpack.io/#Dansoftowner/jSystemThemeDetector)!

Gradle example:
```groovy
repositories {
	...
	maven { url 'https://jitpack.io' }
}

dependencies {
    implementation 'com.github.Dansoftowner:jSystemThemeDetector:1.1'
}
```

# JRegistry
[JRegistry](https://jregistry.sourceforge.io/) is another library used by this project.
It's licensed under the LGPLv2 license.

