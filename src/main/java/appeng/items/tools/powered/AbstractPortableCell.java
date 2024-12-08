package appeng.items.tools.powered;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.config.Actionable;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.core.AEConfig;
import appeng.core.localization.PlayerMessages;
import appeng.items.contents.PortableCellMenuHost;
import appeng.menu.MenuOpener;
import appeng.menu.locator.ItemMenuHostLocator;
import appeng.menu.locator.MenuLocators;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import appeng.util.InteractionUtil;

public abstract class AbstractPortableCell extends PoweredContainerItem
        implements ICellWorkbenchItem, IMenuItem {

    private final MenuType<?> menuType;
    private final int defaultColor;

    public AbstractPortableCell(MenuType<?> menuType, Properties props, int defaultColor) {
        super(AEConfig.instance().getPortableCellBattery(), props);
        this.menuType = menuType;
        this.defaultColor = defaultColor;
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
    public boolean openFromInventory(Player player, ItemMenuHostLocator locator) {
        return openFromInventory(player, locator, false);
    }

    protected boolean openFromInventory(Player player, ItemMenuHostLocator locator, boolean returningFromSubmenu) {
        var is = locator.locateItem(player);
        if (is.getItem() == this) {
            return MenuOpener.open(this.menuType, player, locator,
                    returningFromSubmenu);
        } else {
            return false;
        }
    }

    @Nullable
    @Override
    public PortableCellMenuHost<?> getMenuHost(Player player, ItemMenuHostLocator locator,
            @Nullable BlockHitResult hitResult) {
        return new PortableCellMenuHost<>(this, player, locator,
                (p, sm) -> openFromInventory(p, locator, true));
    }

    // Override to change the default color
    public int getColor(ItemStack stack) {
        return DyedItemColor.getOrDefault(stack, defaultColor);
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
        var playerInventory = player.getInventory();
        var disassemblyItems = StorageCellDisassemblyRecipe.getDisassemblyResult(level, stack.getItem());
        if (disassemblyItems.isEmpty() || playerInventory.getSelected() != stack || stack.getCount() != 1) {
            return false;
        }

        if (level.isClientSide()) {
            return true; // Further checks cannot be done on the client
        }

        var inv = StorageCells.getCellInventory(stack, null);
        if (inv != null && !inv.getAvailableStacks().isEmpty()) {
            player.displayClientMessage(PlayerMessages.OnlyEmptyCellsCanBeDisassembled.text(), true);
            return true; // Prevents the UI from opening and overlaying the error message
        }

        playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);

        double remainingEnergy = getAECurrentPower(stack);
        for (var recipeStack : disassemblyItems) {
            var droppedStack = recipeStack.copy();
            // Dump remaining energy into whatever can accept it
            if (remainingEnergy > 0 && droppedStack.getItem() instanceof EnergyCellBlockItem energyCell) {
                remainingEnergy = energyCell.injectAEPower(droppedStack, remainingEnergy, Actionable.MODULATE);
            }

            playerInventory.placeItemBackInInventory(droppedStack);
        }

        // Drop upgrades
        getUpgrades(stack).forEach(playerInventory::placeItemBackInInventory);

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
        } else if (tintIndex == 2 && stack.getItem() instanceof AbstractPortableCell portableCell) {
            return portableCell.getColor(stack);
        } else {
            // White
            return 0xFFFFFF;
        }
    }
}
