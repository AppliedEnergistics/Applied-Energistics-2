/*
 * This file is part of Applied Energistics 2.
 * Copyright (c) 2013 - 2015, AlgorithmX2, All rights reserved.
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

package appeng.core;

import com.mojang.brigadier.CommandDispatcher;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.block.Block;
import net.minecraft.command.CommandSource;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.stats.AeStats;
import appeng.init.InitBlockEntities;
import appeng.init.InitBlocks;
import appeng.init.InitContainerTypes;
import appeng.init.InitEntityTypes;
import appeng.init.InitItems;
import appeng.init.InitRecipeSerializers;
import appeng.init.client.InitAdditionalModels;
import appeng.init.client.InitAutoRotatingModel;
import appeng.init.client.InitBlockColors;
import appeng.init.client.InitBlockEntityRenderers;
import appeng.init.client.InitBuiltInModels;
import appeng.init.client.InitItemColors;
import appeng.init.client.InitItemModelsProperties;
import appeng.init.client.InitParticleFactories;
import appeng.init.client.InitParticleTypes;
import appeng.init.client.InitRenderTypes;
import appeng.init.client.InitScreens;
import appeng.init.internal.InitCellHandlers;
import appeng.init.internal.InitChargerRates;
import appeng.init.internal.InitGridCaches;
import appeng.init.internal.InitMatterCannonAmmo;
import appeng.init.internal.InitP2PAttunements;
import appeng.init.internal.InitSpatialMovableRegistry;
import appeng.init.internal.InitUpgrades;
import appeng.init.internal.InitWirelessHandlers;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitFeatures;
import appeng.init.worldgen.InitStructures;
import appeng.server.AECommand;
import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

final class Registration {

    static AdvancementTriggers advancementTriggers;

    public static void setupInternalRegistries() {
        InitGridCaches.init();
        InitMatterCannonAmmo.init();
        InitCellHandlers.init();
    }

    @OnlyIn(Dist.CLIENT)
    public void modelRegistryEvent(ModelRegistryEvent event) {
        InitAdditionalModels.init();
        InitBlockEntityRenderers.init();
        InitItemModelsProperties.init();
        InitRenderTypes.init();
        InitBuiltInModels.init();
    }

    public void registerBiomes(RegistryEvent.Register<Biome> event) {
        InitBiomes.init(event.getRegistry());
    }

    public void registerBlocks(RegistryEvent.Register<Block> event) {
        InitBlocks.init(event.getRegistry());
    }

    public void registerItems(RegistryEvent.Register<Item> event) {
        InitItems.init(event.getRegistry());
    }

    public void registerTileEntities(RegistryEvent.Register<TileEntityType<?>> event) {
        InitBlockEntities.init(event.getRegistry());
    }

    public void registerContainerTypes(RegistryEvent.Register<ContainerType<?>> event) {
        InitContainerTypes.init(event.getRegistry());

        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> {
            InitScreens.init();
        });
    }

    public void registerRecipeSerializers(RegistryEvent.Register<IRecipeSerializer<?>> event) {
        InitRecipeSerializers.init(event.getRegistry());
    }

    public void registerEntities(RegistryEvent.Register<EntityType<?>> event) {
        InitEntityTypes.init(event.getRegistry());
    }

    public void registerParticleTypes(RegistryEvent.Register<ParticleType<?>> event) {
        InitParticleTypes.init(event.getRegistry());
    }

    public void registerStructures(RegistryEvent.Register<Structure<?>> event) {
        InitStructures.init(event.getRegistry());
    }

    public void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        InitFeatures.init(event.getRegistry());
    }

    @OnlyIn(Dist.CLIENT)
    public void registerParticleFactories(ParticleFactoryRegisterEvent event) {
        InitParticleFactories.init();
    }

    // FIXME LATER
    public static void postInit() {
        AeStats.register();
        advancementTriggers = new AdvancementTriggers(CriteriaTriggers::register);

        InitP2PAttunements.init();
        InitWirelessHandlers.init();
        InitUpgrades.init();
        InitChargerRates.init();
        InitSpatialMovableRegistry.init();
    }

    @OnlyIn(Dist.CLIENT)
    public void registerTextures(TextureStitchEvent.Pre event) {
        SkyChestTESR.registerTextures(event);
        InscriberTESR.registerTexture(event);
    }

    public void registerCommands(final FMLServerStartingEvent evt) {
        CommandDispatcher<CommandSource> dispatcher = evt.getServer().getCommandManager().getDispatcher();
        new AECommand().register(dispatcher);
    }

    @OnlyIn(Dist.CLIENT)
    public void registerBlockColors(ColorHandlerEvent.Block event) {
        InitBlockColors.init(event.getBlockColors());
    }

    @OnlyIn(Dist.CLIENT)
    public void registerItemColors(ColorHandlerEvent.Item event) {
        InitItemColors.init(event.getItemColors());
    }

    @OnlyIn(Dist.CLIENT)
    public void registerClientEvents() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::registerTextures);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerBlockColors);
        modEventBus.addListener(this::registerItemColors);

        InitAutoRotatingModel.init(modEventBus);
    }

    public void registerDimension(RegistryEvent.NewRegistry e) {
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, SpatialStorageDimensionIds.CHUNK_GENERATOR_ID,
                SpatialStorageChunkGenerator.CODEC);
    }

}
