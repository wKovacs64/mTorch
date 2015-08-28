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

    /**
     * Intent filter used to communicate bewteen {@link com.wkovacs64.mtorch.service.TorchService}
     * and {@link com.wkovacs64.mtorch.receiver.CommandReceiver}. This <b>must</b> match the intent
     * filter action name in the manifest!
     */
    public static final String INTENT_COMMAND = "com.wkovacs64.mtorch.command";

    // Extras
    public static final String EXTRA_DEATH_THREAT = "die";
    public static final String EXTRA_UPDATE_UI = "refresh_ui";
    public static final String EXTRA_START_TORCH = "start_torch";
    public static final String EXTRA_STOP_TORCH = "stop_torch";

    // Request Results
    public static final int RESULT_PERMISSION_CAMERA = 0;
}
