package com.wkovacs64.mtorch;

/**
 * Uninstantiable constants class.
 */
public final class Constants {

    /**
     * Suppress default constructor to prevent instantiation.
     */
    private Constants() {
        throw new AssertionError();
    }

    // SharedPreferences Keys
    public static final String SETTINGS_KEY_AUTO_ON = "auto_on";
    public static final String SETTINGS_KEY_PERSISTENCE = "persistence";

    // Intents
    public static final String INTENT_INTERNAL = Constants.class.getPackage().getName()
            + "INTENT_INTERNAL";

    // Extras
    public static final String EXTRA_DEATH_THREAT = "die";
    public static final String EXTRA_REFRESH_UI = "refresh_ui";
    public static final String EXTRA_START_TORCH = "start_torch";
    public static final String EXTRA_STOP_TORCH = "stop_torch";
}
