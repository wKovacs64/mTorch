package com.wkovacs64.mtorch.bus;

/**
 * A bus event occurring in response to a {@link ToggleRequestEvent}.
 */
public final class ToggleResponseEvent {

    private final boolean state;

    /**
     * Constructs a new ToggleResponseEvent for the resulting/current state of the torch.
     *
     * @param state the current state of the torch (true for on, false for off)
     */
    public ToggleResponseEvent(boolean state) {
        this.state = state;
    }

    /**
     * Retrieves the current state of the torch as reported in the ToggleResponseEvent.
     *
     * @return the current state of the torch (true for on, false for off)
     */
    public boolean getState() {
        return state;
    }
}
