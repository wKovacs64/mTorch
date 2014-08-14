# [mTorch](http://mtorch.wkovacs64.com)

mTorch is a minimalistic torch/flashlight app for Android. It is objectively and measurably superior to every other app ever created for any platform in the history of computing. Period. It is technologically impossible for a better app to exist, and that's a proven fact. It's science. The previous statements are entirely false, except for the first sentence.

I realize there are probably more torch apps on the Play Store than any other type of app, and most of them are probably much more polished and feature-rich than mine. Honestly, I just did it to have a little fun and learn some more Android. If anyone finds it useful and prefers the minimalistic approach I took with it, all the better.

## Building

This project was created in [Android Studio](http://developer.android.com/sdk/installing/studio.html) and uses the [Gradle build system](http://www.gradleware.com/resources/tech/android). The first time you build something using the Gradle wrapper script, it will download the appropriate version of Gradle which can be relatively large (~150 MB). By default, it will save it in the `.gradle` subdirectory of your home directory. If you wish to override this location, set the environment variable GRADLE_USER_HOME to a more desirable path. If you already have Gradle installed on your system and would like to use that instead, make sure you set the GRADLE_HOME environment variable to the location where Gradle is installed and use the `gradle` command instead of the `gradlew` wrapper script to perform the build.

#### System Requirements:
* [Oracle Java JDK 7](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
    * Set environment variable JAVA_HOME to JDK installation location
    * You may want to set ANDROID_STUDIO_JDK to the same path, especially if JAVA_HOME already points elsewhere
    * JDK 6 works as well, if you prefer

* [Android SDK](https://developer.android.com/sdk)
    * Latest version recommended
    * Set environment variable ANDROID_HOME to SDK installation location
    * Within Android SDK Manager, install the following options:
        * Tools:
            * Android SDK Tools
            * Android SDK Platform-tools
            * Android SDK Build-tools
        * Android 4.4.2 (API19):
            * SDK Platform
        * Extras:
            * Android Support Repository
            * Android Support Library

#### Build It:
* After you have the system requirements installed and configured and you've cloned a copy of the git repository, simple leverage the Gradle wrapper to build: `gradlew build`
* That's it! You should now have an installable APK in the `<project_root>/mTorch/build/apk` directory.

## Contributing
You can use the Import Project feature in Android Studio and point at the project root directory to start hacking on this app. Feel free to send me pull requests with fixes or improvements. I also welcome localization efforts if you would like to [translate](https://crowdin.com/project/mtorch) some strings.

[![Crowdin](https://d322cqt584bo4o.cloudfront.net/mtorch/localized.png)](https://crowdin.com/project/mtorch)

## Developed By
Designed and developed by Justin R. Hall. Special thanks to [Santoso Wijaya](https://github.com/santa4nt), see the [CONTRIBUTORS](../master/CONTRIBUTORS) file for further details.

## License/Copying
mTorch is distributed under the MIT License. See the [LICENSE](../master/LICENSE) file for further details.

## Donating
If you feel like donating for some unfathomable reason, I accept [Bitcoin](http://bit.co.in/mtorch).
