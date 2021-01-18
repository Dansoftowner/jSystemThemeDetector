# jSystemThemeDetector
In most modern operating systems there is a dark mode option. This library is created for detecting 
this using java.

It can be useful for example if you want to synchronize your GUI App's look and feel with the operating system.

It works on **Windows 10**, **MacOS Mojave** (or later) and even on **some Linux distributions**.

This library is inspired by the dark-theme detection in [Intellij Idea](https://github.com/JetBrains/intellij-community).

# Requirements
**Java 11 or higher**

# Basic examples

#### Simple detection
```java
final OsThemeDetector detector = OsThemeDetector.getDetector();
final boolean isDarkThemeUsed = detector.isDark();
if (isDarkThemeUsed) {
    //The OS uses a dark theme
} else {
    //The OS uses a light theme
}
```

#### Listening to changes

```java
final OsThemeDetector detector = OsThemeDetector.getDetector();
detector.registerListener(isDark -> {
    if (isDark) {
        //The OS switched to a dark theme
    } else {
        //The OS switched to a light theme
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
    implementation 'com.github.Dansoftowner:jSystemThemeDetector:2.1'
}
```

# GUI Demo application

There is a Demo application available [here](src/test/java/GuiDemo.java). It's 
a basic JavaFX application that changes the UI when the OS switched to a dark/light theme.

### Linux/Ubuntu example
![Running the demo app on Ubuntu](docs/screenshot/UbuntuThemeDetection.gif)

### Windows 10 example
![Running the demo app on Windows 10](docs/screenshot/Windows10ThemeDetection.gif)

# Used libraries

 - [SLF4J](http://www.slf4j.org/) - Simple Logging Facade for Java
 - [Jetbrains Annotations](https://github.com/JetBrains/java-annotations) - Annotations for JVM-based languages
 - [JNA](https://github.com/java-native-access/jna) - Java Native Access
 - [JFA](https://github.com/0x4a616e/jfa) - Java Foundation Access
 - [OSHI](https://github.com/oshi/oshi) - Operating system & hardware information

