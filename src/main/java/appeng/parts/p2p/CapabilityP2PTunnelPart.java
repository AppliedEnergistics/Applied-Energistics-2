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
    private int insertionNesting = 0;
    private final InputCapability inputCapability = new InputCapability();
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

    protected C getAdjacentCapability() {
        C adjacentCapability = null;

        if (this.isActive()) {
            final TileEntity self = this.getTile();
            final TileEntity te = self.getWorld().getTileEntity(self.getPos().offset(this.getSide().getFacing()));

            if (te != null) {
                adjacentCapability = te.getCapability(capability, this.getSide().getOpposite().getFacing())
                        .orElse(null);
            }
        }

        return adjacentCapability == null ? emptyHandler : adjacentCapability;
    }

    protected InputCapability inputCapability() {
        insertionNesting++;
        return inputCapability;
    }

    protected class InputCapability implements AutoCloseable {
        @Override
        public void close() {
            insertionNesting--;
        }

        /**
         * Return the capability connected to the input side of this P2P connection, or the empty handler if it's not
         * available. The RAII guard will prevent infinite recursion.
         */
        protected C get() {
            if (insertionNesting == 0) {
                throw new IllegalStateException("This should be at least 1.");
            } else if (insertionNesting == 1) {
                P input = getInput();
                return input == null ? emptyHandler : input.getAdjacentCapability();
            } else {
                return emptyHandler;
            }
        }
    }

    // Send a block update on p2p status change, or any update on another endpoint.
    // Prevent recursive block updates.

    protected void sendBlockUpdate() {
        if (!inBlockUpdate) {
            inBlockUpdate = true;
            // getHost().notifyNeighbors() would queue a callback, but we want to do an update synchronously!
            // (otherwise we can't detect infinite recursion, it would just queue updates endlessly)
            TileEntity self = getTile();
            self.getWorld().notifyNeighborsOfStateChange(self.getPos(), Blocks.AIR);
            inBlockUpdate = false;
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
            inBlockUpdate = false;
        }
    }
}
