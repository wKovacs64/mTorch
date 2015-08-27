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
    public static final String SETTINGS_AUTO_ON_KEY = "auto_on";
    public static final String SETTINGS_PERSISTENCE_KEY = "persistence";

    // Intents
    public static final String INTERNAL_INTENT = Constants.class.getPackage().getName()
            + "INTERNAL_INTENT";
    public static final String DEATH_THREAT = "die";
    public static final String REFRESH_UI = "refresh_ui";
}
