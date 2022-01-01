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

package appeng.menu.slot;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.features.GridLinkables;
import appeng.api.features.IGridLinkableHandler;
import appeng.api.ids.AETags;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.inventories.InternalInventory;
import appeng.api.storage.StorageCells;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.api.upgrades.Upgrades;
import appeng.blockentity.misc.InscriberRecipes;
import appeng.blockentity.misc.VibrationChamberBlockEntity;
import appeng.client.gui.Icon;
import appeng.core.definitions.AEItems;
import appeng.crafting.pattern.EncodedPatternItem;
import appeng.util.Platform;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class RestrictedInputSlot extends AppEngSlot {

    private final PlacableItemType which;
    private boolean allowEdit = true;
    private int stackLimit = -1;

    public RestrictedInputSlot(PlacableItemType valid, InternalInventory inv, int invSlot) {
        super(inv, invSlot);
        this.which = valid;
        this.setIcon(valid.icon);
    }

    @Override
    public int getMaxStackSize() {
        if (this.stackLimit != -1) {
            return this.stackLimit;
        }
        return super.getMaxStackSize();
    }

    public Slot setStackLimit(int i) {
        this.stackLimit = i;
        return this;
    }

    private Level getLevel() {
        return getMenu().getPlayerInventory().player.getCommandSenderWorld();
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        if (!this.getMenu().isValidForSlot(this, stack)) {
            return false;
        }

        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() == Items.AIR) {
            return false;
        }

        if (!super.mayPlace(stack)) {
            return false;
        }

        if (!this.isAllowEdit()) {
            return false;
        }

        // TODO: might need to check for our own patterns in some cases
        switch (this.which) {
            case ENCODED_AE_CRAFTING_PATTERN:
                return AEItems.CRAFTING_PATTERN.isSameAs(stack);
            case ENCODED_PATTERN:
                return PatternDetailsHelper.isEncodedPattern(stack);
            case ENCODED_AE_PATTERN:
                return AEItems.CRAFTING_PATTERN.isSameAs(stack)
                        || AEItems.PROCESSING_PATTERN.isSameAs(stack);
            case BLANK_PATTERN:
                return AEItems.BLANK_PATTERN.isSameAs(stack);

            case INSCRIBER_PLATE:
                if (AEItems.NAME_PRESS.isSameAs(stack)) {
                    return true;
                }

                return InscriberRecipes.isValidOptionalIngredient(getLevel(), stack);

            case INSCRIBER_INPUT:
                return true;/*
                             * for (ItemStack is : Inscribe.inputs) if ( Platform.isSameItemPrecise( is, i ) ) return
                             * true; return false;
                             */

            case METAL_INGOTS:

                return isMetalIngot(stack);

            case VIEW_CELL:
                return AEItems.VIEW_CELL.isSameAs(stack);
            case FUEL:
                return VibrationChamberBlockEntity.hasBurnTime(stack);
            case POWERED_TOOL:
                return Platform.isChargeable(stack);
            case QE_SINGULARITY:
                return AEItems.QUANTUM_ENTANGLED_SINGULARITY.isSameAs(stack);

            case RANGE_BOOSTER:
                return AEItems.WIRELESS_BOOSTER.isSameAs(stack);

            case SPATIAL_STORAGE_CELLS:
                return stack.getItem() instanceof ISpatialStorageCell
                        && ((ISpatialStorageCell) stack.getItem()).isSpatialStorage(stack);
            case STORAGE_CELLS:
                return StorageCells.isCellHandled(stack);
            case WORKBENCH_CELL:
                return stack.getItem() instanceof ICellWorkbenchItem
                        && ((ICellWorkbenchItem) stack.getItem()).isEditable(stack);
            case STORAGE_COMPONENT:
                return stack.getItem() instanceof IStorageComponent
                        && ((IStorageComponent) stack.getItem()).isStorageComponent(stack);
            case TRASH:
                if (StorageCells.isCellHandled(stack)) {
                    return false;
                }

                return !(stack.getItem() instanceof IStorageComponent
                        && ((IStorageComponent) stack.getItem()).isStorageComponent(stack));
            case GRID_LINKABLE_ITEM: {
                var handler = GridLinkables.get(stack.getItem());
                return handler != null && handler.canLink(stack);
            }
            case BIOMETRIC_CARD:
                return stack.getItem() instanceof IBiometricCard;
            case UPGRADES:
                return Upgrades.isUpgradeCardItem(stack);
            default:
                break;
        }

        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return this.isAllowEdit();
    }

    @Override
    public ItemStack getDisplayStack() {
        // If the slot only takes encoded patterns, show the encoded item instead
        if (isRemote() && this.which == PlacableItemType.ENCODED_PATTERN) {
            final ItemStack is = super.getDisplayStack();
            if (!is.isEmpty() && is.getItem() instanceof EncodedPatternItem iep) {
                final ItemStack out = iep.getOutput(is);
                if (!out.isEmpty()) {
                    return out;
                }
            }
        }
        return super.getDisplayStack();
    }

    public static boolean isMetalIngot(ItemStack i) {
        return AETags.METAL_INGOTS.contains(i.getItem());
    }

    private boolean isAllowEdit() {
        return this.allowEdit;
    }

    public void setAllowEdit(boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public enum PlacableItemType {
        STORAGE_CELLS(Icon.BACKGROUND_STORAGE_CELL),
        ORE(Icon.BACKGROUND_ORE),
        STORAGE_COMPONENT(Icon.BACKGROUND_STORAGE_COMPONENT),
        /**
         * Only allows items that have a registered {@link IGridLinkableHandler}.
         */
        GRID_LINKABLE_ITEM(Icon.BACKGROUND_WIRELESS_TERM),
        TRASH(Icon.BACKGROUND_TRASH),
        /**
         * Accepts {@link AEItems#CRAFTING_PATTERN} or {@link AEItems#PROCESSING_PATTERN}.
         */
        ENCODED_AE_PATTERN(Icon.BACKGROUND_ENCODED_PATTERN),
        /**
         * Only accepts {@link AEItems#CRAFTING_PATTERN}.
         */
        ENCODED_AE_CRAFTING_PATTERN(Icon.BACKGROUND_ENCODED_PATTERN),
        /**
         * An encoded pattern from any mod (AE2 or otherwise). Delegates to AE2 API to identify such items.
         */
        ENCODED_PATTERN(Icon.BACKGROUND_ENCODED_PATTERN),
        PATTERN(Icon.BACKGROUND_BLANK_PATTERN),
        BLANK_PATTERN(Icon.BACKGROUND_BLANK_PATTERN),
        POWERED_TOOL(Icon.BACKGROUND_CHARGABLE),
        RANGE_BOOSTER(Icon.BACKGROUND_WIRELESS_BOOSTER),
        QE_SINGULARITY(Icon.BACKGROUND_SINGULARITY),
        SPATIAL_STORAGE_CELLS(Icon.BACKGROUND_SPATIAL_CELL),
        FUEL(Icon.BACKGROUND_FUEL),
        UPGRADES(Icon.BACKGROUND_UPGRADE),
        WORKBENCH_CELL(Icon.BACKGROUND_STORAGE_CELL),
        BIOMETRIC_CARD(Icon.BACKGROUND_BIOMETRIC_CARD),
        VIEW_CELL(Icon.BACKGROUND_VIEW_CELL),
        INSCRIBER_PLATE(Icon.BACKGROUND_PLATE),
        INSCRIBER_INPUT(Icon.BACKGROUND_INGOT),
        METAL_INGOTS(Icon.BACKGROUND_INGOT);

        public final Icon icon;

        PlacableItemType(Icon o) {
            this.icon = o;
        }
    }
}
