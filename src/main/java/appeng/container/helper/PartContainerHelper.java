package appeng.container.helper;

import appeng.api.config.SecurityPermissions;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Helper for containers that can be opened for {@link appeng.api.parts.IPart parts} that are attached
 * to a {@link appeng.api.parts.IPartHost part host}.
 *
 * @param <C>
 * @param <P> The type of part this container is for.
 */
public final class PartContainerHelper<C extends AEBaseContainer, P extends IPart> extends AbstractContainerHelper {

    private final Class<P> partClass;

    private final ContainerFactory<P, C> factory;

    private final SecurityPermissions requiredPermission;

    public PartContainerHelper(ContainerFactory<P, C> factory, Class<P> partClass) {
        this(factory, partClass, null);
    }

    public PartContainerHelper(ContainerFactory<P, C> factory, Class<P> partClass, SecurityPermissions requiredPermission) {
        super(requiredPermission);
        this.partClass = partClass;
        this.factory = factory;
        this.requiredPermission = requiredPermission;
    }

    /**
     * Opens a container that is based around a single tile entity.
     * The tile entity's position is encoded in the packet buffer.
     */
    public C fromNetwork(int windowId, PlayerInventory inv, PacketBuffer packetBuf) {
        BlockPos pos = packetBuf.readBlockPos();
        TileEntity te = inv.player.world.getTileEntity(pos);
        if (partClass.isInstance(te)) {
            return factory.create(windowId, inv, partClass.cast(te));
        }
        return null;
    }

    public boolean open(PlayerEntity player, ContainerLocator locator) {
        if (!(player instanceof ServerPlayerEntity)) {
            // Cannot open containers on the client or for non-players
            // FIXME logging?
            return false;
        }

        if (!locator.hasBlockPos() || !locator.hasSide()) {
            return false; // No block was clicked or the side is unknown
            // FIXME: If no side is provided, should be try with INTERNAL???
        }

        TileEntity tileEntity = player.world.getTileEntity(locator.getBlockPos());
        if (!(tileEntity instanceof IPartHost)) {
            // FIXME logging?
            return false; // Wrong tile entity at block position
        }
        IPartHost partHost = (IPartHost) tileEntity;
        IPart part = partHost.getPart(locator.getSide());
        if (!(partClass.isInstance(part))) {
            // FIXME logging?
            return false;
        }
        P actualPart = partClass.cast(tileEntity);

        if (!checkPermission(player, actualPart)) {
            return false;
        }

        // Use block name at position
        // FIXME: this is not right, we'd need to check the part's item stack, or custom naming interface impl
        ITextComponent title = player.world.getBlockState(locator.getBlockPos()).getBlock().getNameTextComponent();

        // FIXME: Check permissions...
        if (requiredPermission != null) {
            throw new IllegalStateException(); // NOT YET IMPLEMENTED
        }

        INamedContainerProvider container = new SimpleNamedContainerProvider(
                (wnd, p, pl) -> {
                    C c = factory.create(wnd, p, actualPart);
                    // Set the original locator on the opened server-side container for it to more
                    // easily remember how to re-open after being closed.
                    c.setLocator(locator);
                    return c;
                }, title
        );
        NetworkHooks.openGui((ServerPlayerEntity) player, container, locator.getBlockPos());

        return true;
    }

    @FunctionalInterface
    public interface ContainerFactory<T, C> {
        C create(int windowId, PlayerInventory playerInv, T part);
    }

}
