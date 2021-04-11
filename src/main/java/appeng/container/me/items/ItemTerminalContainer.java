package appeng.container.me.items;

import javax.annotation.Nullable;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;

import appeng.api.config.Actionable;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.channels.IItemStorageChannel;
import appeng.api.storage.data.IAEItemStack;
import appeng.container.ContainerLocator;
import appeng.container.ContainerOpener;
import appeng.container.implementations.ContainerHelper;
import appeng.container.me.common.MEMonitorableContainer;
import appeng.container.me.crafting.CraftAmountContainer;
import appeng.core.AELog;
import appeng.core.Api;
import appeng.helpers.InventoryAction;
import appeng.util.InventoryAdaptor;
import appeng.util.Platform;
import appeng.util.inv.AdaptorItemHandler;
import appeng.util.inv.WrapperCursorItemHandler;
import appeng.util.item.AEItemStack;

public class ItemTerminalContainer extends MEMonitorableContainer<IAEItemStack> {

    public static ContainerType<ItemTerminalContainer> TYPE;

    private static final ContainerHelper<ItemTerminalContainer, ITerminalHost> helper = new ContainerHelper<>(
            ItemTerminalContainer::new, ITerminalHost.class);

    public static ItemTerminalContainer fromNetwork(int windowId, PlayerInventory inv, PacketBuffer buf) {
        return helper.fromNetwork(windowId, inv, buf);
    }

    public static boolean open(PlayerEntity player, ContainerLocator locator) {
        return helper.open(player, locator);
    }

    public ItemTerminalContainer(int id, PlayerInventory ip, ITerminalHost monitorable) {
        this(TYPE, id, ip, monitorable, true);
    }

