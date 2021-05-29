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
    // Prevents recursive capability queries.
    private int capabilityNesting = 0;
    private final AdjCapability adjCapability = new AdjCapability();
    private final EmptyAdjCapability emptyAdjCapability = new EmptyAdjCapability();
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
     * Return the capability connected to side of this P2P connection, or the empty handler if it's not available. The
     * RAII guard will prevent infinite recursion. Use with try-with-resources!
     */
    protected AdjCapability getAdjacentCapability() {
        capabilityNesting++;
        return adjCapability;
    }

    protected class AdjCapability implements AutoCloseable {
        @Override
        public void close() {
            capabilityNesting--;
        }

        /**
         * Get the capability, or a null handler if not available. Use within the scope of the enclosing AdjCapability.
         */
        protected C get() {
            if (capabilityNesting == 0) {
                throw new IllegalStateException("This should be at least 1.");
            } else if (capabilityNesting == 1) {
                C adjacentCapability = null;
                if (isActive()) {
                    final TileEntity self = getTile();
                    final TileEntity te = self.getWorld().getTileEntity(self.getPos().offset(getSide().getFacing()));

                    if (te != null) {
                        adjacentCapability = te.getCapability(capability, getSide().getOpposite().getFacing())
                                .orElse(null);
                    }
                }

                return adjacentCapability == null ? emptyHandler : adjacentCapability;
            } else {
                // This capability is already in use (as the nesting is > 1), so we return an empty handler to prevent
                // infinite recursion.
                return emptyHandler;
            }
        }
    }

    // Override when there is no capability to retrieve.
    protected class EmptyAdjCapability extends AdjCapability implements AutoCloseable {
        @Override
        public void close() {
        }

        @Override
        protected C get() {
            return emptyHandler;
        }
    }

    protected AdjCapability inputCapability() {
        P input = getInput();
        return input == null ? emptyAdjCapability : input.getAdjacentCapability();
    }

    // Send a block update on p2p status change, or any update on another endpoint.
    // Prevent recursive block updates.

    protected void sendBlockUpdate() {
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

    @Override
    public void onNeighborChanged(IBlockReader w, BlockPos pos, BlockPos neighbor) {
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
