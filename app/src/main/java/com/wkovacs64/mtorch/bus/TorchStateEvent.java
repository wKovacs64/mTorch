package com.wkovacs64.mtorch.bus;

/**
 * A bus event containing the current state of the torch. Typically seen in response to a {@link
 * StateRequestEvent} or {@link ToggleRequestEvent}.
 */
public final class TorchStateEvent {

  private final boolean state;

  /**
   * Constructs a new TorchStateEvent for the current state of the torch.
   *
   * @param state the current state of the torch (true for on, false for off)
   */
  public TorchStateEvent(boolean state) {
    this.state = state;
  }

  /**
   * Retrieves the current state of the torch as reported in the TorchStateEvent.
   *
   * @return the current state of the torch (true for on, false for off)
   */
  public boolean getState() {
    return state;
  }
}
