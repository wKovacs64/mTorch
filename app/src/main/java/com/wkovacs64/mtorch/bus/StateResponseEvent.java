package com.wkovacs64.mtorch.bus;

/**
 * A bus event occurring in response to a {@link StateRequestEvent}.
 */
public final class StateResponseEvent {

    private final boolean state;

    /**
     * Constructs a new StateResponseEvent for the current state of the torch.
     *
     * @param state the current state of the torch (true for on, false for off)
     */
    public StateResponseEvent(boolean state) {
        this.state = state;
    }

    /**
     * Retrieves the current state of the torch as reported in the StateResponseEvent.
     *
     * @return the current state of the torch (true for on, false for off)
     */
    public boolean getState() {
        return state;
    }
}
