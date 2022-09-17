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

package appeng.block.networking;


import appeng.api.implementations.items.IAEItemPowerStorage;
import appeng.block.AEBaseItemBlockChargeable;
import appeng.bootstrap.BlockRenderingCustomizer;
import appeng.bootstrap.IBlockRendering;
import appeng.bootstrap.IItemRendering;
import appeng.tile.networking.TileEnergyCell;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;


public class BlockEnergyCellRendering extends BlockRenderingCustomizer {

    private final ResourceLocation baseModel;

    public BlockEnergyCellRendering(ResourceLocation baseModel) {
        this.baseModel = baseModel;
    }

    @Override
    public void customize(IBlockRendering rendering, IItemRendering itemRendering) {
        itemRendering.meshDefinition(this::getItemModel);
        // Note: Since we use the block models, we dont need to register custom variants
    }

    /**
     * Determines which version of the energy cell model should be used depending on the fill factor
     * of the item stack.
     */
    private ModelResourceLocation getItemModel(ItemStack is) {
        double fillFactor = getFillFactor(is);

        int storageLevel = TileEnergyCell.getStorageLevelFromFillFactor(fillFactor);
        return new ModelResourceLocation(this.baseModel, "fullness=" + storageLevel);
    }

    /**
     * Helper method that returns the energy fill factor (between 0 and 1) of a given item stack.
     * Returns 0 if the item stack has no fill factor.
     */
    private static double getFillFactor(ItemStack is) {
        if (!(is.getItem() instanceof IAEItemPowerStorage)) {
            return 0;
        }

        AEBaseItemBlockChargeable itemChargeable = (AEBaseItemBlockChargeable) is.getItem();
        double curPower = itemChargeable.getAECurrentPower(is);
        double maxPower = itemChargeable.getAEMaxPower(is);

        return curPower / maxPower;
    }
}
