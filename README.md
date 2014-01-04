# mTorch

mTorch is a minimalistic torch app for Android. It is objectively superior to every other torch or flashlight app ever created for any platform in the history of computing. The previous statement is completely false. 

I wrote this app because I felt there was a real niche for something that could make your mobile device act as a flashlight. Bumping around in the dark can be dangerous and I can never find a flashlight when I need one - but I _always_ have my mobile phone in my pocket. I scoured the Play Store and other third-party app collections and just couldn't find anything that would accomplish such a feat. Thus, I felt compelled to create one myself. (What's the markdown for sarcasm? There are probably more torch apps on the Play Store than any other type of app. I just did it for fun and as a learning experience.)

## Building

This project was created in Android Studio and uses the Gradle build system.

#### System Requirements:
* [Oracle Java JDK 6](http://www.oracle.com/technetwork/java/javase/downloads/index.html)
    * Set environment variable JAVA_HOME to JDK installation location
    * JDK 7 may work if you already have it installed, I haven't tested it

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

After you have the system requirements installed and configured and you've cloned a copy of the git repository, simple leverage the Gradle wrapper to build: `gradlew build`

That's it! You should now have an installable APK in the `<project_root>/mTorch/build/apk` directory.

## Contributing
You can use the Import Project feature in Android Studio and point at the project root directory to start hacking on this app.

_More contributing details to come._

## Developed By
Designed and developed by Justin R. Hall. Special thanks to [Santoso Wijaya](https://github.com/santa4nt), see the [CONTRIBUTORS](../blob/master/CONTRIBUTORS) file for further details.

## License/Copying
mTorch is distributed under the MIT License. See the [LICENSE](../blob/master/LICENSE) file for further details.
