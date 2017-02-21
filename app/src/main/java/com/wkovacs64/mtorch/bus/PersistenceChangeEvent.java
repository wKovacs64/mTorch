package com.wkovacs64.mtorch.bus;

/**
 * A bus event indicating the persistence feature has been enabled or disabled.
 */
public final class PersistenceChangeEvent {

  private final boolean state;

  /**
   * Constructs a new PersistenceChangeEvent containing the current state of the feature.
   *
   * @param state the current state of the persistence feature (true for on, false for off)
   */
  public PersistenceChangeEvent(boolean state) {
    this.state = state;
  }

  /**
   * Retrieves the current state of the persistence feature.
   *
   * @return the current state of the persistence feature (true for on, false for off)
   */
  public boolean getState() {
    return state;
  }
}