    public ItemTerminalContainer(ContainerType<?> containerType, int id, PlayerInventory ip, ITerminalHost host,
            boolean bindInventory) {
        super(containerType, id, ip, host, bindInventory,
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class));
    }

    @Override
    protected void handleNetworkInteraction(ServerPlayerEntity player, @Nullable IAEItemStack stack,
            InventoryAction action) {

        // Handle interactions where the player wants to put something into the network
        if (stack == null) {
            if (action == InventoryAction.SPLIT_OR_PLACE_SINGLE || action == InventoryAction.ROLL_DOWN) {
                putHeldItemIntoNetwork(player, true);
            } else if (action == InventoryAction.PICKUP_OR_SET_DOWN) {
                putHeldItemIntoNetwork(player, false);
            }
            return;
        }

        switch (action) {
            case AUTO_CRAFT:
                final ContainerLocator locator = getLocator();
                if (locator != null) {
                    ContainerOpener.openContainer(CraftAmountContainer.TYPE, player, locator);

                    if (player.openContainer instanceof CraftAmountContainer) {
                        CraftAmountContainer cca = (CraftAmountContainer) player.openContainer;
                        cca.setItemToCraft(stack);
                        cca.detectAndSendChanges();
                    }
                }
                break;

            case SHIFT_CLICK:
                moveOneStackToPlayer(stack, player);
                break;

            case ROLL_DOWN: {
                final int releaseQty = 1;
                final ItemStack isg = player.inventory.getItemStack();

                if (!isg.isEmpty() && releaseQty > 0) {
                    IAEItemStack ais = Api.instance().storage().getStorageChannel(IItemStorageChannel.class)
                            .createStack(isg);
                    ais.setStackSize(1);
                    final IAEItemStack extracted = ais.copy();

                    ais = Platform.poweredInsert(powerSource, monitor, ais,
                            this.getActionSource());
                    if (ais == null) {
                        final InventoryAdaptor ia = new AdaptorItemHandler(
                                new WrapperCursorItemHandler(player.inventory));

                        final ItemStack fail = ia.removeItems(1, extracted.getDefinition(), null);
                        if (fail.isEmpty()) {
                            monitor.extractItems(extracted, Actionable.MODULATE,
                                    this.getActionSource());
                        }

                        this.updateHeld(player);
                    }
                }
            }
                break;
            case ROLL_UP:
            case PICKUP_SINGLE:
                int liftQty = 1;
                final ItemStack item = player.inventory.getItemStack();

                if (!item.isEmpty()) {
                    if (item.getCount() >= item.getMaxStackSize()) {
                        liftQty = 0;
                    }
                    if (!Platform.itemComparisons().isSameItem(stack.getDefinition(), item)) {
                        liftQty = 0;
                    }
                }

                if (liftQty > 0) {
                    IAEItemStack ais = stack.copy();
                    ais.setStackSize(1);
                    ais = Platform.poweredExtraction(powerSource, monitor, ais,
                            this.getActionSource());
                    if (ais != null) {
                        final InventoryAdaptor ia = new AdaptorItemHandler(
                                new WrapperCursorItemHandler(player.inventory));

                        final ItemStack fail = ia.addItems(ais.createItemStack());
                        if (!fail.isEmpty()) {
                            monitor.injectItems(ais, Actionable.MODULATE, this.getActionSource());
                        }

                        this.updateHeld(player);
                    }
                }
                break;
            case PICKUP_OR_SET_DOWN:
                if (!player.inventory.getItemStack().isEmpty()) {
                    putHeldItemIntoNetwork(player, false);
                } else {
                    IAEItemStack ais = stack.copy();
                    ais.setStackSize(ais.getDefinition().getMaxStackSize());
                    ais = Platform.poweredExtraction(powerSource, monitor, ais,
                            this.getActionSource());
                    if (ais != null) {
                        player.inventory.setItemStack(ais.createItemStack());
                    } else {
                        player.inventory.setItemStack(ItemStack.EMPTY);
                    }
                    this.updateHeld(player);
                }

                break;
            case SPLIT_OR_PLACE_SINGLE:
                if (!player.inventory.getItemStack().isEmpty()) {
                    putHeldItemIntoNetwork(player, true);
                } else {
                    IAEItemStack ais = stack.copy();
                    final long maxSize = ais.getDefinition().getMaxStackSize();
                    ais.setStackSize(maxSize);
                    ais = monitor.extractItems(ais, Actionable.SIMULATE, this.getActionSource());

                    if (ais != null) {
                        final long stackSize = Math.min(maxSize, ais.getStackSize());
                        ais.setStackSize((stackSize + 1) >> 1);
                        ais = Platform.poweredExtraction(powerSource, monitor, ais,
                                this.getActionSource());
                    }

                    if (ais != null) {
                        player.inventory.setItemStack(ais.createItemStack());
                    } else {
                        player.inventory.setItemStack(ItemStack.EMPTY);
                    }
                    this.updateHeld(player);
                }

                break;
            case CREATIVE_DUPLICATE:
                if (player.abilities.isCreativeMode) {
                    final ItemStack is = stack.createItemStack();
                    is.setCount(is.getMaxStackSize());
                    player.inventory.setItemStack(is);
                    this.updateHeld(player);
                }
                break;
            case MOVE_REGION:
                final int playerInv = player.inventory.mainInventory.size();
                for (int slotNum = 0; slotNum < playerInv; slotNum++) {
                    if (!moveOneStackToPlayer(stack, player)) {
                        break;
                    }
                }
                break;
            default:
                AELog.warn("Received unhandled inventory action %s from client in %s", action, getClass());
                break;
        }
    }

    protected void putHeldItemIntoNetwork(ServerPlayerEntity player, boolean singleItem) {
        ItemStack heldStack = player.inventory.getItemStack();

        IAEItemStack stackToInsert = AEItemStack.fromItemStack(heldStack);
        if (stackToInsert == null) {
            return;
        }

        if (singleItem) {
            stackToInsert.setStackSize(1);
        }

        IAEItemStack remainder = Platform.poweredInsert(powerSource, monitor, stackToInsert, this.getActionSource());
        long inserted = stackToInsert.getStackSize() - (remainder == null ? 0 : remainder.getStackSize());

        if (inserted >= heldStack.getCount()) {
            player.inventory.setItemStack(ItemStack.EMPTY);
        } else {
            heldStack = heldStack.copy();
            heldStack.setCount(heldStack.getCount() - (int) inserted);
            player.inventory.setItemStack(heldStack);
        }

        this.updateHeld(player);
    }

    private boolean moveOneStackToPlayer(IAEItemStack stack, ServerPlayerEntity player) {
        IAEItemStack ais = stack.copy();
        ItemStack myItem = ais.createItemStack();

        ais.setStackSize(myItem.getMaxStackSize());

        final InventoryAdaptor adp = InventoryAdaptor.getAdaptor(player);
        myItem.setCount((int) ais.getStackSize());
        myItem = adp.simulateAdd(myItem);

        if (!myItem.isEmpty()) {
            ais.setStackSize(ais.getStackSize() - myItem.getCount());
        }
        if (ais.getStackSize() <= 0) {
            return false;
        }

        ais = Platform.poweredExtraction(powerSource, monitor, ais, getActionSource());

        return ais != null && adp.addItems(ais.createItemStack()).isEmpty();
    }

    @Override
    protected ItemStack transferStackToContainer(ItemStack input) {
        if (!canInteractWithGrid()) {
            return super.transferStackToContainer(input);
        }

        final IAEItemStack ais = Platform.poweredInsert(powerSource, monitor,
                Api.instance().storage().getStorageChannel(IItemStorageChannel.class).createStack(input),
                this.getActionSource());
        return ais == null ? ItemStack.EMPTY : ais.createItemStack();
    }

}
