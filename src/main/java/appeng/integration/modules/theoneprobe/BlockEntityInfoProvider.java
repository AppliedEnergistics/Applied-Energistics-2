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

package appeng.integration.modules.theoneprobe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import mcjty.theoneprobe.api.CompoundText;
import mcjty.theoneprobe.api.IBlockDisplayOverride;
import mcjty.theoneprobe.api.IProbeHitData;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.IProbeInfoProvider;
import mcjty.theoneprobe.api.ProbeMode;
import mcjty.theoneprobe.api.TextStyleClass;

import appeng.api.integrations.igtooltip.ClientRegistration;
import appeng.api.integrations.igtooltip.CommonRegistration;
import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.IconProvider;
import appeng.api.integrations.igtooltip.providers.ModNameProvider;
import appeng.api.integrations.igtooltip.providers.NameProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.AppEng;
import appeng.integration.modules.igtooltip.TooltipProviders;
import appeng.util.Platform;

public final class BlockEntityInfoProvider implements IProbeInfoProvider, IBlockDisplayOverride {
    private final List<ServerDataCollector> dataCollectors = new ArrayList<>();
    private final List<BodyCustomizer<?>> bodyCustomizers = new ArrayList<>();

    private final List<NameCustomizer<?>> nameCustomizers = new ArrayList<>();
    private final List<ModNameCustomizer<?>> modNameCustomizers = new ArrayList<>();
    private final List<IconCustomizer<?>> iconCustomizers = new ArrayList<>();

