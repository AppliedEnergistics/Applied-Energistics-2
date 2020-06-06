package appeng.container.helper;

import appeng.api.config.SecurityPermissions;
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

public final class TileContainerHelper<C extends AEBaseContainer, T extends TileEntity> {

    private final Class<T> tileEntityClass;

    private final ContainerFactory<T, C> factory;

    private final SecurityPermissions requiredPermission;

    public TileContainerHelper(ContainerFactory<T, C> factory, Class<T> tileEntityClass) {
        this(factory, tileEntityClass, null);
    }

    public TileContainerHelper(ContainerFactory<T, C> factory, Class<T> tileEntityClass, SecurityPermissions requiredPermission) {
        this.tileEntityClass = tileEntityClass;
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
        if (tileEntityClass.isInstance(te)) {
            return factory.create(windowId, inv, tileEntityClass.cast(te));
        }
        return null;
    }

    public boolean open(PlayerEntity player, ContainerLocator locator) {
        if (!(player instanceof ServerPlayerEntity)) {
            // Cannot open containers on the client or for non-players
            return false;
        }

        if (!locator.hasBlockPos()) {
            return false; // No block was clicked
        }

        TileEntity tileEntity = player.world.getTileEntity(locator.getBlockPos());
        if (!tileEntityClass.isInstance(tileEntity)) {
            return false; // Wrong tile entity at block position
        }
        T te = tileEntityClass.cast(tileEntity);

        // Use block name at position
        ITextComponent title = player.world.getBlockState(locator.getBlockPos()).getBlock().getNameTextComponent();

        // FIXME: Check permissions...
        if (requiredPermission != null) {
            throw new IllegalStateException(); // NOT YET IMPLEMENTED
        }

        INamedContainerProvider container = new SimpleNamedContainerProvider(
                (wnd, p, pl) -> {
                    C c = factory.create(wnd, p, te);
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
        C create(int windowId, PlayerInventory playerInv, T tileEntity);
    }

}
