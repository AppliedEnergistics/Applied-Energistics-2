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

package appeng.parts.automation;

import java.util.List;

import javax.annotation.Nonnull;

import net.fabricmc.fabric.api.tag.TagRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.StorageHelper;
import appeng.api.storage.data.AEFluidKey;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.helpers.FluidContainerHelper;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.parts.BasicStatePart;

public class FluidAnnihilationPlanePart extends BasicStatePart implements IGridTickable {

    public static final Tag<Fluid> TAG_BLACKLIST = TagRegistry
            .fluid(AppEng.makeId("blacklisted/fluid_annihilation_plane"));

    private static final PlaneModels MODELS = new PlaneModels("part/fluid_annihilation_plane",
            "part/fluid_annihilation_plane_on");

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    /**
     * {@link System#currentTimeMillis()} of when the last sound/visual effect was played by this plane.
     */
    private long lastEffect;

    private final IActionSource mySrc = new MachineSource(this);

    private final PlaneConnectionHelper connectionHelper = new PlaneConnectionHelper(this);

    public FluidAnnihilationPlanePart(final ItemStack is) {
        super(is);
        getMainNode().addService(IGridTickable.class, this);
    }

    @Override
    public void getBoxes(final IPartCollisionHelper bch) {
        connectionHelper.getBoxes(bch);
    }

    public PlaneConnections getConnections() {
        return connectionHelper.getConnections();
    }

    @Override
    public void onNeighborChanged(BlockGetter level, BlockPos pos, BlockPos neighbor) {
        if (pos.relative(this.getSide()).equals(neighbor)) {
            this.refresh();
        } else {
            connectionHelper.updateConnections();
        }
    }

    @Override
    public float getCableConnectionLength(AECableType cable) {
        return 1;
    }

    private void refresh() {
        getMainNode().ifPresent((g, n) -> g.getTickManager().alertDevice(n));
    }

    @Override
    protected void onMainNodeStateChanged(IGridNodeListener.State reason) {
        super.onMainNodeStateChanged(reason);
        this.refresh();
    }

    private TickRateModulation pickupFluid(IGrid grid) {
        if (!this.getMainNode().isActive()) {
            return TickRateModulation.SLEEP;
        }

        final var te = this.getBlockEntity();
        final var level = te.getLevel();
        final var pos = te.getBlockPos().relative(this.getSide());

        var blockstate = level.getBlockState(pos);
        if (blockstate.getBlock() instanceof BucketPickup bucketPickup) {
            var fluidState = blockstate.getFluidState();

            var fluid = fluidState.getType();
            if (isFluidBlacklisted(fluid)) {
                return TickRateModulation.SLEEP;
            }

            if (fluid != Fluids.EMPTY && fluidState.isSource()) {
                // Attempt to store the fluid in the network
                var what = AEFluidKey.of(fluid);
                if (this.storeFluid(grid, what, AEFluidKey.AMOUNT_BLOCK, false)) {
                    // If that would succeed, actually slurp up the liquid as if we were using a
                    // bucket
                    // This _MIGHT_ change the liquid, and if it does, and we dont have enough
                    // space, tough luck. you loose the source block.
                    var fluidContainer = bucketPickup.pickupBlock(level, pos, blockstate);
                    var pickedUpStack = FluidContainerHelper.getContainedStack(fluidContainer);
                    if (pickedUpStack != null && pickedUpStack.what() instanceof AEFluidKey fluidKey) {
                        this.storeFluid(grid, fluidKey, pickedUpStack.amount(), true);
                    }

                    if (!throttleEffect()) {
                        AppEng.instance().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, level,
                                new BlockTransitionEffectPacket(pos, blockstate, this.getSide().getOpposite(),
                                        BlockTransitionEffectPacket.SoundMode.FLUID));
                    }

                    return TickRateModulation.URGENT;
                }
                return TickRateModulation.IDLE;
            }
        }

        // nothing to do here :)
        return TickRateModulation.SLEEP;
    }

    @Override
    public TickingRequest getTickingRequest(final IGridNode node) {
        return new TickingRequest(TickRates.AnnihilationPlane.getMin(), TickRates.AnnihilationPlane.getMax(), false,
                true);
    }

    @Override
    public TickRateModulation tickingRequest(final IGridNode node, final int ticksSinceLastCall) {
        return this.pickupFluid(node.getGrid());
    }

    private boolean storeFluid(IGrid grid, AEFluidKey what, long amount, boolean modulate) {
        final var storage = grid.getStorageService();
        var inv = storage.getInventory();

        if (modulate) {
            var energy = grid.getEnergyService();
            return StorageHelper.poweredInsert(energy, inv, what, amount, this.mySrc) >= amount;
        } else {
            var requiredPower = amount / Math.min(1.0f, what.getChannel().transferFactor());
            final var energy = grid.getEnergyService();

            if (energy.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) < requiredPower) {
                return false;
            }
            return inv.insert(what, amount, Actionable.SIMULATE, this.mySrc) >= amount;
        }
    }

    @Override
    public IPartModel getStaticModels() {
        return MODELS.getModel(this.isPowered(), this.isActive());
    }

    @Nonnull
    @Override
    public Object getRenderAttachmentData() {
        return getConnections();
    }

    private boolean isFluidBlacklisted(Fluid fluid) {
        return TAG_BLACKLIST.contains(fluid);
    }

    /**
     * Only play the effect every 250ms.
     */
    private boolean throttleEffect() {
        var now = System.currentTimeMillis();
        if (now < lastEffect + 250) {
            return true;
        }
        lastEffect = now;
        return false;
    }

}
