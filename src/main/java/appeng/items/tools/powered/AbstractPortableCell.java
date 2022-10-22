package appeng.items.tools.powered;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;

import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;
import appeng.hooks.AEToolItem;
import appeng.items.contents.PortableCellMenuHost;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.InteractionUtil;

public abstract class AbstractPortableCell extends AEBasePoweredItem
        implements ICellWorkbenchItem, IMenuItem, AEToolItem {

    private final MenuType<?> menuType;

    public AbstractPortableCell(MenuType<?> menuType, Properties props) {
        super(AEConfig.instance().getPortableCellBattery(), props);
        this.menuType = menuType;
    }

    /**
     * Gets the recipe ID for crafting this particular cell.
     */
    public abstract ResourceLocation getRecipeId();

    @Override
    public abstract double getChargeRate(ItemStack stack);

    /**
     * Open a Portable Cell from a slot in the player inventory, i.e. activated via hotkey.
     *
     * @return True if the menu was opened.
     */
    public boolean openFromInventory(Player player, int inventorySlot) {
        return openFromInventory(player, inventorySlot, false);
    }

    protected boolean openFromInventory(Player player, int inventorySlot, boolean returningFromSubmenu) {
        var is = player.getInventory().getItem(inventorySlot);
        if (is.getItem() == this) {
            return MenuOpener.open(this.menuType, player, MenuLocators.forInventorySlot(inventorySlot),
                    returningFromSubmenu);
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public PortableCellMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, @Nullable BlockPos pos) {
        return new PortableCellMenuHost(player, inventorySlot, this, stack,
                (p, sm) -> openFromInventory(p, inventorySlot, true));
    }

    @Override
    public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack oldStack,
            ItemStack newStack) {
        return false;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return context.isSecondaryUseActive()
                && this.disassembleDrive(stack, context.getLevel(), context.getPlayer())
                        ? InteractionResult.sidedSuccess(context.getLevel().isClientSide())
                        : InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (!InteractionUtil.isInAlternateUseMode(player)
                || !disassembleDrive(player.getItemInHand(hand), level, player)) {
            if (!level.isClientSide()) {
                MenuOpener.open(this.menuType, player, MenuLocators.forHand(player, hand));
            }
        }
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    private boolean disassembleDrive(ItemStack stack, Level level, Player player) {
        if (!AEConfig.instance().isPortableCellDisassemblyEnabled()) {
            return false;
        }

        // We refund the crafting recipe ingredients (the first one each)
        var recipe = level.getRecipeManager().byKey(getRecipeId()).orElse(null);
        if (!(recipe instanceof CraftingRecipe craftingRecipe)) {
            AELog.debug("Cannot disassemble portable cell because it's crafting recipe doesn't exist: %s",
                    getRecipeId());
            return false;
        }

        if (level.isClientSide()) {
            return true;
        }

        var playerInventory = player.getInventory();
        if (playerInventory.getSelected() != stack) {
            return false;
        }

        var inv = StorageCells.getCellInventory(stack, null);
        if (inv == null) {
            return false;
        }

        if (inv.getAvailableStacks().isEmpty()) {
            playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);

            var remainingEnergy = getAECurrentPower(stack);
            for (var ingredient : craftingRecipe.getIngredients()) {
                var ingredientStack = ingredient.getItems()[0].copy();

                // Dump remaining energy into whatever can accept it
                if (remainingEnergy > 0 && ingredientStack.getItem() instanceof EnergyCellBlockItem energyCell) {
                    remainingEnergy = energyCell.injectAEPower(ingredientStack, remainingEnergy, Actionable.MODULATE);
                }

                playerInventory.placeItemBackInInventory(ingredientStack);
            }

            // Drop upgrades
            for (var upgrade : getUpgrades(stack)) {
                playerInventory.placeItemBackInInventory(upgrade);
            }
        } else {
            player.sendSystemMessage(PlayerMessages.OnlyEmptyCellsCanBeDisassembled.text());
        }

        return true;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2, this::onUpgradesChanged);
    }

    public void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        // The energy card is crafted with a dense energy cell, while the base portable just uses a normal energy cell.
        // Since the dense cells capacity is 8x the normal capacity, the result should be 9x normal.
        setAEMaxPowerMultiplier(stack, 1 + Upgrades.getEnergyCardMultiplier(upgrades) * 8);
    }

    /**
     * Tries inserting into a portable cell without having to open it.
     *
     * @return Amount inserted.
     */
    public long insert(Player player, ItemStack stack, AEKey what, AEKeyType allowed, long amount, Actionable mode) {
        if (allowed.tryCast(what) == null) {
            return 0;
        }

        var host = getMenuHost(player, -1, stack, null);
        if (host == null) {
            return 0;
        }

        var inv = host.getInventory();
        if (inv != null) {
            return StorageHelper.poweredInsert(host, inv, what, amount, new PlayerSource(player), mode);
        }
        return 0;
    };

    @Override
    public abstract boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player);

    @Override
    public abstract boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access);

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1 && stack.getItem() instanceof AbstractPortableCell portableCell) {
            // If the cell is out of power, always display empty
            if (portableCell.getAECurrentPower(stack) <= 0) {
                return CellState.ABSENT.getStateColor();
            }

            // Determine LED color
            var cellInv = StorageCells.getCellInventory(stack, null);
            var cellStatus = cellInv != null ? cellInv.getStatus() : CellState.EMPTY;
            return cellStatus.getStateColor();
        } else {
            // White
            return 0xFFFFFF;
        }
    }
}
