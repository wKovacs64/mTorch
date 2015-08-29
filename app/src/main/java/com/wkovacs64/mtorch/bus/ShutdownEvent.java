package com.wkovacs64.mtorch.bus;

/**
 * A bus event indicating every subscriber should shut itself down.
 */
public final class ShutdownEvent {

    private final String error;

    /**
     * Constructs a new ShutdownEvent with an included error message.
     *
     * @param error the error message which triggered the event
     */
    public ShutdownEvent(String error) {
        this.error = error;
    }

    /**
     * Retrieves the error message which triggered the event.
     *
     * @return the error message which triggered the event
     */
    public String getError() {
        return error;
    }
}
