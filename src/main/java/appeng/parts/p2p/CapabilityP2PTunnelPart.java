/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.parts.p2p;

import java.util.Objects;

import net.minecraft.core.Direction;
import net.neoforged.neoforge.capabilities.BlockCapability;

import appeng.api.parts.IPartItem;
import appeng.hooks.ticking.TickHandler;
import appeng.parts.PartAdjacentApi;

/**
 * Base class for simple capability-based p2p tunnels. Don't forget to set the 3 handlers in the constructor of the
 * child class!
 */
public abstract class CapabilityP2PTunnelPart<P extends CapabilityP2PTunnelPart<P, T>, T> extends P2PTunnelPart<P> {
    private final PartAdjacentApi<T> adjacentCapability;
    // Prevents recursive access to the adjacent capability in case P2P input/output faces touch
    private int accessDepth = 0;
    private final CapabilityGuard capabilityGuard = new CapabilityGuard();
    private final EmptyCapabilityGuard emptyCapabilityGuard = new EmptyCapabilityGuard();
    protected T inputHandler;
    protected T outputHandler;
    protected T emptyHandler;

    public CapabilityP2PTunnelPart(IPartItem<?> partItem, BlockCapability<T, Direction> capability) {
        super(partItem);
        this.adjacentCapability = new PartAdjacentApi<>(this, capability, this::forwardCapabilityInvalidation);
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    public T getExposedApi() {
        if (isOutput()) {
            return outputHandler;
        } else {
            return inputHandler;
        }
    }

    /**
     * Return the capability connected to this side of this P2P connection. If this method is called again on this
     * tunnel while the returned object has not been closed, further calls to {@link CapabilityGuard#get()} will return
     * a dummy capability.
     */
    protected final CapabilityGuard getAdjacentCapability() {
        accessDepth++;
        return capabilityGuard;
    }

    /**
     * Returns the capability attached to the input side of this tunnel's P2P connection. If this method is called again
     * on this tunnel while the returned object has not been closed, further calls to {@link CapabilityGuard#get()} will
     * return a dummy capability.
     */
    protected final CapabilityGuard getInputCapability() {
        P input = getInput();
        return input == null ? emptyCapabilityGuard : input.getAdjacentCapability();
    }

    protected class CapabilityGuard implements AutoCloseable {
        /**
         * Get the capability, or a null handler if not available. Use within the scope of the enclosing AdjCapability.
         */
        public T get() {
            if (accessDepth == 0) {
                throw new IllegalStateException("get was called after closing the wrapper");
            } else if (accessDepth == 1) {
                if (isActive()) {
                    return Objects.requireNonNullElse(adjacentCapability.find(), emptyHandler);
                }

                return emptyHandler;
            } else {
                // This capability is already in use (as the nesting is > 1), so we return an empty handler to prevent
                // infinite recursion.
                return emptyHandler;
            }
        }

        @Override
        public void close() {
            if (--accessDepth < 0) {
                throw new IllegalStateException("Close has been called multiple times");
            }
        }
    }

    /**
     * This specialization is used when the tunnel is not connected.
     */
    protected class EmptyCapabilityGuard extends CapabilityGuard implements AutoCloseable {
        @Override
        public void close() {
        }

        @Override
        public T get() {
            return emptyHandler;
        }
    }

    protected void forwardCapabilityInvalidation() {
        if (isOutput()) {
            P input = getInput();

            if (input != null) {
                input.getBlockEntity().invalidateCapabilities();
            }
        } else {
            for (P output : getOutputs()) {
                output.getBlockEntity().invalidateCapabilities();
            }
        }
    }

    @Override
    public void onTunnelNetworkChange() {
        // This might be invoked while the network is being unloaded and we don't want to send a block update then, so
        // we delay it until the next tick.
        TickHandler.instance().addCallable(getLevel(), () -> {
            if (getMainNode().isReady()) { // Check that the p2p tunnel is still there.
                getBlockEntity().invalidateCapabilities();
            }
        });
    }
}
