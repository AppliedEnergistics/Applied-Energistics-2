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
import net.minecraft.world.gen.feature.ConfiguredFeature;
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

import appeng.api.config.Upgrades;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.movable.IMovableRegistry;
import appeng.client.render.tesr.InscriberTESR;
import appeng.client.render.tesr.SkyChestTESR;
import appeng.core.api.definitions.ApiBlocks;
import appeng.core.api.definitions.ApiItems;
import appeng.core.api.definitions.ApiParts;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.localization.GuiText;
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
import appeng.init.internal.InitGridCaches;
import appeng.init.internal.InitMatterCannonAmmo;
import appeng.init.worldgen.InitBiomes;
import appeng.init.worldgen.InitFeatures;
import appeng.init.worldgen.InitStructures;
import appeng.server.AECommand;
import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;
import appeng.tile.AEBaseTileEntity;

final class Registration {

    static AdvancementTriggers advancementTriggers;

    private static ConfiguredFeature<?, ?> quartzOreFeature;
    private static ConfiguredFeature<?, ?> chargedQuartzOreFeature;

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

        final IRegistryContainer registries = Api.instance().registries();

        // Block and part interface have different translation keys, but support the
        // same upgrades
        String interfaceGroup = ApiParts.INTERFACE.asItem().getTranslationKey();
        String itemIoBusGroup = GuiText.IOBuses.getTranslationKey();
        String fluidIoBusGroup = GuiText.IOBusesFluids.getTranslationKey();
        String storageCellGroup = GuiText.StorageCells.getTranslationKey();

        // default settings..
        ((P2PTunnelRegistry) registries.p2pTunnel()).configure();

        // Interface
        Upgrades.CRAFTING.registerItem(ApiParts.INTERFACE, 1, interfaceGroup);
        Upgrades.CRAFTING.registerItem(ApiBlocks.INTERFACE, 1, interfaceGroup);

        // IO Port!
        Upgrades.SPEED.registerItem(ApiBlocks.IO_PORT, 3);
        Upgrades.REDSTONE.registerItem(ApiBlocks.IO_PORT, 1);

        // Level Emitter!
        Upgrades.FUZZY.registerItem(ApiParts.LEVEL_EMITTER, 1);
        Upgrades.CRAFTING.registerItem(ApiParts.LEVEL_EMITTER, 1);

