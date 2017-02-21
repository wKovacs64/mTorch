package com.wkovacs64.mtorch.bus;

/**
 * A bus event requesting the torch be toggled on or off.
 */
public final class ToggleRequestEvent {

    private final boolean requestedState;
    private final boolean persistence;

    private boolean produced;

    /**
     * Constructs a new ToggleRequestEvent, requesting the torch be toggled to the requested state.
     *
     * @param requestedState the requested state of the torch (true for on, false for off)
     * @param persistence    the current state of the persistence feature (true for on, false for
     *                       off)
     */
    public ToggleRequestEvent(boolean requestedState, boolean persistence) {
        this.requestedState = requestedState;
        this.persistence = persistence;
    }

    /**
     * Retrieves the requested state of the torch.
     *
     * @return the desired state of the torch (true for on, false for off)
     */
    public boolean getRequestedState() {
        return requestedState;
    }

    /**
     * Retrieves the current state of the persistence feature.
     *
     * @return the current state of the persistence feature (true for on, false for off)
     */
    public boolean getPersistence() {
        return persistence;
    }

    /**
     * Determines if this event was produced from cached values.
     *
     * @return true if the event was produced from cached results, false if it's fresh
     */
    public boolean isProduced() {
        return produced;
    }

    /**
     * Sets the produced flag on this event, indicating it was produced from cached values.
     *
     * @param produced true if this event was produced from cached values, false if it's fresh
     */
    public void setProduced(boolean produced) {
        this.produced = produced;
    }
}
