package appeng.container.implementations;

import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.text.Text;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.world.World;

import appeng.api.AEApi;
import appeng.api.config.SecurityPermissions;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.implementations.guiobjects.IGuiItem;
import appeng.api.implementations.guiobjects.IGuiItemObject;
import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.container.AEBaseContainer;
import appeng.container.ContainerLocator;
import appeng.core.AELog;
import appeng.helpers.ICustomNameObject;
import appeng.helpers.WirelessTerminalGuiObject;
import appeng.util.Platform;

import javax.annotation.Nullable;

/**
 * Helper for containers that can be opened for a part <em>or</em> tile given
 * that either implements a given interface.
 *
 * @param <C>
 */
public final class ContainerHelper<C extends AEBaseContainer, I> {

    private final Class<I> interfaceClass;

    private final ContainerFactory<C, I> factory;

    private final SecurityPermissions requiredPermission;

    public ContainerHelper(ContainerFactory<C, I> factory, Class<I> interfaceClass) {
        this(factory, interfaceClass, null);
    }

    public ContainerHelper(ContainerFactory<C, I> factory, Class<I> interfaceClass,
            SecurityPermissions requiredPermission) {
        this.requiredPermission = requiredPermission;
        this.interfaceClass = interfaceClass;
        this.factory = factory;
    }

    /**
     * Opens a container that is based around a single block entity. The tile
     * entity's position is encoded in the packet buffer.
     */
    public C fromNetwork(int windowId, PlayerInventory inv, PacketByteBuf packetBuf) {
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

        if (accessInterface == null) {
            return false;
        }

        if (!checkPermission(player, accessInterface)) {
            return false;
        }

        Text title = findContainerTitle(player.world, locator, accessInterface);

        player.openHandledScreen(new HandlerFactory(locator, title, accessInterface));

        return true;
    }

    private class HandlerFactory implements ExtendedScreenHandlerFactory {

        private final ContainerLocator locator;

        private final I accessInterface;

        private final Text title;

        public HandlerFactory(ContainerLocator locator, Text title, I accessInterface) {
            this.locator = locator;
            this.title = title;
            this.accessInterface = accessInterface;
        }

        @Override
        public void writeScreenOpeningData(ServerPlayerEntity player, PacketByteBuf buf) {
            locator.write(buf);
        }

        @Override
        public Text getDisplayName() {
            return title;
        }

        @Nullable
        @Override
        public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
            C c = factory.create(syncId, inv, accessInterface);
            // Set the original locator on the opened server-side container for it to more
            // easily remember how to re-open after being closed.
            c.setLocator(locator);
            return c;
        }

    }

    private Text findContainerTitle(World world, ContainerLocator locator, I accessInterface) {

        if (accessInterface instanceof ICustomNameObject) {
            ICustomNameObject customNameObject = (ICustomNameObject) accessInterface;
            if (customNameObject.hasCustomInventoryName()) {
                return customNameObject.getCustomInventoryName();
            }
        }

        // Use block name at position
        // FIXME: this is not right, we'd need to check the part's item stack, or custom
        // naming interface impl
        // FIXME: Should move this up, because at this point, it's hard to know where
        // the terminal host came from (part or tile)
        if (locator.hasBlockPos()) {
            return new TranslatableText(
                    world.getBlockState(locator.getBlockPos()).getBlock().getTranslationKey());
        }

        return new LiteralText("Unknown");

    }

    private I getHostFromLocator(PlayerEntity player, ContainerLocator locator) {
        if (locator.hasItemIndex()) {
            return getHostFromPlayerInventory(player, locator);
        }

        if (!locator.hasBlockPos()) {
            return null; // No block was clicked
        }

        BlockEntity tileEntity = player.world.getBlockEntity(locator.getBlockPos());

        // The block entity itself can host a terminal (i.e. Chest!)
        if (interfaceClass.isInstance(tileEntity)) {
            return interfaceClass.cast(tileEntity);
        }

        if (!locator.hasSide()) {
            return null;
        }

        if (tileEntity instanceof IPartHost) {
            // But it could also be a part attached to the block entity
            IPartHost partHost = (IPartHost) tileEntity;
            IPart part = partHost.getPart(locator.getSide());
            if (part == null) {
                return null;
            }

            if (interfaceClass.isInstance(part)) {
                return interfaceClass.cast(part);
            } else {
                AELog.debug("Trying to open a container @ %s for a %s, but the container requires %s", locator,
                        part.getClass(), interfaceClass);
                return null;
            }
        } else {
            // FIXME: Logging? Dont know how to obtain the terminal host
            return null;
        }
    }

    private I getHostFromPlayerInventory(PlayerEntity player, ContainerLocator locator) {

        ItemStack it = player.inventory.getStack(locator.getItemIndex());

        if (it.isEmpty()) {
            AELog.debug("Cannot open container for player %s since they no longer hold the item in slot %d", player,
                    locator.hasItemIndex());
            return null;
        }

        if (it.getItem() instanceof IGuiItem) {
            IGuiItem guiItem = (IGuiItem) it.getItem();
            // Optionally contains the block the item was used on to open the container
            BlockPos blockPos = locator.hasBlockPos() ? locator.getBlockPos() : null;
            IGuiItemObject guiObject = guiItem.getGuiObject(it, locator.getItemIndex(), player.world, blockPos);
            if (interfaceClass.isInstance(guiObject)) {
                return interfaceClass.cast(guiObject);
            }
        }

        if (interfaceClass.isAssignableFrom(WirelessTerminalGuiObject.class)) {
            final IWirelessTermHandler wh = AEApi.instance().registries().wireless().getWirelessTerminalHandler(it);
            if (wh != null) {
                return interfaceClass.cast(new WirelessTerminalGuiObject(wh, it, player, locator.getItemIndex()));
            }
        }

        return null;
    }

    @FunctionalInterface
    public interface ContainerFactory<C, I> {
        C create(int windowId, PlayerInventory playerInv, I accessObj);
    }

    private boolean checkPermission(PlayerEntity player, Object accessInterface) {

        if (requiredPermission != null) {
            return Platform.checkPermissions(player, accessInterface, requiredPermission, true);
        }

        return true;

    }

}
