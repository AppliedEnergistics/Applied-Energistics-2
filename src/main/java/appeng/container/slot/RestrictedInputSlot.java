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

package appeng.container.slot;

import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableList;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.items.IItemHandler;

import appeng.api.crafting.ICraftingHelper;
import appeng.api.definitions.IDefinitions;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IMaterials;
import appeng.api.features.INetworkEncodable;
import appeng.api.implementations.items.IBiometricCard;
import appeng.api.implementations.items.ISpatialStorageCell;
import appeng.api.implementations.items.IStorageComponent;
import appeng.api.implementations.items.IUpgradeModule;
import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.api.storage.cells.ICellWorkbenchItem;
import appeng.core.Api;
import appeng.items.misc.EncodedPatternItem;
import appeng.recipes.handlers.GrinderRecipes;
import appeng.tile.misc.InscriberRecipes;
import appeng.util.Platform;

/**
 * @author AlgorithmX2
 * @author thatsIch
 * @version rv2
 * @since rv0
 */
public class RestrictedInputSlot extends AppEngSlot {

    private static final List<ResourceLocation> METAL_INGOT_TAGS = ImmutableList.of(
            new ResourceLocation("forge:ingots/copper"), new ResourceLocation("forge:ingots/tin"),
            new ResourceLocation("forge:ingots/iron"), new ResourceLocation("forge:ingots/gold"),
            new ResourceLocation("forge:ingots/lead"), new ResourceLocation("forge:ingots/bronze"),
            new ResourceLocation("forge:ingots/brass"), new ResourceLocation("forge:ingots/nickel"),
            new ResourceLocation("forge:ingots/aluminium"));

    private final PlacableItemType which;
    private final PlayerInventory p;
    private boolean allowEdit = true;
    private int stackLimit = -1;

    public RestrictedInputSlot(final PlacableItemType valid, final IItemHandler inv, final int invSlot, final int x,
            final int y, final PlayerInventory p) {
        super(inv, invSlot, x, y);
        this.which = valid;
        this.setIIcon(valid.IIcon);
        this.p = p;
    }

    @Override
    public int getSlotStackLimit() {
        if (this.stackLimit != -1) {
            return this.stackLimit;
        }
        return super.getSlotStackLimit();
    }

    public boolean isValid(final ItemStack is, final World theWorld) {
        if (this.which == PlacableItemType.VALID_ENCODED_PATTERN_W_OUTPUT) {
            return Api.instance().crafting().decodePattern(is, theWorld) != null;
        }
        return true;
    }

    public Slot setStackLimit(final int i) {
        this.stackLimit = i;
        return this;
    }

    @Override
    public boolean isItemValid(final ItemStack stack) {
        if (!this.getContainer().isValidForSlot(this, stack)) {
            return false;
        }

        if (stack.isEmpty()) {
            return false;
        }

        if (stack.getItem() == Items.AIR) {
            return false;
        }

        if (!super.isItemValid(stack)) {
            return false;
        }

        if (!this.isAllowEdit()) {
            return false;
        }

        final IDefinitions definitions = Api.instance().definitions();
        final IMaterials materials = definitions.materials();
        final IItems items = definitions.items();
        final ICraftingHelper crafting = Api.instance().crafting();

        switch (this.which) {
            case ENCODED_CRAFTING_PATTERN:
                final ICraftingPatternDetails de = crafting.decodePattern(stack, this.p.player.world);
                if (de != null) {
                    return de.isCraftable();
                }
                return false;
            case VALID_ENCODED_PATTERN_W_OUTPUT:
            case ENCODED_PATTERN_W_OUTPUT:
            case ENCODED_PATTERN:
                return crafting.isEncodedPattern(stack);
            case BLANK_PATTERN:
                return materials.blankPattern().isSameAs(stack);

            case PATTERN:
                return materials.blankPattern().isSameAs(stack) || crafting.isEncodedPattern(stack);

            case INSCRIBER_PLATE:
                if (materials.namePress().isSameAs(stack)) {
                    return true;
                }

                return InscriberRecipes.isValidOptionalIngredient(p.player.world, stack);

            case INSCRIBER_INPUT:
                return true;/*
                             * for (ItemStack is : Inscribe.inputs) if ( Platform.isSameItemPrecise( is, i ) ) return
                             * true; return false;
                             */

            case METAL_INGOTS:

                return isMetalIngot(stack);

            case VIEW_CELL:
                return items.viewCell().isSameAs(stack);
            case ORE:
                return GrinderRecipes.isValidIngredient(p.player.world, stack);
            case FUEL:
                return ForgeHooks.getBurnTime(stack) > 0;
            case POWERED_TOOL:
                return Platform.isChargeable(stack);
            case QE_SINGULARITY:
                return materials.qESingularity().isSameAs(stack);

            case RANGE_BOOSTER:
                return materials.wirelessBooster().isSameAs(stack);

            case SPATIAL_STORAGE_CELLS:
                return stack.getItem() instanceof ISpatialStorageCell
                        && ((ISpatialStorageCell) stack.getItem()).isSpatialStorage(stack);
            case STORAGE_CELLS:
                return Api.instance().registries().cell().isCellHandled(stack);
            case WORKBENCH_CELL:
                return stack.getItem() instanceof ICellWorkbenchItem
                        && ((ICellWorkbenchItem) stack.getItem()).isEditable(stack);
            case STORAGE_COMPONENT:
                return stack.getItem() instanceof IStorageComponent
                        && ((IStorageComponent) stack.getItem()).isStorageComponent(stack);
            case TRASH:
                if (Api.instance().registries().cell().isCellHandled(stack)) {
                    return false;
                }

                return !(stack.getItem() instanceof IStorageComponent
                        && ((IStorageComponent) stack.getItem()).isStorageComponent(stack));
            case ENCODABLE_ITEM:
                return stack.getItem() instanceof INetworkEncodable
                        || Api.instance().registries().wireless().isWirelessTerminal(stack);
            case BIOMETRIC_CARD:
                return stack.getItem() instanceof IBiometricCard;
            case UPGRADES:
                return stack.getItem() instanceof IUpgradeModule
                        && ((IUpgradeModule) stack.getItem()).getType(stack) != null;
            default:
                break;
        }

        return false;
    }

