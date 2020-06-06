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
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.network.NetworkHooks;

/**
 * Helper for containers that can be opened for a part <em>or</em> tile given that either
 * implements a given interface.
 *
 * @param <C>
 */
// FIXME: This is also used in contexts where access is via an item that implements I or exposes I via IGuiItemObject
public final class PartOrTileContainerHelper<C extends AEBaseContainer, I> {

    private final Class<I> interfaceClass;

    private final ContainerFactory<C, I> factory;

    private final SecurityPermissions requiredPermission;

    public PartOrTileContainerHelper(ContainerFactory<C, I> factory, Class<I> interfaceClass) {
        this(factory, interfaceClass, null);
    }

    public PartOrTileContainerHelper(ContainerFactory<C, I> factory, Class<I> interfaceClass, SecurityPermissions requiredPermission) {
        this.interfaceClass = interfaceClass;
        this.factory = factory;
        this.requiredPermission = requiredPermission;
    }

    /**
     * Opens a container that is based around a single tile entity.
     * The tile entity's position is encoded in the packet buffer.
     */
    public C fromNetwork(int windowId, PlayerInventory inv, PacketBuffer packetBuf) {
        I host = getHostFromLocator(inv.player, ContainerLocator.read(packetBuf));
        if (host != null) {
            return factory.create(windowId, inv, host);
        }
        return null;
    }

    public boolean open(PlayerEntity player, ContainerLocator locator) {
        if (!(player instanceof ServerPlayerEntity)) {
            // Cannot open containers on the client or for non-players
            // FIXME logging?
            return false;
        }

        I accessInterface = getHostFromLocator(player, locator);

        // Use block name at position
        // FIXME: this is not right, we'd need to check the part's item stack, or custom naming interface impl
        // FIXME: Should move this up, because at this point, it's hard to know where the terminal host came from (part or tile)
        ITextComponent title = player.world.getBlockState(locator.getBlockPos()).getBlock().getNameTextComponent();

        // FIXME: Check permissions...
        if (requiredPermission != null) {
            throw new IllegalStateException(); // NOT YET IMPLEMENTED
        }

        INamedContainerProvider container = new SimpleNamedContainerProvider(
                (wnd, p, pl) -> {
                    C c = factory.create(wnd, p, accessInterface);
                    // Set the original locator on the opened server-side container for it to more
                    // easily remember how to re-open after being closed.
                    c.setLocator(locator);
                    return c;
                }, title
        );
        NetworkHooks.openGui((ServerPlayerEntity) player, container, locator.getBlockPos());

        return true;
    }

    private I getHostFromLocator(PlayerEntity player, ContainerLocator locator) {
        if (!locator.hasBlockPos() || !locator.hasSide()) {
            return null; // No block was clicked or the side is unknown
            // FIXME: If no side is provided, should be try with INTERNAL???
        }

        TileEntity tileEntity = player.world.getTileEntity(locator.getBlockPos());
        // The tile entity itself can host a terminal (i.e. Chest!)
        if (interfaceClass.isInstance(tileEntity)) {
            return interfaceClass.cast(tileEntity);
        } else if (tileEntity instanceof IPartHost) {
            // But it could also be a part attached to the tile entity
            IPartHost partHost = (IPartHost) tileEntity;
            IPart part = partHost.getPart(locator.getSide());
            if (interfaceClass.isInstance(part)) {
                return interfaceClass.cast(part);
            } else {
                // FIXME: Logging?
                return null;
            }
        } else {
            // FIXME: Logging? Dont know how to obtain the terminal host
            return null;
        }
    }

    @FunctionalInterface
    public interface ContainerFactory<C, I> {
        C create(int windowId, PlayerInventory playerInv, I accessObj);
    }

}
