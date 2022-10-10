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

import java.util.function.BiConsumer;
import java.util.function.Supplier;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.geometry.IGeometryLoader;
import net.minecraftforge.client.model.geometry.IUnbakedGeometry;

import appeng.api.util.AEColor;
import appeng.block.crafting.CraftingUnitType;
import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.SimpleModelLoader;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.crafting.CraftingUnitModelProvider;
import appeng.client.render.model.BiometricCardModel;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.GlassModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.MeteoriteCompassModel;
import appeng.client.render.spatial.SpatialPylonModel;
import appeng.core.AppEng;
import appeng.parts.automation.PlaneModel;

@OnlyIn(Dist.CLIENT)
public final class InitBuiltInModels {
    private InitBuiltInModels() {
    }

    private static BiConsumer<String, IGeometryLoader<?>> register = null;

    public static void init(BiConsumer<String, IGeometryLoader<?>> register) {
        InitBuiltInModels.register = register;

        addBuiltInModel("block/cable_bus", CableBusModel::new);
        addBuiltInModel("block/quartz_glass", GlassModel::new);
        addBuiltInModel("item/meteorite_compass", MeteoriteCompassModel::new);
        for (AEColor color : AEColor.values()) {
            String builtInItemModelName = "memory_card"
                    + (color != AEColor.TRANSPARENT ? ("_" + color.registryPrefix) : "");
            addBuiltInModel("item/" + builtInItemModelName, () -> new MemoryCardModel(color));
        }
        addBuiltInModel("item/biometric_card", BiometricCardModel::new);
        addBuiltInModel("block/drive", DriveModel::new);
        addBuiltInModel("color_applicator", ColorApplicatorModel::new);
        addBuiltInModel("block/spatial_pylon", SpatialPylonModel::new);
        addBuiltInModel("block/paint", PaintSplotchesModel::new);
        addBuiltInModel("block/qnb/qnb_formed", QnbFormedModel::new);
        addBuiltInModel("part/p2p/p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
        addBuiltInModel("item/facade", FacadeItemModel::new);

        // Fabric doesn't have model-loaders, so we register the models by hand instead
        addPlaneModel("part/annihilation_plane", "part/annihilation_plane");
        addPlaneModel("part/annihilation_plane_on", "part/annihilation_plane_on");
        addPlaneModel("part/identity_annihilation_plane", "part/identity_annihilation_plane");
        addPlaneModel("part/identity_annihilation_plane_on", "part/identity_annihilation_plane_on");
        addPlaneModel("part/formation_plane", "part/formation_plane");
        addPlaneModel("part/formation_plane_on", "part/formation_plane_on");

        addBuiltInModel("block/crafting/1k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_1K)));
        addBuiltInModel("block/crafting/4k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_4K)));
        addBuiltInModel("block/crafting/16k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_16K)));
        addBuiltInModel("block/crafting/64k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_64K)));
        addBuiltInModel("block/crafting/256k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_256K)));
        addBuiltInModel("block/crafting/accelerator_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.ACCELERATOR)));
        addBuiltInModel("block/crafting/monitor_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.MONITOR)));
        addBuiltInModel("block/crafting/unit_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.UNIT)));

        InitBuiltInModels.register = null;
    }

    private static void addPlaneModel(String planeName,
            String frontTexture) {
        ResourceLocation frontTextureId = AppEng.makeId(frontTexture);
        ResourceLocation sidesTextureId = AppEng.makeId("part/plane_sides");
        ResourceLocation backTextureId = AppEng.makeId("part/transition_plane_back");
        addBuiltInModel(planeName, () -> new PlaneModel(frontTextureId, sidesTextureId, backTextureId));
    }

    private static <T extends IUnbakedGeometry<T>> void addBuiltInModel(String id,
            Supplier<T> modelFactory) {
        register.accept(id, new SimpleModelLoader<>(modelFactory));
    }
}
