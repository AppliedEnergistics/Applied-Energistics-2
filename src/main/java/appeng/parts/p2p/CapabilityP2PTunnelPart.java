package appeng.parts.p2p;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;

/**
 * Base class for simple capability-based p2p tunnels. Don't forget to set the 3 handlers in the constructor of the
 * child class!
 */
public abstract class CapabilityP2PTunnelPart<P extends CapabilityP2PTunnelPart<P, C>, C> extends P2PTunnelPart<P> {
    private final Capability<C> capability;
    protected C inputHandler;
    protected C outputHandler;
    protected C nullHandler;

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

        return adjacentCapability == null ? nullHandler : adjacentCapability;
    }

    protected C getInputCapability() {
        P input = getInput();
        return input == null ? nullHandler : input.getAdjacentCapability();
    }
}
