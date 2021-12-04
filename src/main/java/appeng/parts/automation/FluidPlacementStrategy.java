package appeng.parts.automation;

import java.util.HashSet;
import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEKey;

public class FluidPlacementStrategy implements PlacementStrategy {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;
    /**
     * The fluids that we tried to place unsuccessfully.
     */
    private final Set<Fluid> blocked = new HashSet<>();
    /**
     * {@link System#currentTimeMillis()} of when the last sound/visual effect was played by this plane.
     */
    private long lastEffect;

    public FluidPlacementStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host) {
        this.level = level;
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void clearBlocked() {
        blocked.clear();
    }

    @Override
    public long placeInWorld(AEKey f, long amount, Actionable type, boolean placeAsEntity) {
        if (placeAsEntity || !(f instanceof AEFluidKey fluidKey)) {
            return 0; // Not supported
        }

        if (amount < AEFluidKey.AMOUNT_BLOCK) {
            // need a full bucket
            return 0;
        }

        var fluid = fluidKey.getFluid();

        // We previously tried placing this fluid unsuccessfully, so don't check it again.
        if (blocked.contains(fluid)) {
            return 0;
        }

        // We do not support placing fluids with NBT for now
        if (fluidKey.hasTag()) {
            return 0;
        }

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

    /**
     * Only play the effect every 250ms.
     */
    protected final boolean throttleEffect() {
        long now = System.currentTimeMillis();
        if (now < lastEffect + 250) {
            return true;
        }
        lastEffect = now;
        return false;
    }

}