        // Import Bus
        Upgrades.FUZZY.registerItem(ApiParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.IMPORT_BUS, 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(ApiParts.IMPORT_BUS, 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.IMPORT_BUS, 4, itemIoBusGroup);

        // Fluid Import Bus
        Upgrades.CAPACITY.registerItem(ApiParts.FLUID_IMPORT_BUS, 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.FLUID_IMPORT_BUS, 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.FLUID_IMPORT_BUS, 4, fluidIoBusGroup);

        // Export Bus
        Upgrades.FUZZY.registerItem(ApiParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.EXPORT_BUS, 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(ApiParts.EXPORT_BUS, 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.EXPORT_BUS, 4, itemIoBusGroup);
        Upgrades.CRAFTING.registerItem(ApiParts.EXPORT_BUS, 1, itemIoBusGroup);

        // Fluid Export Bus
        Upgrades.CAPACITY.registerItem(ApiParts.FLUID_EXPORT_BUS, 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(ApiParts.FLUID_EXPORT_BUS, 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(ApiParts.FLUID_EXPORT_BUS, 4, fluidIoBusGroup);

        // Storage Cells
        Upgrades.FUZZY.registerItem(ApiItems.CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL1K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.CELL4K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL4K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL16K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.CELL64K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.CELL64K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL1K, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL4k, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL16K, 1, storageCellGroup);
        Upgrades.FUZZY.registerItem(ApiItems.PORTABLE_CELL64K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL4k, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.PORTABLE_CELL64K, 1, storageCellGroup);

        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL1K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL4K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL16K, 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(ApiItems.FLUID_CELL64K, 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(ApiItems.VIEW_CELL, 1);
        Upgrades.INVERTER.registerItem(ApiItems.VIEW_CELL, 1);

        // Storage Bus
        Upgrades.FUZZY.registerItem(ApiParts.STORAGE_BUS, 1);
        Upgrades.INVERTER.registerItem(ApiParts.STORAGE_BUS, 1);
        Upgrades.CAPACITY.registerItem(ApiParts.STORAGE_BUS, 5);

        // Storage Bus Fluids
        Upgrades.INVERTER.registerItem(ApiParts.FLUID_STORAGE_BUS, 1);
        Upgrades.CAPACITY.registerItem(ApiParts.FLUID_STORAGE_BUS, 5);

        // Formation Plane
        Upgrades.FUZZY.registerItem(ApiParts.FORMATION_PLANE, 1);
        Upgrades.INVERTER.registerItem(ApiParts.FORMATION_PLANE, 1);
        Upgrades.CAPACITY.registerItem(ApiParts.FORMATION_PLANE, 5);

        // Matter Cannon
        Upgrades.FUZZY.registerItem(ApiItems.MASS_CANNON, 1);
        Upgrades.INVERTER.registerItem(ApiItems.MASS_CANNON, 1);
        Upgrades.SPEED.registerItem(ApiItems.MASS_CANNON, 4);

        // Molecular Assembler
        Upgrades.SPEED.registerItem(ApiBlocks.MOLECULAR_ASSEMBLER, 5);

        // Inscriber
        Upgrades.SPEED.registerItem(ApiBlocks.INSCRIBER, 3);

        // Wireless Terminal Handler
        registries.wireless().registerWirelessHandler((IWirelessTermHandler) ApiItems.WIRELESS_TERMINAL.item());

        // Charge Rates
        registries.charger().addChargeRate(ApiItems.CHARGED_STAFF, 320d);
        registries.charger().addChargeRate(ApiItems.PORTABLE_CELL1K, 800d);
        registries.charger().addChargeRate(ApiItems.PORTABLE_CELL4k, 800d);
        registries.charger().addChargeRate(ApiItems.PORTABLE_CELL16K, 800d);
        registries.charger().addChargeRate(ApiItems.PORTABLE_CELL64K, 800d);
        registries.charger().addChargeRate(ApiItems.COLOR_APPLICATOR, 800d);
        registries.charger().addChargeRate(ApiItems.WIRELESS_TERMINAL, 8000d);
        registries.charger().addChargeRate(ApiItems.ENTROPY_MANIPULATOR, 8000d);
        registries.charger().addChargeRate(ApiItems.MASS_CANNON, 8000d);
        registries.charger().addChargeRate(ApiBlocks.ENERGY_CELL, 8000d);
        registries.charger().addChargeRate(ApiBlocks.DENSE_ENERGY_CELL, 16000d);

        // FIXME // add villager trading to black smiths for a few basic materials
        // FIXME if( AEConfig.instance().isFeatureEnabled( AEFeature.VILLAGER_TRADING ) )
        // FIXME {
        // FIXME // TODO: VILLAGER TRADING
        // FIXME // VillagerRegistry.instance().getRegisteredVillagers().registerVillageTradeHandler( 3, new AETrading()
        // );
        // FIXME }

        final IMovableRegistry mr = registries.movable();

        /*
         * You can't move bed rock.
         */
        mr.blacklistBlock(net.minecraft.block.Blocks.BEDROCK);

        /*
         * White List Vanilla...
         */
        mr.whiteListTileEntity(net.minecraft.tileentity.BannerTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.BeaconTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.BrewingStandTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.ChestTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.CommandBlockTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.ComparatorTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.DaylightDetectorTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.DispenserTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.DropperTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.EnchantingTableTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.EnderChestTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.EndPortalTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.FurnaceTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.HopperTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.MobSpawnerTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.PistonTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.ShulkerBoxTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.SignTileEntity.class);
        mr.whiteListTileEntity(net.minecraft.tileentity.SkullTileEntity.class);

        /*
         * Whitelist AE2
         */
        mr.whiteListTileEntity(AEBaseTileEntity.class);
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
