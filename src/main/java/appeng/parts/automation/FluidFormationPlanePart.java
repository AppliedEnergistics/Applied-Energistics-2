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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.parts.IPartModel;
import appeng.api.storage.StorageChannels;
import appeng.api.storage.data.AEFluidKey;
import appeng.items.parts.PartModels;
import appeng.menu.implementations.FormationPlaneMenu;

public class FluidFormationPlanePart extends AbstractFormationPlanePart<AEFluidKey> {
    private static final PlaneModels MODELS = new PlaneModels("part/fluid_formation_plane",
            "part/fluid_formation_plane_on");

    /**
     * The fluids that we tried to place unsuccessfully.
     */
    private final Set<Fluid> blocked = new HashSet<>();

    @PartModels
    public static List<IPartModel> getModels() {
        return MODELS.getModels();
    }

    public FluidFormationPlanePart(ItemStack is) {
        super(is, StorageChannels.fluids());
    }

    @Override
    protected void clearBlocked(BlockGetter level, BlockPos pos) {
        blocked.clear();
    }

    @Override
    protected final long placeInWorld(AEFluidKey what, long amount, Actionable type) {
        if (amount < AEFluidKey.AMOUNT_BLOCK) {
            // need a full bucket
            return 0;
        }

        var fluid = what.getFluid();

        // We previously tried placing this fluid unsuccessfully, so don't check it again.
        if (blocked.contains(fluid)) {
            return 0;
        }

        // We do not support placing fluids with NBT for now
        if (what.hasTag()) {
            return 0;
        }

        var te = this.getHost().getBlockEntity();
        var level = te.getLevel();
        var side = this.getSide();
        var pos = te.getBlockPos().relative(side);
        var state = level.getBlockState(pos);

        if (!this.canPlace(level, state, pos, fluid)) {
            // Remember that this fluid cannot be placed right now.
            blocked.add(fluid);
            return 0;
        }

        if (type == Actionable.MODULATE) {
            // Placing water in nether voids the fluid, but plays effects
            if (level.dimensionType().ultraWarm() && fluid.is(FluidTags.WATER)) {
                playEvaporationEffect(level, pos);
            } else if (state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer
                    && fluid == Fluids.WATER) {
                liquidBlockContainer.placeLiquid(level, pos, state, ((FlowingFluid) fluid).getSource(false));
                playEmptySound(level, pos, fluid);
            } else {
                if (state.canBeReplaced(fluid) && !state.getMaterial().isLiquid()) {
                    level.destroyBlock(pos, true);
                }

                if (!level.setBlock(pos, fluid.defaultFluidState().createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE)
                        && !state.getFluidState().isSource()) {
                    return 0;
                } else {
                    playEmptySound(level, pos, fluid);
                }
            }
        }

        return AEFluidKey.AMOUNT_BLOCK;
    }

    @Override
    public boolean supportsEntityPlacement() {
        return false;
    }

    private void playEmptySound(Level level, BlockPos pos, Fluid fluid) {
        if (throttleEffect()) {
            return;
        }

        SoundEvent soundEvent = fluid.is(FluidTags.LAVA) ? SoundEvents.BUCKET_EMPTY_LAVA : SoundEvents.BUCKET_EMPTY;
        level.playSound(null, pos, soundEvent, SoundSource.BLOCKS, 1.0F, 1.0F);
        level.gameEvent(GameEvent.FLUID_PLACE, pos);
    }

    private void playEvaporationEffect(Level level, BlockPos pos) {
        if (throttleEffect()) {
            return;
        }

        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F,
                2.6F + (level.random.nextFloat() - level.random.nextFloat()) * 0.8F);

        for (int l = 0; l < 8; ++l) {
            level.addParticle(
                    ParticleTypes.LARGE_SMOKE,
                    (double) pos.getX() + Math.random(),
                    (double) pos.getY() + Math.random(),
                    (double) pos.getZ() + Math.random(),
                    0.0D,
                    0.0D,
                    0.0D);
        }
    }

    /**
     * Checks from {@link net.minecraft.world.item.BucketItem#emptyContents}
     */
    private boolean canPlace(Level level, BlockState state, BlockPos pos, Fluid fluid) {
        if (!(fluid instanceof FlowingFluid)) {
            return false;
        }

        // This check is in addition to vanilla's checks. If the fluid is already in place,
        // don't place it again. This is for water, since water is otherwise replaceable by water.
        if (state == fluid.defaultFluidState().createLegacyBlock()) {
            return false;
        }

        return state.isAir()
                || state.canBeReplaced(fluid)
                || state.getBlock() instanceof LiquidBlockContainer liquidBlockContainer
                        && liquidBlockContainer.canPlaceLiquid(level, pos, state, fluid);
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

    @Override
    public MenuType<?> getMenuType() {
        return FormationPlaneMenu.FLUID_TYPE;
    }
}
