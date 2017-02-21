package com.wkovacs64.mtorch.bus;

import com.squareup.otto.Bus;

/**
 * Maintains a singleton instance of the event bus and provides access through the {@link #getBus}
 * method.
 */
public final class BusProvider {

    private static final Bus BUS = new Bus();

    /**
     * Retrieves the event bus singleton instance.
     *
     * @return the event bus
     */
    public static Bus getBus() {
        return BUS;
    }

    /**
     * Suppress default constructor to prevent instantiation.
     */
    private BusProvider() {
        throw new AssertionError();
    }
}
