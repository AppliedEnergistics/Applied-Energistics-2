package appeng.parts.p2p;

import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

import appeng.me.GridAccessException;

/**
 * Base class for simple capability-based p2p tunnels. Don't forget to set the 3 handlers in the constructor of the
 * child class!
 */
public abstract class CapabilityP2PTunnelPart<P extends CapabilityP2PTunnelPart<P, C>, C> extends P2PTunnelPart<P> {
    private final Capability<C> capability;
    // Prevents recursive block updates.
    private boolean inBlockUpdate = false;
    // Prevents recursive access to the adjacent capability in case P2P input/output faces touch
    private int accessDepth = 0;
    private final CapabilityGuard capabilityGuard = new CapabilityGuard();
    private final EmptyCapabilityGuard emptyCapabilityGuard = new EmptyCapabilityGuard();
    protected C inputHandler;
    protected C outputHandler;
    protected C emptyHandler;

    public CapabilityP2PTunnelPart(ItemStack is, Capability<C> capability) {
        super(is);
        this.capability = capability;
    }

    @Override
    protected float getPowerDrainPerTick() {
        return 2.0f;
    }

    public final <T> LazyOptional<T> getCapability(Capability<T> capabilityClass) {
        if (capabilityClass == capability) {
            if (isOutput()) {
                return LazyOptional.of(() -> outputHandler).cast();
            } else {
                return LazyOptional.of(() -> inputHandler).cast();
            }
        }
        return LazyOptional.empty();
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
        protected C get() {
            if (accessDepth == 0) {
                throw new IllegalStateException("get was called after closing the wrapper");
            } else if (accessDepth == 1) {
                if (isActive()) {
                    final TileEntity self = getTile();
                    final TileEntity te = self.getWorld().getTileEntity(getFacingPos());

                    if (te != null) {
                        return te.getCapability(capability, getSide().getOpposite().getFacing())
                                .orElse(emptyHandler);
                    }
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
     * The position right in front of this P2P tunnel.
     */
    private BlockPos getFacingPos() {
        return getHost().getLocation().getPos().offset(getSide().getFacing());
    }

    /**
     * This specialization is used when the tunnel is not connected.
     */
    protected class EmptyCapabilityGuard extends CapabilityGuard implements AutoCloseable {
        @Override
        public void close() {
        }

        @Override
        protected C get() {
            return emptyHandler;
        }
    }

    // Send a block update on p2p status change, or any update on another endpoint.
    protected void sendBlockUpdate() {
        // Prevent recursive block updates.
        if (!inBlockUpdate) {
            inBlockUpdate = true;

            try {
                // getHost().notifyNeighbors() would queue a callback, but we want to do an update synchronously!
                // (otherwise we can't detect infinite recursion, it would just queue updates endlessly)
                TileEntity self = getTile();
                self.getWorld().notifyNeighborsOfStateChange(self.getPos(), Blocks.AIR);
            } finally {
                inBlockUpdate = false;
            }
        }
    }

    @Override
    public void onTunnelNetworkChange() {
        sendBlockUpdate();
    }

    /**
     * Forward block updates from the attached tile's position to the other end of the tunnel. Required for TE's on the
     * other end to know that the available caps may have changed.
     */
    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
        // We only care about block updates on the side this tunnel is facing
        if (!getFacingPos().equals(neighbor)) {
            return;
        }

        // Prevent recursive block updates.
        if (!inBlockUpdate) {
            inBlockUpdate = true;

            try {
                if (isOutput()) {
                    P input = getInput();

                    if (input != null) {
                        input.sendBlockUpdate();
                    }
                } else {
                    try {
                        for (P output : getOutputs()) {
                            output.sendBlockUpdate();
                        }
                    } catch (GridAccessException ignored) {
                        // :-P
                    }
                }
            } finally {
                inBlockUpdate = false;
            }
        }
    }
}
