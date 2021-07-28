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

package appeng.parts.automation;

import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.IModelData;

import appeng.api.parts.IPartModel;
import appeng.items.parts.PartModels;

public class IdentityAnnihilationPlanePart extends AnnihilationPlanePart {

    private static final PlaneModels MODELS = new PlaneModels("part/item_identity_annihilation_plane",
            "part/item_identity_annihilation_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    private static final float SILK_TOUCH_FACTOR = 16;

    public IdentityAnnihilationPlanePart(final ItemStack is) {
        super(is);
    }

    @Override
    protected float calculateEnergyUsage(final ServerLevel w, final BlockPos pos, final List<ItemStack> items) {
        final float requiredEnergy = super.calculateEnergyUsage(w, pos, items);

        return requiredEnergy * SILK_TOUCH_FACTOR;
    }

    @Override
    protected ItemStack createHarvestTool(BlockState state) {
        ItemStack harvestTool = super.createHarvestTool(state);

        // For silk touch purposes, enchant the fake tool
        if (harvestTool != null) {
            EnchantmentHelper.setEnchantments(ImmutableMap.of(Enchantments.SILK_TOUCH, 1), harvestTool);
        }

        return harvestTool;
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public IModelData getModelData() {
        return new PlaneModelData(getConnections());
    }

}
