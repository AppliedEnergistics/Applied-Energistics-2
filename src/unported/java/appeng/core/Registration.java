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

import appeng.bootstrap.ModelsReloadCallback;
import appeng.client.render.model.*;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.GenerationStep;
import net.minecraft.world.gen.decorator.Decorator;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.FeatureConfig;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.gen.placement.CountRangeConfig;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

import appeng.api.AEApi;
import appeng.api.config.Upgrades;
import appeng.api.definitions.IBlocks;
import appeng.api.definitions.IItems;
import appeng.api.definitions.IParts;
import appeng.api.features.AEFeature;
import appeng.api.features.IRegistryContainer;
import appeng.api.features.IWirelessTermHandler;
import appeng.api.features.IWorldGen;
import appeng.api.movable.IMovableRegistry;
import appeng.core.features.registries.P2PTunnelRegistry;
import appeng.core.localization.GuiText;
import appeng.core.stats.AdvancementTriggers;
import appeng.server.AECommand;
import appeng.spatial.StorageCellBiome;
import appeng.spatial.StorageCellModDimension;
import appeng.tile.AEBaseBlockEntity;
import appeng.tile.crafting.MolecularAssemblerRenderer;
import appeng.worldgen.ChargedQuartzOreConfig;
import appeng.worldgen.ChargedQuartzOreFeature;
import appeng.worldgen.meteorite.MeteoriteStructure;

final class Registration {

    public Registration() {
    }

    AdvancementTriggers advancementTriggers;


    @Environment(EnvType.CLIENT)
    public void modelRegistryEvent(ModelRegistryEvent event) {
        registerSpecialModels();
    }

    /**
     * Registers any JSON model files with Minecraft that are not referenced via
     * blockstates or item IDs
     */
    @Environment(EnvType.CLIENT)
    private void registerSpecialModels() {
        SkyCompassModel.DEPENDENCIES.forEach(ModelLoader::addSpecialModel);
        ModelLoader.addSpecialModel(BiometricCardModel.MODEL_BASE);
        ModelLoader.addSpecialModel(MemoryCardModel.MODEL_BASE);
        DriveModel.DEPENDENCIES.forEach(ModelLoader::addSpecialModel);
        ModelLoader.addSpecialModel(MolecularAssemblerRenderer.LIGHTS_MODEL);

    }