    public BlockEntityInfoProvider() {
        TooltipProviders.loadCommon(new CommonRegistration() {
            @Override
            public <T extends BlockEntity> void addBlockEntityData(Class<T> blockEntityClass,
                    ServerDataProvider<? super T> provider) {
                dataCollectors.add((blockEntity, player, serverData) -> {
                    if (blockEntityClass.isInstance(blockEntity)) {
                        var obj = blockEntityClass.cast(blockEntity);
                        provider.provideServerData(player, obj, serverData);
                    }
                });
            }
        });
        TooltipProviders.loadClient(new ClientRegistration() {
            @Override
            public <T extends BlockEntity> void addBlockEntityBody(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, BodyProvider<? super T> provider,
                    int priority) {
                bodyCustomizers.add(new BodyCustomizer<>(blockEntityClass, provider, priority));
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityIcon(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, IconProvider<? super T> provider,
                    int priority) {
                iconCustomizers.add(new IconCustomizer<>(blockEntityClass, provider, priority));
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityName(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, NameProvider<? super T> provider,
                    int priority) {
                nameCustomizers.add(new NameCustomizer<>(blockEntityClass, provider, priority));
            }

            @Override
            public <T extends BlockEntity> void addBlockEntityModName(Class<T> blockEntityClass,
                    Class<? extends Block> blockClass, ResourceLocation id, ModNameProvider<? super T> provider,
                    int priority) {
                modNameCustomizers.add(new ModNameCustomizer<>(blockEntityClass, provider, priority));
            }
        });

        nameCustomizers.sort(Comparator.comparingInt(NameCustomizer::priority));
        iconCustomizers.sort(Comparator.comparingInt(IconCustomizer::priority));
        modNameCustomizers.sort(Comparator.comparingInt(ModNameCustomizer::priority));
        bodyCustomizers.sort(Comparator.comparingInt(BodyCustomizer::priority));
    }

    @Override
    public ResourceLocation getID() {
        return AppEng.makeId("block-entity");
    }

    @Override
    public void addProbeInfo(ProbeMode mode, IProbeInfo probeInfo, Player player, Level level,
            BlockState blockState, IProbeHitData data) {
        var blockEntity = level.getBlockEntity(data.getPos());
        if (blockEntity != null) {
            var serverData = getServerData(player, blockEntity);

            // Then allow all providers to modify the probe info
            var context = getContext(player, data, serverData);
            var tooltipBuilder = new TopTooltipBuilder(probeInfo);
            for (var customizer : bodyCustomizers) {
                customizer.buildTooltip(blockEntity, context, tooltipBuilder);
            }
        }
    }

    @Override
    public boolean overrideStandardInfo(ProbeMode probeMode, IProbeInfo probeInfo, Player player, Level level,
            BlockState blockState, IProbeHitData probeHitData) {

        var blockEntity = level.getBlockEntity(probeHitData.getPos());
        if (blockEntity == null) {
            return false;
        }

        var serverData = getServerData(player, blockEntity);
        var context = getContext(player, probeHitData, serverData);

        // If any one of our providers customizes the info, we have to override it all
        Component name = null;
        String modName = null;
        ItemStack icon = null;

        for (var customizer : nameCustomizers) {
            name = customizer.getName(blockEntity, context);
            if (name != null) {
                break;
            }
        }

        for (var customizer : modNameCustomizers) {
            modName = customizer.getModName(blockEntity, context);
            if (modName != null) {
                break;
            }
        }

        for (var customizer : iconCustomizers) {
            icon = customizer.getIcon(blockEntity, context);
            if (icon != null) {
                break;
            }
        }

        if (name != null || modName != null || icon != null) {
            // Fill out defaults
            var pickBlock = probeHitData.getPickBlock();
            if (name == null) {
                name = pickBlock.getHoverName();
            }
            if (icon == null) {
                icon = pickBlock;
            }
            if (modName == null) {
                modName = Platform.getModName(BuiltInRegistries.ITEM.getKey(pickBlock.getItem()).getNamespace());
            }

            // TOP itself checks a config here to enable/disable the mod name, but I don't know how to get access to it
            probeInfo.horizontal().item(icon).vertical().text(name)
                    .text(CompoundText.create().style(TextStyleClass.MODNAME).text(modName));
            return true;
        }

        return false;
    }

    @FunctionalInterface
    private interface ServerDataCollector {
        void collect(BlockEntity blockEntity, ServerPlayer player, CompoundTag serverData);
    }

    private CompoundTag getServerData(Player player, BlockEntity blockEntity) {
        var serverData = new CompoundTag();

        // Emulate Jade/WTHIT/Waila model by collecting the server-data they would send to the client
        // into a temporary compound tag.
        if (player instanceof ServerPlayer serverPlayer) {
            for (var dataCollector : dataCollectors) {
                dataCollector.collect(blockEntity, serverPlayer, serverData);
            }
        }
        return serverData;
    }

    private static TooltipContext getContext(Player player, IProbeHitData data, CompoundTag serverData) {
        return new TooltipContext(
                serverData,
                data.getHitVec(),
                player);
    }

    record NameCustomizer<T> (Class<T> beClass, NameProvider<? super T> provider, int priority) {
        public Component getName(BlockEntity blockEntity, TooltipContext context) {
            if (beClass.isInstance(blockEntity)) {
                return provider.getName(beClass.cast(blockEntity), context);
            }
            return null;
        }
    }

    record IconCustomizer<T> (Class<T> beClass, IconProvider<? super T> provider, int priority) {
        public ItemStack getIcon(BlockEntity blockEntity, TooltipContext context) {
            if (beClass.isInstance(blockEntity)) {
                return provider.getIcon(beClass.cast(blockEntity), context);
            }
            return null;
        }
    }

    record ModNameCustomizer<T> (Class<T> beClass, ModNameProvider<? super T> provider, int priority) {
        public String getModName(BlockEntity blockEntity, TooltipContext context) {
            if (beClass.isInstance(blockEntity)) {
                return provider.getModName(beClass.cast(blockEntity), context);
            }
            return null;
        }
    }

    record BodyCustomizer<T> (Class<T> beClass, BodyProvider<? super T> provider, int priority) {
        public void buildTooltip(BlockEntity blockEntity, TooltipContext context, TooltipBuilder tooltipBuilder) {
            if (beClass.isInstance(blockEntity)) {
                provider.buildTooltip(beClass.cast(blockEntity), context, tooltipBuilder);
            }
        }
    }
}
