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

package appeng.integration.modules.waila.tile;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.HitResult;

import mcp.mobius.waila.api.IBlockAccessor;
import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IRegistrar;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.TooltipPosition;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartHost;
import appeng.api.parts.SelectedPart;
import appeng.block.networking.CableBusBlock;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.integration.modules.waila.part.ChannelDataProvider;
import appeng.integration.modules.waila.part.GridNodeStateProvider;
import appeng.integration.modules.waila.part.IPartDataProvider;
import appeng.integration.modules.waila.part.P2PStateDataProvider;
import appeng.integration.modules.waila.part.StorageMonitorDataProvider;
import appeng.util.Platform;

public final class CableBusDataProvider {

    private static final List<IPartDataProvider> PROVIDERS = List.of(
            new ChannelDataProvider(),
            new StorageMonitorDataProvider(),
            new GridNodeStateProvider(),
            new P2PStateDataProvider(),
            new DebugDataProvider());

    private CableBusDataProvider() {
    }

    public static void register(IRegistrar registrar) {
        registrar.addDisplayItem(new IconProvider(), CableBusBlock.class);
        registrar.registerComponentProvider(new NameProvider(),
                TooltipPosition.HEAD, CableBusBlock.class);
        registrar.registerComponentProvider(new TooltipAdapter(),
                TooltipPosition.BODY, CableBusBlock.class);
        registrar.registerBlockDataProvider(new ServerDataAdapter(), CableBusBlockEntity.class);
    }

    private static class NameProvider implements IComponentProvider {
        @Override
        public void appendHead(List<Component> tooltip, IBlockAccessor accessor, IPluginConfig config) {
            var blockEntity = accessor.getBlockEntity();
            var hitResult = accessor.getHitResult();

            var selected = getPart(blockEntity, hitResult);

            Component name = null;
            if (selected.facade != null) {
                name = selected.facade.getItemStack().getHoverName();
            } else if (selected.part != null) {
                name = selected.part.getPartItem().asItem().getDescription();
            }

            // Replace the object name
            if (name != null) {
                tooltip.clear();
                tooltip.add(name.copy().withStyle(style -> {
                    // Don't overwrite a text color if one is present
                    if (style.getColor() == null) {
                        return style.withColor(ChatFormatting.WHITE);
                    } else {
                        return style;
                    }
                }));
            }
        }
    }

    private static class IconProvider implements IComponentProvider {
        @Override
        public ItemStack getDisplayItem(IBlockAccessor accessor, IPluginConfig config) {
            var blockEntity = accessor.getBlockEntity();
            var hitResult = accessor.getHitResult();

            var selected = getPart(blockEntity, hitResult);
            if (selected.facade != null) {
                return selected.facade.getItemStack();
            } else if (selected.part != null) {
                var item = selected.part.getPartItem();
                return new ItemStack(item);
            }
            return ItemStack.EMPTY;
        }
    }

    private static class TooltipAdapter implements IComponentProvider {
        @Override
        public void appendBody(List<Component> tooltip, IBlockAccessor accessor, IPluginConfig config) {
            // Pick the part the cursor is on
            var selected = getPart(accessor.getBlockEntity(), accessor.getHitResult());
            if (selected.part != null) {
                // Then pick the data for that particular part
                var partTag = accessor.getServerData().getCompound(getPartDataName(selected.side));

                for (var provider : PROVIDERS) {
                    provider.appendBody(selected.part, partTag, tooltip);
                }
            }
        }
    }

    private static class ServerDataAdapter implements IServerDataProvider<BlockEntity> {
        @Override
        public void appendServerData(CompoundTag serverData, ServerPlayer serverPlayer, Level level,
                BlockEntity blockEntity) {

            if (!(blockEntity instanceof CableBusBlockEntity cableBus)) {
                return;
            }

            var partTag = new CompoundTag();
            for (var location : Platform.DIRECTIONS_WITH_NULL) {
                var part = cableBus.getPart(location);
                if (part == null) {
                    continue;
                }

                for (var provider : PROVIDERS) {
                    provider.appendServerData(serverPlayer, part, partTag);
                }

                // Send it to the client if there's some data for it
                if (!partTag.isEmpty()) {
                    serverData.put(getPartDataName(location), partTag);
                    partTag = new CompoundTag();
                }
            }

        }
    }

    private static String getPartDataName(@Nullable Direction location) {
        return "cableBusPart" + (location == null ? "center" : location.name());
    }

    /**
     * Hits a {@link IPartHost} with {@link net.minecraft.core.BlockPos}.
     * <p/>
     * You can derive the looked at {@link IPart} by doing that. If a facade is being looked at, it is defined as being
     * absent.
     *
     * @param blockEntity being looked at {@link BlockEntity}
     * @param hitResult   type of ray-trace
     * @return maybe the looked at {@link IPart}
     */
    private static SelectedPart getPart(BlockEntity blockEntity, HitResult hitResult) {
        if (blockEntity instanceof IPartHost host) {
            return host.selectPartWorld(hitResult.getLocation());
        }

        return new SelectedPart();
    }

}