    // FIXME LATER
    public static void postInit() {
        final IRegistryContainer registries = AEApi.instance().registries();
        // TODO: Do not use the internal API
        ApiDefinitions definitions = Api.INSTANCE.definitions();
        final IParts parts = definitions.parts();
        final IBlocks blocks = definitions.blocks();
        final IItems items = definitions.items();

        // Block and part interface have different translation keys, but support the
        // same upgrades
        Text interfaceGroup = parts.iface().asItem().getName();
        Text itemIoBusGroup = GuiText.IOBuses.text();
        Text fluidIoBusGroup = GuiText.IOBusesFluids.text();
        Text storageCellGroup = GuiText.IOBusesFluids.text();

        // default settings..
        ((P2PTunnelRegistry) registries.p2pTunnel()).configure();

        // Interface
        Upgrades.CRAFTING.registerItem(parts.iface(), 1, interfaceGroup);
        Upgrades.CRAFTING.registerItem(blocks.iface(), 1, interfaceGroup);

        // IO Port!
        Upgrades.SPEED.registerItem(blocks.iOPort(), 3);
        Upgrades.REDSTONE.registerItem(blocks.iOPort(), 1);

        // Level Emitter!
        Upgrades.FUZZY.registerItem(parts.levelEmitter(), 1);
        Upgrades.CRAFTING.registerItem(parts.levelEmitter(), 1);

        // Import Bus
        Upgrades.FUZZY.registerItem(parts.importBus(), 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.importBus(), 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(parts.importBus(), 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(parts.importBus(), 4, itemIoBusGroup);

        // Fluid Import Bus
        Upgrades.CAPACITY.registerItem(parts.fluidImportBus(), 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.fluidImportBus(), 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(parts.fluidImportBus(), 4, fluidIoBusGroup);

        // Export Bus
        Upgrades.FUZZY.registerItem(parts.exportBus(), 1, itemIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.exportBus(), 1, itemIoBusGroup);
        Upgrades.CAPACITY.registerItem(parts.exportBus(), 2, itemIoBusGroup);
        Upgrades.SPEED.registerItem(parts.exportBus(), 4, itemIoBusGroup);
        Upgrades.CRAFTING.registerItem(parts.exportBus(), 1, itemIoBusGroup);

        // Fluid Export Bus
        Upgrades.CAPACITY.registerItem(parts.fluidExportBus(), 2, fluidIoBusGroup);
        Upgrades.REDSTONE.registerItem(parts.fluidExportBus(), 1, fluidIoBusGroup);
        Upgrades.SPEED.registerItem(parts.fluidExportBus(), 4, fluidIoBusGroup);

        // Storage Cells
        Upgrades.FUZZY.registerItem(items.cell1k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell1k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.cell4k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell4k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.cell16k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell16k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.cell64k(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.cell64k(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.portableCell(), 1, storageCellGroup);
        Upgrades.INVERTER.registerItem(items.portableCell(), 1, storageCellGroup);

        Upgrades.FUZZY.registerItem(items.viewCell(), 1);
        Upgrades.INVERTER.registerItem(items.viewCell(), 1);

        // Storage Bus
        Upgrades.FUZZY.registerItem(parts.storageBus(), 1);
        Upgrades.INVERTER.registerItem(parts.storageBus(), 1);
        Upgrades.CAPACITY.registerItem(parts.storageBus(), 5);

        // Storage Bus Fluids
        Upgrades.INVERTER.registerItem(parts.fluidStorageBus(), 1);
        Upgrades.CAPACITY.registerItem(parts.fluidStorageBus(), 5);

        // Formation Plane
        Upgrades.FUZZY.registerItem(parts.formationPlane(), 1);
        Upgrades.INVERTER.registerItem(parts.formationPlane(), 1);
        Upgrades.CAPACITY.registerItem(parts.formationPlane(), 5);

        // Matter Cannon
        Upgrades.FUZZY.registerItem(items.massCannon(), 1);
        Upgrades.INVERTER.registerItem(items.massCannon(), 1);
        Upgrades.SPEED.registerItem(items.massCannon(), 4);

        // Molecular Assembler
        Upgrades.SPEED.registerItem(blocks.molecularAssembler(), 5);

        // Inscriber
        Upgrades.SPEED.registerItem(blocks.inscriber(), 3);

        // Wireless Terminal Handler
        items.wirelessTerminal().maybeItem()
                .ifPresent(terminal -> registries.wireless().registerWirelessHandler((IWirelessTermHandler) terminal));

        // Charge Rates
        items.chargedStaff().maybeItem()
                .ifPresent(chargedStaff -> registries.charger().addChargeRate(chargedStaff, 320d));
        items.portableCell().maybeItem()
                .ifPresent(chargedStaff -> registries.charger().addChargeRate(chargedStaff, 800d));
        items.colorApplicator().maybeItem()
                .ifPresent(colorApplicator -> registries.charger().addChargeRate(colorApplicator, 800d));
        items.wirelessTerminal().maybeItem().ifPresent(terminal -> registries.charger().addChargeRate(terminal, 8000d));
        items.entropyManipulator().maybeItem()
                .ifPresent(entropyManipulator -> registries.charger().addChargeRate(entropyManipulator, 8000d));
        items.massCannon().maybeItem().ifPresent(massCannon -> registries.charger().addChargeRate(massCannon, 8000d));
        blocks.energyCell().maybeItem().ifPresent(cell -> registries.charger().addChargeRate(cell, 8000d));
        blocks.energyCellDense().maybeItem().ifPresent(cell -> registries.charger().addChargeRate(cell, 16000d));

// FIXME		// add villager trading to black smiths for a few basic materials
// FIXME		if( AEConfig.instance().isFeatureEnabled( AEFeature.VILLAGER_TRADING ) )
// FIXME		{
// FIXME			// TODO: VILLAGER TRADING
// FIXME			// VillagerRegistry.instance().getRegisteredVillagers().registerVillageTradeHandler( 3, new AETrading() );
// FIXME		}

        final IMovableRegistry mr = registries.movable();

        /*
         * You can't move bed rock.
         */
        mr.blacklistBlock(net.minecraft.block.Blocks.BEDROCK);

        /*
         * White List Vanilla...
         */
        mr.whiteListBlockEntity(BannerBlockEntity.class);
        mr.whiteListBlockEntity(BeaconBlockEntity.class);
        mr.whiteListBlockEntity(BrewingStandBlockEntity.class);
        mr.whiteListBlockEntity(ChestBlockEntity.class);
        mr.whiteListBlockEntity(CommandBlockBlockEntity.class);
        mr.whiteListBlockEntity(ComparatorBlockEntity.class);
        mr.whiteListBlockEntity(DaylightDetectorBlockEntity.class);
        mr.whiteListBlockEntity(DispenserBlockEntity.class);
        mr.whiteListBlockEntity(DropperBlockEntity.class);
        mr.whiteListBlockEntity(EnchantingTableBlockEntity.class);
        mr.whiteListBlockEntity(EnderChestBlockEntity.class);
        mr.whiteListBlockEntity(EndPortalBlockEntity.class);
        mr.whiteListBlockEntity(FurnaceBlockEntity.class);
        mr.whiteListBlockEntity(HopperBlockEntity.class);
        mr.whiteListBlockEntity(MobSpawnerBlockEntity.class);
        mr.whiteListBlockEntity(PistonBlockEntity.class);
        mr.whiteListBlockEntity(ShulkerBoxBlockEntity.class);
        mr.whiteListBlockEntity(SignBlockEntity.class);
        mr.whiteListBlockEntity(SkullBlockEntity.class);

        /*
         * Whitelist AE2
         */
        mr.whiteListBlockEntity(AEBaseBlockEntity.class);

        /*
         * world gen
         */
        for (final IWorldGen.WorldGenType type : IWorldGen.WorldGenType.values()) {
            // FIXME: registries.worldgen().disableWorldGenForProviderID( type,
            // StorageWorldProvider.class );

            registries.worldgen().disableWorldGenForDimension(type, DimensionType.THE_NETHER_REGISTRY_KEY.getValue());
        }

        // whitelist from config
        for (final String dimension : AEConfig.instance().getMeteoriteDimensionWhitelist()) {
            registries.worldgen().enableWorldGenForDimension(IWorldGen.WorldGenType.METEORITES,
                    new Identifier(dimension));
        }

        Biome.BIOMES.forEach(b -> {
            addMeteoriteWorldGen(b);
            addQuartzWorldGen(b);
        });
    }

    private static void addMeteoriteWorldGen(Biome b) {
        if (!AEConfig.instance().isFeatureEnabled(AEFeature.METEORITE_WORLD_GEN)) {
            return;
        }

        if (b.getCategory() == Biome.Category.THEEND || b.getCategory() == Biome.Category.NETHER) {
            return;
        }

        b.addStructureFeature(MeteoriteStructure.INSTANCE.configure(FeatureConfig.DEFAULT));
        b.addFeature(GenerationStep.Feature.TOP_LAYER_MODIFICATION,
                MeteoriteStructure.INSTANCE.configure(FeatureConfig.DEFAULT));
    }

    public void registerWorldGen(RegistryEvent.Register<Feature<?>> evt) {
        IForgeRegistry<Feature<?>> r = evt.getRegistry();

//        r.register(MeteoriteWorldGen.INSTANCE);
        r.register(MeteoriteStructure.INSTANCE);
        r.register(ChargedQuartzOreFeature.INSTANCE);
    }

    public void registerBiomes(RegistryEvent.Register<Biome> evt) {
        evt.getRegistry().register(StorageCellBiome.INSTANCE);
    }

    public void registerModDimension(RegistryEvent.Register<ModDimension> evt) {
        evt.getRegistry().register(StorageCellModDimension.INSTANCE);
    }

    public void registerCommands(final FMLServerStartingEvent evt) {
        new AECommand().register(evt.getCommandDispatcher());
    }

    @Environment(EnvType.CLIENT)
    public void registerClientEvents() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::registerParticleFactories);
        modEventBus.addListener(this::registerTextures);
        modEventBus.addListener(this::modelRegistryEvent);
        modEventBus.addListener(this::registerItemColors);

        ModelsReloadCallback.EVENT.register(this::onModelsReloaded);

    }
}
