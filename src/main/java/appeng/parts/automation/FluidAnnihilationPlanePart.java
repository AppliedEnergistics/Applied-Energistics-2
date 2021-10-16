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
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidConstants;
import net.fabricmc.fabric.api.transfer.v1.fluid.FluidVariant;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.security.IActionSource;
import appeng.api.networking.storage.IStorageService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.parts.IPartCollisionHelper;
import appeng.api.parts.IPartModel;
import appeng.api.storage.IMEInventory;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.IAEFluidStack;
import appeng.api.util.AECableType;
import appeng.core.AppEng;
import appeng.core.settings.TickRates;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.helpers.FluidContainerHelper;
import appeng.items.parts.PartModels;
import appeng.me.helpers.MachineSource;
import appeng.parts.BasicStatePart;
import appeng.util.Platform;
import appeng.util.fluid.AEFluidStack;

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

        final BlockEntity te = this.getBlockEntity();
        final Level level = te.getLevel();
        final BlockPos pos = te.getBlockPos().relative(this.getSide());

        BlockState blockstate = level.getBlockState(pos);
        if (blockstate.getBlock() instanceof BucketPickup bucketPickup) {
            FluidState fluidState = blockstate.getFluidState();

            Fluid fluid = fluidState.getType();
            if (isFluidBlacklisted(fluid)) {
                return TickRateModulation.SLEEP;
            }

            if (fluid != Fluids.EMPTY && fluidState.isSource()) {
                // Attempt to store the fluid in the network
                var blockFluid = AEFluidStack.of(FluidVariant.of(fluid), FluidConstants.BLOCK);
                if (this.storeFluid(grid, blockFluid, false)) {
                    // If that would succeed, actually slurp up the liquid as if we were using a
                    // bucket
                    // This _MIGHT_ change the liquid, and if it does, and we dont have enough
                    // space, tough luck. you loose the source block.
                    var fluidContainer = bucketPickup.pickupBlock(level, pos, blockstate);
                    var pickedUpStack = FluidContainerHelper.getContainedFluid(fluidContainer);
                    if (pickedUpStack != null) {
                        this.storeFluid(grid, pickedUpStack, true);
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

    private boolean storeFluid(IGrid grid, IAEFluidStack stack, boolean modulate) {
        final IStorageService storage = grid.getStorageService();
        final IMEInventory<IAEFluidStack> inv = storage
                .getInventory(StorageChannels.fluids());

        if (modulate) {
            var energy = grid.getEnergyService();
            return Platform.poweredInsert(energy, inv, stack, this.mySrc) == null;
        } else {
            var requiredPower = stack.getStackSize() / Math.min(1.0f, stack.getChannel().transferFactor());
            final IEnergyService energy = grid.getEnergyService();

            if (energy.extractAEPower(requiredPower, Actionable.SIMULATE, PowerMultiplier.CONFIG) < requiredPower) {
                return false;
            }
            final IAEFluidStack leftOver = inv.injectItems(stack, Actionable.SIMULATE, this.mySrc);
            return leftOver == null || leftOver.getStackSize() == 0;
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
        long now = System.currentTimeMillis();
        if (now < lastEffect + 250) {
            return true;
        }
        lastEffect = now;
        return false;
    }

}
