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

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidStorage;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import appeng.api.behaviors.GenericContainerHelper;
import appeng.api.config.Actionable;
import appeng.api.config.FuzzyMode;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.IBasicCellItem;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.UpgradeInventories;
import appeng.api.upgrades.Upgrades;
import appeng.core.AppEng;
import appeng.items.contents.CellConfig;
import appeng.items.storage.StorageTier;
import appeng.util.ConfigInventory;
import appeng.util.IVariantConversion;
import appeng.util.fluid.FluidSoundHelper;

public class PortableCellItem extends AbstractPortableCell implements IBasicCellItem {

    private final StorageTier tier;
    private final AEKeyType keyType;

    public PortableCellItem(AEKeyType keyType, MenuType<?> menuType, StorageTier tier, Properties props) {
        super(menuType, props);
        this.tier = tier;
        this.keyType = keyType;
    }

    @Override
    public double getChargeRate(ItemStack stack) {
        return 80d + 80d * Upgrades.getEnergyCardMultiplier(getUpgrades(stack));
    }

    @Override
    public ResourceLocation getRecipeId() {
        return AppEng.makeId("tools/" + Objects.requireNonNull(getRegistryName()).getPath());
    }

    @Override
    @Environment(EnvType.CLIENT)
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
        return this.tier.bytes() / 2;
    }

    @Override
    public int getBytesPerType(ItemStack cellItem) {
        return this.tier.bytes() / 128;
    }

    @Override
    public int getTotalTypes(ItemStack cellItem) {
        return 63 - this.tier.index() * 9;
    }

    @Override
    public double getIdleDrain() {
        return 0.5;
    }

    @Override
    public IUpgradeInventory getUpgrades(ItemStack is) {
        return UpgradeInventories.forItem(is, this.keyType == AEKeyType.items() ? 4 : 3, super::onUpgradesChanged);
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
    public AEKeyType getKeyType() {
        return keyType;
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
            int inserted = (int) insert(player, stack, key, keyType, other.getCount(), Actionable.MODULATE);
            other.shrink(inserted);

        } else if (keyType == AEKeyType.fluids()) {
            GenericStack fluidStack = GenericContainerHelper.getContainedStack(other, FluidStorage.ITEM,
                    IVariantConversion.FLUID);
            if (fluidStack != null) {
                if (insert(player, stack, fluidStack.what(), keyType, fluidStack.amount(),
                        Actionable.SIMULATE) == fluidStack.amount()) {
                    var extracted = GenericContainerHelper.extractFromPlayerInventory(player,
                            (AEFluidKey) fluidStack.what(), fluidStack.amount(), other, FluidStorage.ITEM,
                            IVariantConversion.FLUID);
                    if (extracted > 0) {
                        insert(player, stack, fluidStack.what(), keyType, extracted, Actionable.MODULATE);
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
            int inserted = (int) insert(player, stack, key, keyType, other.getCount(), Actionable.MODULATE);
            other.shrink(inserted);

        } else if (keyType == AEKeyType.fluids()) {
            GenericStack fluidStack = GenericContainerHelper.getContainedStack(other, FluidStorage.ITEM,
                    IVariantConversion.FLUID);
            if (fluidStack != null) {
                if (insert(player, stack, fluidStack.what(), keyType, fluidStack.amount(),
                        Actionable.SIMULATE) == fluidStack.amount()) {
                    var extracted = GenericContainerHelper.extractFromCarried(player, (AEFluidKey) fluidStack.what(),
                            fluidStack.amount(), other, FluidStorage.ITEM, IVariantConversion.FLUID);
                    if (extracted > 0) {
                        insert(player, stack, fluidStack.what(), keyType, extracted, Actionable.MODULATE);
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
}
