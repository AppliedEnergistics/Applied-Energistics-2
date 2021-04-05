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

package appeng.integration.modules.waila;

import java.util.List;
import java.util.Optional;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import mcp.mobius.waila.api.IComponentProvider;
import mcp.mobius.waila.api.IDataAccessor;
import mcp.mobius.waila.api.IPluginConfig;
import mcp.mobius.waila.api.IServerDataProvider;

import appeng.api.parts.IPart;
import appeng.api.parts.PartItemStack;
import appeng.integration.modules.waila.part.ChannelWailaDataProvider;
import appeng.integration.modules.waila.part.IPartWailaDataProvider;
import appeng.integration.modules.waila.part.P2PStateWailaDataProvider;
import appeng.integration.modules.waila.part.PartAccessor;
import appeng.integration.modules.waila.part.PartStackWailaDataProvider;
import appeng.integration.modules.waila.part.PowerStateWailaDataProvider;
import appeng.integration.modules.waila.part.StorageMonitorWailaDataProvider;
import appeng.integration.modules.waila.part.Tracer;

/**
 * Delegation provider for parts through {@link IPartWailaDataProvider}
 *
 * @author thatsIch
 * @version rv2
 * @since rv2
 */
public final class PartWailaDataProvider implements IComponentProvider, IServerDataProvider<TileEntity> {
    /**
     * Contains all providers
     */
    private final List<IPartWailaDataProvider> providers;

    /**
     * Can access parts through view-hits
     */
    private final PartAccessor accessor = new PartAccessor();

    /**
     * Traces views hit on blocks
     */
    private final Tracer tracer = new Tracer();

    /**
     * Initializes the provider list with all wanted providers
     */
    public PartWailaDataProvider() {
        final IPartWailaDataProvider channel = new ChannelWailaDataProvider();
        final IPartWailaDataProvider storageMonitor = new StorageMonitorWailaDataProvider();
        final IPartWailaDataProvider powerState = new PowerStateWailaDataProvider();
        final IPartWailaDataProvider p2pState = new P2PStateWailaDataProvider();
        final IPartWailaDataProvider partStack = new PartStackWailaDataProvider();

        this.providers = Lists.newArrayList(channel, storageMonitor, powerState, partStack, p2pState);
    }

    @Override
    public ItemStack getStack(final IDataAccessor accessor, final IPluginConfig config) {
        final TileEntity te = accessor.getBlockEntity();
        final RayTraceResult mop = accessor.getHitResult();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            ItemStack wailaStack = ItemStack.EMPTY;

            for (final IPartWailaDataProvider provider : this.providers) {
                wailaStack = provider.getStack(part, config, wailaStack);
            }
            return wailaStack;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public void appendHead(List<ITextComponent> currentToolTip, final IDataAccessor accessor,
            final IPluginConfig config) {
        final TileEntity te = accessor.getBlockEntity();
        final RayTraceResult mop = accessor.getHitResult();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            currentToolTip.clear();
            currentToolTip.add(part.getItemStack(PartItemStack.PICK).getDisplayName());

            for (final IPartWailaDataProvider provider : this.providers) {
                provider.appendHead(part, currentToolTip, accessor, config);
            }
        }
    }

    @Override
    public void appendBody(List<ITextComponent> tooltip, final IDataAccessor accessor, final IPluginConfig config) {
        final TileEntity te = accessor.getBlockEntity();
        final RayTraceResult mop = accessor.getHitResult();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            for (final IPartWailaDataProvider provider : this.providers) {
                provider.appendBody(part, tooltip, accessor, config);
            }
        }
    }

    @Override
    public void appendTail(List<ITextComponent> tooltip, final IDataAccessor accessor, final IPluginConfig config) {
        final TileEntity te = accessor.getBlockEntity();
        final RayTraceResult mop = accessor.getHitResult();

        final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

        if (maybePart.isPresent()) {
            final IPart part = maybePart.get();

            for (final IPartWailaDataProvider provider : this.providers) {
                provider.appendTail(part, tooltip, accessor, config);
            }
        }
    }

    @Override
    public void appendServerData(CompoundNBT tag, ServerPlayerEntity player, World world, TileEntity te) {
        final RayTraceResult mop = this.tracer.retraceBlock(world, player, te.getPos());

        if (mop != null) {
            final Optional<IPart> maybePart = this.accessor.getMaybePart(te, mop);

            if (maybePart.isPresent()) {
                final IPart part = maybePart.get();

                for (final IPartWailaDataProvider provider : this.providers) {
                    provider.appendServerData(player, part, te, tag, world, te.getPos());
                }
            }
        }
    }
}
