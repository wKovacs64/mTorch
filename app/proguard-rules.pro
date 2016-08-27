# Add project specific ProGuard rules here.

# Timber (may only be needed for release builds logging to a third party like Crashlytics)
-keep public class timber.log.**

# Otto
-keepclassmembers class ** {
    @com.squareup.otto.Subscribe public *;
    @com.squareup.otto.Produce public *;
}

# mTorch
-keep class com.wkovacs64.mtorch.bus.** { *; }
