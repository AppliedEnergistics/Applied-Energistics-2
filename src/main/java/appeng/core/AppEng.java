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

import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import com.google.common.base.Stopwatch;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.particles.ParticleType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import appeng.bootstrap.components.IInitComponent;
import appeng.bootstrap.components.IPostInitComponent;
import appeng.capabilities.Capabilities;
import appeng.client.ClientHelper;
import appeng.core.stats.AdvancementTriggers;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.worlddata.WorldData;
import appeng.entity.ChargedQuartzEntity;
import appeng.entity.GrowingCrystalEntity;
import appeng.entity.SingularityEntity;
import appeng.entity.TinyTNTPrimedEntity;
import appeng.entity.TinyTNTPrimedRenderer;
import appeng.hooks.TickHandler;
import appeng.integration.Integrations;
import appeng.parts.PartPlacement;
import appeng.server.ServerHelper;
import appeng.spatial.SpatialStorageChunkGenerator;
import appeng.spatial.SpatialStorageDimensionIds;

@Mod(AppEng.MOD_ID)
public final class AppEng {
    public static CommonHelper proxy;

    public static final String MOD_ID = "appliedenergistics2";
    public static final String MOD_NAME = "Applied Energistics 2";

    private static AppEng INSTANCE;

    public static ResourceLocation makeId(String id) {
        return new ResourceLocation(MOD_ID, id);
    }

    private final Registration registration;

    public AppEng() {
        if (INSTANCE != null) {
            throw new IllegalStateException();
        }
        INSTANCE = this;

        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, AEConfig.CLIENT_SPEC);
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, AEConfig.COMMON_SPEC);

        proxy = DistExecutor.unsafeRunForDist(() -> ClientHelper::new, () -> ServerHelper::new);

        CreativeTab.init();
        new FacadeItemGroup(); // This call has a side-effect (adding it to the creative screen)

        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        registration = new Registration();
        modEventBus.addListener(registration::registerDimension);
        modEventBus.addGenericListener(Block.class, registration::registerBlocks);
        modEventBus.addGenericListener(Item.class, registration::registerItems);
        modEventBus.addGenericListener(EntityType.class, registration::registerEntities);
        modEventBus.addGenericListener(ParticleType.class, registration::registerParticleTypes);
        modEventBus.addGenericListener(TileEntityType.class, registration::registerTileEntities);
        modEventBus.addGenericListener(ContainerType.class, registration::registerContainerTypes);
        modEventBus.addGenericListener(IRecipeSerializer.class, registration::registerRecipeSerializers);
        modEventBus.addGenericListener(Structure.class, registration::registerStructures);
        modEventBus.addGenericListener(Feature.class, registration::registerFeatures);

        modEventBus.addListener(Integrations::enqueueIMC);
        modEventBus.addListener(this::commonSetup);

        // Register client-only events
        DistExecutor.runWhenOn(Dist.CLIENT, () -> registration::registerClientEvents);
        DistExecutor.runWhenOn(Dist.CLIENT, () -> () -> modEventBus.addListener(this::clientSetup));

        TickHandler.setup(MinecraftForge.EVENT_BUS);

        MinecraftForge.EVENT_BUS.addListener(this::onServerAboutToStart);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopped);
        MinecraftForge.EVENT_BUS.addListener(this::serverStopping);
        MinecraftForge.EVENT_BUS.addListener(registration::registerCommands);

        MinecraftForge.EVENT_BUS.register(new PartPlacement());
        MinecraftForge.EVENT_BUS.addListener(registration::addWorldGenToBiome);

    }

    private void commonSetup(FMLCommonSetupEvent event) {
        ApiDefinitions definitions = Api.INSTANCE.definitions();
        definitions.getRegistry().getBootstrapComponents(IInitComponent.class)
                .forEachRemaining(IInitComponent::initialize);
        definitions.getRegistry().getBootstrapComponents(IPostInitComponent.class)
                .forEachRemaining(IPostInitComponent::postInitialize);

        Capabilities.register();
        Registration.setupInternalRegistries();
        Registration.postInit();

        registerNetworkHandler();

        AddonLoader.loadAddons(Api.INSTANCE);
    }

    @OnlyIn(Dist.CLIENT)
    private void clientSetup(FMLClientSetupEvent event) {

        ((ClientHelper) proxy).clientInit();

        RenderingRegistry.registerEntityRenderingHandler(TinyTNTPrimedEntity.TYPE, TinyTNTPrimedRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(SingularityEntity.TYPE,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(GrowingCrystalEntity.TYPE,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ChargedQuartzEntity.TYPE,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));

    }

    @Nonnull
    public static AppEng instance() {
        if (INSTANCE == null) {
            throw new IllegalStateException();
        }
        return INSTANCE;
    }

    public AdvancementTriggers getAdvancementTriggers() {
        return this.registration.advancementTriggers;
    }

    private void startService(final String serviceName, final Thread thread) {
        thread.setName(serviceName);
        thread.setPriority(Thread.MIN_PRIORITY);

        AELog.info("Starting " + serviceName);
        thread.start();
    }

    private void registerNetworkHandler() {
        final Stopwatch start = Stopwatch.createStarted();
        AELog.info("Post Initialization ( started )");

        // FIXME IntegrationRegistry.INSTANCE.postInit();
        // FIXME CrashReportExtender.registerCrashCallable( new
        // IntegrationCrashEnhancement() );

        AppEng.proxy.postInit();
        AEConfig.instance().save();

        NetworkHandler.init(new ResourceLocation(MOD_ID, "main"));

        AELog.info("Post Initialization ( ended after " + start.elapsed(TimeUnit.MILLISECONDS) + "ms )");
    }

    private void onServerAboutToStart(final FMLServerAboutToStartEvent evt) {
        WorldData.onServerStarting(evt.getServer());
    }

    private void serverStopping(final FMLServerStoppingEvent event) {
        WorldData.instance().onServerStopping();
    }

    private void serverStopped(final FMLServerStoppedEvent event) {
        WorldData.instance().onServerStoppped();
        TickHandler.instance().shutdown();
    }

}
