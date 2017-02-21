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

  // Request Results
  public static final int RESULT_PERMISSION_CAMERA = 0;
}
