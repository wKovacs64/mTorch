package com.wkovacs64.mtorch.core;

/**
 * A generic torch object that can be toggled on or off.
 */
public interface Torch {
    /**
     * Initializes the torch device.
     *
     * @throws IllegalStateException if the torch could not be initialized
     */
    void init() throws IllegalStateException;
    /**
     * Toggles the torch state.
     *
     * @param enabled true to turn the torch on, false to turn it off
     */
    void toggle(boolean enabled);
    /**
     * Reports the current illumination status of the torch.
     *
     * @return true if the torch is on, false if it's off
     */
    boolean isOn();
    /**
     * Releases or tears down any necessary torch components or dependencies.
     */
    void tearDown();
}
