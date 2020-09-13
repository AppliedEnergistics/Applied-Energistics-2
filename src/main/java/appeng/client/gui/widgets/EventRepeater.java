/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.client.gui.widgets;

import java.time.Duration;

/**
 * This class can be used to implement repeating events such as holding down a button to fire
 * an event repeatedly while the button is still being held, or repeatedly scrolling down
 * a page, while the mouse is held down on the scrollbar.
 */
public class EventRepeater {

    /**
     * -1 if no repeat event is scheduled.
     * Otherwise contains the {@link System#nanoTime()} at which the next event should occur.
     */
    private long nextEventTime = -1;

    private EventCallback eventCallback = null;

    private final long eventDelay; // In nanoseconds

    private final long eventInterval; // In nanoseconds

    public EventRepeater(Duration delay, Duration interval) {
        this.eventDelay = delay.toNanos();
        this.eventInterval = interval.toNanos();
    }

    public void tick() {
        if (this.eventCallback == null) {
            return; // No event scheduled
        }

        // Use nanoTime here because it is monotonically increasing, while System.currentTimeMillis is not
        long nanoTime = System.nanoTime();
        if (nanoTime < this.nextEventTime) {
            return; // Event time not reached
        }

        // Before triggering, recompute the next event, since
        // the event callback itself may reschedule/cancel, and
        // we should not overwrite that
        this.nextEventTime = nanoTime + this.eventInterval;
        this.eventCallback.trigger();
    }

    /**
     * Schedule the given callback to be called after a given initial delay, and then
     * after the given interval repeatedly.
     *
     * <p>Replaces any previously queued callback.
     */
    public void repeat(EventCallback callback) {
        long time = System.nanoTime();
        this.eventCallback = callback;
        this.nextEventTime = time + eventDelay;
    }

    public boolean isRepeating() {
        return this.eventCallback != null;
    }

    /**
     * Stop repeating the event.
     */
    public void stop() {
        this.eventCallback = null;
    }

    @FunctionalInterface
    public interface EventCallback {
        void trigger();
    }

}
