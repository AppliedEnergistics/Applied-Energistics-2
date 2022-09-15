/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2014, AlgorithmX2, All rights reserved.
 *
 * Applied Energistics 2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Applied Energistics 2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Applied Energistics 2.  If not, see <http://www.gnu.org/licenses/lgpl>.
 */

package appeng.items.tools.powered;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEItemIds;
import appeng.api.implementations.menuobjects.IMenuItem;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.StorageCells;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableItem;
import appeng.api.upgrades.UpgradeInventories;
import appeng.block.AEBaseBlockItemChargeable;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.core.definitions.AEItems;
import appeng.core.localization.PlayerMessages;
import appeng.helpers.FluidContainerHelper;
import appeng.hooks.AEToolItem;
import appeng.items.contents.CellConfig;
import appeng.items.contents.PortableCellMenuHost;
import appeng.items.tools.powered.powersink.AEBasePoweredItem;
import appeng.me.helpers.PlayerSource;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.fluid.FluidSoundHelper;

public class PortableCellItem extends AEBasePoweredItem
        implements IBasicCellItem, IMenuItem, IUpgradeableItem, AEToolItem {

    public static final StorageTier SIZE_1K = new StorageTier("1k", 512, 54, 8,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_1K));
    public static final StorageTier SIZE_4K = new StorageTier("4k", 2048, 45, 32,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_4K));
    public static final StorageTier SIZE_16K = new StorageTier("16k", 8192, 36, 128,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_16K));
    public static final StorageTier SIZE_64K = new StorageTier("64k", 32768, 27, 512,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_64K));
    public static final StorageTier SIZE_256K = new StorageTier("256k", 131072, 18, 2048,
            () -> Registry.ITEM.get(AEItemIds.CELL_COMPONENT_256K));

    /**
     * Gets the recipe ID for crafting this particular cell.
     */
    public ResourceLocation getRecipeId() {
        return AppEng.makeId("tools/" + Objects.requireNonNull(getRegistryName()).getPath());
    }

    private final StorageTier tier;
    private final AEKeyType keyType;
    private final MenuType<?> menuType;

    public PortableCellItem(AEKeyType keyType, MenuType<?> menuType, StorageTier tier, Properties props) {
        super(AEConfig.instance().getPortableCellBattery(), props);
        this.menuType = menuType;
        this.tier = tier;
        this.keyType = keyType;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * getUpgrades(stack).getInstalledUpgrades(AEItems.ENERGY_CARD);
    }

    /**
     * Open a Portable Cell from a slot in the player inventory, i.e. activated via hotkey.
     *
     * @return True if the menu was opened.
     */
    public boolean openFromInventory(Player player, int inventorySlot) {
        var is = player.getInventory().getItem(inventorySlot);
        if (is.getItem() == this) {
            return MenuOpener.open(getMenuType(), player, MenuLocators.forInventorySlot(inventorySlot));
        } else {
            return false;
        }
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
                MenuOpener.open(getMenuType(), player, MenuLocators.forHand(player, hand));
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
                if (remainingEnergy > 0 && ingredientStack.getItem() instanceof AEBaseBlockItemChargeable chargeable) {
                    remainingEnergy = chargeable.injectAEPower(ingredientStack, remainingEnergy, Actionable.MODULATE);
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
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> lines,
            TooltipFlag advancedTooltips) {
        super.appendHoverText(stack, level, lines, advancedTooltips);
        addCellInformationToTooltip(stack, lines);
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return getCellTooltipImage(stack);
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return this.tier.bytes();
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.tier.bytesPerType();
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return this.tier.types();
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public boolean isEditable(ItemStack is) {
        return true;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, 2, this::onUpgradesChanged);
    }

    private void onUpgradesChanged(ItemStack stack, IUpgradeInventory upgrades) {
        var energyCards = upgrades.getInstalledUpgrades(AEItems.ENERGY_CARD);
        // The energy card is crafted with a dense energy cell, while the portable cell just uses a normal energy cell
        // Since the dense cells capacity is 8x the normal capacity, the result should be 9x normal.
        setAEMaxPowerMultiplier(stack, 1 + energyCards * 8);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(keyType.filter(), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        final String fz = is.getOrCreateTag().getString("FuzzyMode");
        try {
            return FuzzyMode.valueOf(fz);
        } catch (Throwable t) {
            return FuzzyMode.IGNORE_ALL;
        }
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.getOrCreateTag().putString("FuzzyMode", fzMode.name());
    }

    @Override
    public PortableCellMenuHost getMenuHost(Player player, int inventorySlot, ItemStack stack, BlockPos pos) {
        return new PortableCellMenuHost(player, inventorySlot, this, stack,
                (p, sm) -> openFromInventory(p, inventorySlot));
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    /**
     * Tries inserting into a portable cell without having to open it.
     *
     * @return Amount inserted.
     */
    public long insert(Player player, ItemStack itemStack, AEKey what, long amount, Actionable mode) {
        if (keyType.tryCast(what) == null) {
            return 0;
        }

        var host = getMenuHost(player, -1, itemStack, null);
        if (host == null) {
            return 0;
        }

        var inv = host.getInventory();
        if (inv != null) {
            return StorageHelper.poweredInsert(
                    host,
                    inv,
                    what,
                    amount,
                    new PlayerSource(player),
                    mode);
        }
        return 0;
    }

    public record StorageTier(String namePrefix, int bytes, int types, int bytesPerType,
            Supplier<Item> componentSupplier) {
    }

    @Override
    public AEKeyType getKeyType() {
        return keyType;
    }

    public MenuType<?> getMenuType() {
        return menuType;
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction action, Player player) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        var other = slot.getItem();
        if (other.isEmpty()) {
            return true;
        }

        if (keyType == AEKeyType.items()) {
            AEKey key = AEItemKey.of(other);
            int inserted = (int) insert(player, stack, key, other.getCount(), Actionable.MODULATE);
            other.shrink(inserted);

        } else if (keyType == AEKeyType.fluids()) {
            GenericStack fluidStack = FluidContainerHelper.getContainedStack(other);
            if (fluidStack != null) {
                if (insert(player, stack, fluidStack.what(), fluidStack.amount(), Actionable.SIMULATE) == fluidStack
                        .amount()) {
                    var extracted = FluidContainerHelper.extractFromPlayerInventory(player,
                            (AEFluidKey) fluidStack.what(),
                            fluidStack.amount(),
                            other);
                    if (extracted > 0) {
                        insert(player, stack, fluidStack.what(), extracted, Actionable.MODULATE);
                        FluidSoundHelper.playEmptySound(player, (AEFluidKey) fluidStack.what());
                    }
                }
            }
        }
        return true;
    }

    /**
     * Allows directly inserting items and fluids into portable cells by right-clicking the cell with the item or bucket
     * in hand.
     */
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack stack, ItemStack other, Slot slot, ClickAction action,
            Player player, SlotAccess access) {
        if (action != ClickAction.SECONDARY || !slot.allowModification(player)) {
            return false;
        }

        if (other.isEmpty()) {
            return false;
        }

        if (keyType == AEKeyType.items()) {
            AEKey key = AEItemKey.of(other);
            int inserted = (int) insert(player, stack, key, other.getCount(), Actionable.MODULATE);
            other.shrink(inserted);

        } else if (keyType == AEKeyType.fluids()) {
            GenericStack fluidStack = FluidContainerHelper.getContainedStack(other);
            if (fluidStack != null) {
                if (insert(player, stack, fluidStack.what(), fluidStack.amount(), Actionable.SIMULATE) == fluidStack
                        .amount()) {
                    var extracted = FluidContainerHelper.extractFromCarried(player, (AEFluidKey) fluidStack.what(),
                            fluidStack.amount(), other);
                    if (extracted > 0) {
                        insert(player, stack, fluidStack.what(), extracted, Actionable.MODULATE);
                        FluidSoundHelper.playEmptySound(player, (AEFluidKey) fluidStack.what());
                    }
                }
            }
        }
        return true;
    }

    public StorageTier getTier() {
        return tier;
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1 && stack.getItem() instanceof PortableCellItem portableCellItem) {
            // If the cell is out of power, always display empty
            if (portableCellItem.getAECurrentPower(stack) <= 0) {
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
