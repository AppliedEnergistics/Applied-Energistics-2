/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2018, AlgorithmX2, All rights reserved.
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

package appeng.items.storage;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import appeng.api.config.Actionable;
import appeng.block.networking.EnergyCellBlockItem;
import appeng.core.AELog;
import appeng.core.AppEng;
import appeng.recipes.game.StorageCellDisassemblyRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

import appeng.api.config.FuzzyMode;
import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.core.localization.PlayerMessages;
import appeng.hooks.AEToolItem;
import appeng.items.AEBaseItem;
import appeng.items.contents.CellConfig;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import appeng.util.Platform;

public class BasicStorageCell extends AEBaseItem implements IBasicCellItem, AEToolItem {
    /**
     * This can be retrieved when disassembling the storage cell.
     */
    @Deprecated(forRemoval = true, since = "1.21.1")
    protected final ItemLike coreItem;
    @Deprecated(forRemoval = true, since = "1.21.1")
    protected final ItemLike housingItem;
    protected final double idleDrain;
    protected final int totalBytes;
    protected final int bytesPerType;
    protected final int totalTypes;
    private final AEKeyType keyType;

    @Deprecated(forRemoval = true, since = "1.21.1")
    public BasicStorageCell(Properties properties,
            ItemLike coreItem,
            ItemLike housingItem,
            double idleDrain,
            int kilobytes,
            int bytesPerType,
            int totalTypes,
            AEKeyType keyType) {
        super(properties);
        this.idleDrain = idleDrain;
        this.totalBytes = kilobytes * 1024;
        this.coreItem = coreItem;
        this.housingItem = housingItem;
        this.bytesPerType = bytesPerType;
        this.totalTypes = totalTypes;
        this.keyType = keyType;
    }

    public BasicStorageCell(Properties properties,
            double idleDrain,
            int kilobytes,
            int bytesPerType,
            int totalTypes,
            AEKeyType keyType) {
        super(properties);
        this.idleDrain = idleDrain;
        this.totalBytes = kilobytes * 1024;
        this.bytesPerType = bytesPerType;
        this.totalTypes = totalTypes;
        this.keyType = keyType;
        //TODO: Remove when deprecated fields are removed.
        this.housingItem = null;
        this.coreItem = null;
    }

    @Override
    public void appendHoverText(ItemStack stack,
            TooltipContext context,
            List<Component> lines,
            TooltipFlag advancedTooltips) {
        if (Platform.isClient()) {
            addCellInformationToTooltip(stack, lines);
        }
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        return getCellTooltipImage(stack);
    }

    @Override
    public AEKeyType getKeyType() {
        return this.keyType;
    }

    @Override
    public int getBytes(ItemStack cellItem) {
        return this.totalBytes;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return totalTypes;
    }

    @Override
    public double getIdleDrain() {
        return idleDrain;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return bytesPerType;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, keyType == AEKeyType.items() ? 4 : 3);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return CellConfig.create(Set.of(keyType), is);
    }

    @Override
    public FuzzyMode getFuzzyMode(ItemStack is) {
        return is.getOrDefault(AEComponents.STORAGE_CELL_FUZZY_MODE, FuzzyMode.IGNORE_ALL);
    }

    @Override
    public void setFuzzyMode(ItemStack is, FuzzyMode fzMode) {
        is.set(AEComponents.STORAGE_CELL_FUZZY_MODE, fzMode);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (InteractionUtil.isInAlternateUseMode(player)) this.disassembleDrive(player.getItemInHand(hand), level, player);
        return new InteractionResultHolder<>(InteractionResult.sidedSuccess(level.isClientSide()),
                player.getItemInHand(hand));
    }

    private boolean disassembleDrive(ItemStack stack, Level level, Player player) {
        if (level.isClientSide()) return false;
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());

        if (itemId == BuiltInRegistries.ITEM.getDefaultKey()) {
            AELog.debug("Cannot disassemble storage cell because its item is unregistered?");
            return false;
        }

        var recipe = StorageCellDisassemblyRecipe.getDisassemblyRecipe(level, AppEng.makeId("upgrade/" + itemId.getPath()), stack.getItem());
        if (recipe == null) return false;

        final Inventory playerInventory = player.getInventory();
        var inv = StorageCells.getCellInventory(stack, null);

        if (inv == null || playerInventory.getSelected() != stack) return false;

        if (!inv.getAvailableStacks().isEmpty()) {
            player.displayClientMessage(PlayerMessages.OnlyEmptyCellsCanBeDisassembled.text(), true);
            return false;
        }

        playerInventory.setItem(playerInventory.selected, ItemStack.EMPTY);

        // Drop items from the recipe.
        recipe.getCellDisassemblyItems().forEach(playerInventory::placeItemBackInInventory);

        // Drop upgrades
        getUpgrades(stack).forEach(playerInventory::placeItemBackInInventory);

        return true;
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        return this.disassembleDrive(stack, context.getLevel(), context.getPlayer())
                ? InteractionResult.sidedSuccess(context.getLevel().isClientSide())
                : InteractionResult.PASS;
    }

    public static int getColor(ItemStack stack, int tintIndex) {
        if (tintIndex == 1) {
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