    @Override
    public boolean canTakeStack(final PlayerEntity player) {
        return this.isAllowEdit();
    }

    @Override
    public ItemStack getDisplayStack() {
        if (isRemote() && (this.which == PlacableItemType.ENCODED_PATTERN)) {
            final ItemStack is = super.getStack();
            if (!is.isEmpty() && is.getItem() instanceof EncodedPatternItem) {
                final EncodedPatternItem iep = (EncodedPatternItem) is.getItem();
                final ItemStack out = iep.getOutput(is);
                if (!out.isEmpty()) {
                    return out;
                }
            }
        }
        return super.getStack();
    }

    public static boolean isMetalIngot(final ItemStack i) {
        if (Platform.itemComparisons().isSameItem(i, new ItemStack(Items.IRON_INGOT))) {
            return true;
        }

        Set<ResourceLocation> itemTags = i.getItem().getTags();
        for (ResourceLocation tagName : METAL_INGOT_TAGS) {
            if (itemTags.contains(tagName)) {
                return true;
            }
        }

        return false;
    }

    private boolean isAllowEdit() {
        return this.allowEdit;
    }

    public void setAllowEdit(final boolean allowEdit) {
        this.allowEdit = allowEdit;
    }

    public enum PlacableItemType {
        STORAGE_CELLS(15), ORE(16 + 15), STORAGE_COMPONENT(3 * 16 + 15),

        ENCODABLE_ITEM(4 * 16 + 15), TRASH(5 * 16 + 15), VALID_ENCODED_PATTERN_W_OUTPUT(7 * 16 + 15),
        ENCODED_PATTERN_W_OUTPUT(7 * 16 + 15),

        ENCODED_CRAFTING_PATTERN(7 * 16 + 15), ENCODED_PATTERN(7 * 16 + 15), PATTERN(8 * 16 + 15),
        BLANK_PATTERN(8 * 16 + 15), POWERED_TOOL(9 * 16 + 15),

        RANGE_BOOSTER(6 * 16 + 15), QE_SINGULARITY(10 * 16 + 15), SPATIAL_STORAGE_CELLS(11 * 16 + 15),

        FUEL(12 * 16 + 15), UPGRADES(13 * 16 + 15), WORKBENCH_CELL(15), BIOMETRIC_CARD(14 * 16 + 15),
        VIEW_CELL(4 * 16 + 14),

        INSCRIBER_PLATE(2 * 16 + 14), INSCRIBER_INPUT(3 * 16 + 14), METAL_INGOTS(3 * 16 + 14);

        public final int IIcon;

        PlacableItemType(final int o) {
            this.IIcon = o;
        }
    }
}
