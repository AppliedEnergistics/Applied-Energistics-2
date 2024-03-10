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

import appeng.block.crafting.CraftingUnitType;
import appeng.block.paint.PaintSplotchesModel;
import appeng.block.qnb.QnbFormedModel;
import appeng.client.render.FacadeItemModel;
import appeng.client.render.cablebus.CableBusModel;
import appeng.client.render.cablebus.P2PTunnelFrequencyModel;
import appeng.client.render.crafting.CraftingCubeModel;
import appeng.client.render.crafting.CraftingUnitModelProvider;
import appeng.client.render.model.BuiltInModelLoader;
import appeng.client.render.model.ColorApplicatorModel;
import appeng.client.render.model.DriveModel;
import appeng.client.render.model.GlassModel;
import appeng.client.render.model.MemoryCardModel;
import appeng.client.render.model.MeteoriteCompassModel;
import appeng.client.render.tesr.spatial.SpatialPylonModel;
import appeng.core.AppEng;
import appeng.parts.automation.PlaneModel;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;

import java.util.function.Supplier;

@OnlyIn(Dist.CLIENT)
public final class InitBuiltInModels {
    private InitBuiltInModels() {
    }

    public static void init(ModelEvent.RegisterGeometryLoaders event) {
        addBuiltInModel(event, "block/cable_bus", CableBusModel::new);
        addBuiltInModel(event, "block/quartz_glass", GlassModel::new);
        addBuiltInModel(event, "item/meteorite_compass", MeteoriteCompassModel::new);
        addBuiltInModel(event, "item/memory_card", MemoryCardModel::new);
        addBuiltInModel(event, "block/drive", DriveModel::new);
//        addBuiltInModel(event, "color_applicator", ColorApplicatorModel::new);
        addBuiltInModel(event, "block/spatial_pylon", SpatialPylonModel::new);
        addBuiltInModel(event, "block/paint", PaintSplotchesModel::new);
        addBuiltInModel(event, "block/qnb/qnb_formed", QnbFormedModel::new);
        addBuiltInModel(event, "part/p2p/p2p_tunnel_frequency", P2PTunnelFrequencyModel::new);
        addBuiltInModel(event, "item/facade", FacadeItemModel::new);

        // Fabric doesn't have model-loaders, so we register the models by hand instead
        addPlaneModel(event, "part/annihilation_plane", "part/annihilation_plane");
        addPlaneModel(event, "part/annihilation_plane_on", "part/annihilation_plane_on");
        addPlaneModel(event, "part/identity_annihilation_plane", "part/identity_annihilation_plane");
        addPlaneModel(event, "part/identity_annihilation_plane_on", "part/identity_annihilation_plane_on");
        addPlaneModel(event, "part/formation_plane", "part/formation_plane");
        addPlaneModel(event, "part/formation_plane_on", "part/formation_plane_on");

        addBuiltInModel(event, "block/crafting/1k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_1K)));
        addBuiltInModel(event, "block/crafting/4k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_4K)));
        addBuiltInModel(event, "block/crafting/16k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_16K)));
        addBuiltInModel(event, "block/crafting/64k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_64K)));
        addBuiltInModel(event, "block/crafting/256k_storage_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.STORAGE_256K)));
        addBuiltInModel(event, "block/crafting/accelerator_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.ACCELERATOR)));
        addBuiltInModel(event, "block/crafting/monitor_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.MONITOR)));
        addBuiltInModel(event, "block/crafting/unit_formed",
                () -> new CraftingCubeModel(new CraftingUnitModelProvider(CraftingUnitType.UNIT)));
    }

    private static void addPlaneModel(ModelEvent.RegisterGeometryLoaders event,
                                      String planeName,
                                      String frontTexture) {
        ResourceLocation frontTextureId = AppEng.makeId(frontTexture);
        ResourceLocation sidesTextureId = AppEng.makeId("part/plane_sides");
        ResourceLocation backTextureId = AppEng.makeId("part/transition_plane_back");
        addBuiltInModel(event, planeName, () -> new PlaneModel(frontTextureId, sidesTextureId, backTextureId));
    }

    private static <T extends IUnbakedGeometry<T>> void addBuiltInModel(ModelEvent.RegisterGeometryLoaders event,
                                                                        String id,
                                                                        Supplier<T> modelFactory) {

        event.register(AppEng.makeId(id), new BuiltInModelLoader<>(modelFactory));

//        BuiltInModelHooks.addBuiltInModel(AppEng.makeId(id), modelFactory.get());
    }
}
