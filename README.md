# [DEPRECATED]

This project is no longer maintained.

# [mTorch](https://wkovacs64.github.io/mTorch/)

*A minimalistic torch/flashlight app for Android™.*

## Description and Requirements

mTorch was originally created as an exercise in Android development, but now it appears some people actually use it so I try to maintain it when I have time. The intended design was to be as minimalistic as possible while still providing a user-friendly experience. As such, the project has the following requirements:

##### Features

* _Auto On_  
If enabled, the torch should automatically illuminate upon entering the app. If disabled, the torch remains off upon entering the app and must be toggled by tapping the toggle image.

* _Persistence_  
If enabled, the state of the torch persists after leaving the app. If the torch was illuminated upon leaving the app and persistence is enabled, the torch remains lit and a notification is displayed for quick return to the app. If the torch was off upon leaving the app and persistence is enabled, persistence does nothing and no notification is created.

##### Behavior

* _Hardware Flash_  
A hardware LED flash is required. Some flashlight apps will turn the display brightness up and just display a white screen in the absence of a hardware flash, but this app is not one of them.

* _Quick Toggle_  
One of the most annoying behavioral issues I found in almost every other flashlight/torch app on the market was a (relatively) long delay between tapping the toggle image and the light illuminating. While developing mTorch, I came to realize what caused this annoying delay. Toggling the hardware flash requires acquiring access to the camera device. Acquiring access to the camera produces the delay in question. Most (all?) of the apps with this delay are acquiring and releasing the camera device when the user toggles the torch state, causing the delay to be present on each tap. One of my primary goals with mTorch was to reduce/eliminate that delay, so I take a different approach and acquire the camera device upon entering the app and release it when leaving the app (assuming the torch was off or the Persistence feature was disabled). _(Note: this approach produces an obscure bug that drives me crazy, even though I doubt any real user would ever run into it. See issue [#2](../../issues/2) on GitHub.)_

## Contributing
See the [CONTRIBUTING](../master/CONTRIBUTING.md) file if you'd like to contribute code. I also welcome localization efforts if you would like to [translate](https://crowdin.com/project/mtorch) some strings.

[![Crowdin](https://d322cqt584bo4o.cloudfront.net/mtorch/localized.svg)](https://crowdin.com/project/mtorch)

## Credits

* [Speedy McVroom](http://viscious-speed.deviantart.com) - public domain lamp graphic
* Aleksandr Loktionov - Russian translation
* naofum - Japanese translation
* YFdyh000 - Chinese Simplified translation

## License/Copying
mTorch is distributed under the MIT License. See the [LICENSE](../master/LICENSE) file for details.

## Donating
If you feel like donating for some unfathomable reason, I accept [Bitcoin](http://bit.co.in/mtorch).
