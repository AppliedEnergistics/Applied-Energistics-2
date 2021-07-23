/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2021, TeamAppliedEnergistics, All rights reserved.
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

package appeng.init.client;

import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ModelLoaderRegistry;
import net.minecraftforge.client.model.geometry.IModelGeometry;

import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.client.render.DummyFluidItemModel;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.SimpleModelLoader;
import appeng.client.render.cablebus.CableBusModelLoader;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModelLoader;
import appeng.client.render.crafting.EncodedPatternModelLoader;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.GlassModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.SkyCompassModel;
import appeng.client.render.spatial.SpatialPylonModel;
import appeng.core.Api;
import appeng.core.AppEng;
import appeng.core.registries.PartModels;
import appeng.parts.automation.PlaneModelLoader;

@OnlyIn(Dist.CLIENT)
public final class InitBuiltInModels {
    private InitBuiltInModels() {
    }

    public static void init() {
        addBuiltInModel("glass", GlassModel::new);
        addBuiltInModel("sky_compass", SkyCompassModel::new);
        addBuiltInModel("dummy_fluid_item", DummyFluidItemModel::new);
        addBuiltInModel("memory_card", MemoryCardModel::new);
        addBuiltInModel("biometric_card", BiometricCardModel::new);
        addBuiltInModel("drive", DriveModel::new);
        addBuiltInModel("color_applicator", ColorApplicatorModel::new);
        addBuiltInModel("spatial_pylon", SpatialPylonModel::new);
        addBuiltInModel("paint_splotches", PaintSplotchesModel::new);
        addBuiltInModel("quantum_bridge_formed", QnbFormedModel::new);
        addBuiltInModel("p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
        addBuiltInModel("facade", FacadeItemModel::new);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "encoded_pattern"),
                EncodedPatternModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "part_plane"),
                PlaneModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new ResourceLocation(AppEng.MOD_ID, "crafting_cube"),
                CraftingCubeModelLoader.INSTANCE);
        ModelLoaderRegistry.registerLoader(new net.minecraft.resources.ResourceLocation(AppEng.MOD_ID, "cable_bus"),
                new CableBusModelLoader((PartModels) Api.INSTANCE.registries().partModels()));
    }

    private static <T extends IModelGeometry<T>> void addBuiltInModel(String id, Supplier<T> modelFactory) {
        ModelLoaderRegistry.registerLoader(new net.minecraft.resources.ResourceLocation(AppEng.MOD_ID, id),
                new SimpleModelLoader<>(modelFactory));
    }
}
