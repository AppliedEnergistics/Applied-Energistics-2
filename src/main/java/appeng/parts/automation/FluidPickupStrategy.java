package appeng.parts.automation;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import appeng.api.behaviors.PickupSink;
import appeng.api.behaviors.PickupStrategy;
import appeng.api.behaviors.PickupStrategy.Result;
import appeng.api.config.Actionable;
import appeng.api.ids.AETags;
import appeng.api.networking.energy.IEnergySource;
import appeng.api.stacks.AEFluidKey;
import appeng.core.AppEng;
import appeng.core.sync.packets.BlockTransitionEffectPacket;
import appeng.helpers.FluidContainerHelper;

public class FluidPickupStrategy implements PickupStrategy {
    private final ServerLevel level;
    private final BlockPos pos;
    private final Direction side;

    /**
     * {@link System#currentTimeMillis()} of when the last sound/visual effect was played by this plane.
     */
    private long lastEffect;

    public FluidPickupStrategy(ServerLevel level, BlockPos pos, Direction side, BlockEntity host,
            Map<?, ?> enchantments) {
        this.level = level;
        this.pos = pos;
        this.side = side;
    }

    @Override
    public void reset() {
    }

    @Override
    public boolean canPickUpEntity(Entity entity) {
        return false;
    }

    @Override
    public boolean pickUpEntity(IEnergySource energySource, PickupSink sink, Entity entity) {
        return false;
    }

    @Override
    public Result tryStartPickup(IEnergySource energySource, PickupSink sink) {
        var blockstate = level.getBlockState(pos);
        if (blockstate.getBlock() instanceof BucketPickup bucketPickup) {
            var fluidState = blockstate.getFluidState();

            var fluid = fluidState.getType();
            if (isFluidBlacklisted(fluid)) {
                return Result.CANT_PICKUP;
            }

            if (fluid != Fluids.EMPTY && fluidState.isSource()) {
                // Attempt to store the fluid in the network
                var what = AEFluidKey.of(fluid);
                if (this.storeFluid(sink, what, AEFluidKey.AMOUNT_BLOCK, false)) {
                    // If that would succeed, actually slurp up the liquid as if we were using a
                    // bucket
                    // This _MIGHT_ change the liquid, and if it does, and we dont have enough
                    // space, tough luck. you loose the source block.
                    var fluidContainer = bucketPickup.pickupBlock(level, pos, blockstate);
                    var pickedUpStack = FluidContainerHelper.getContainedStack(fluidContainer);
                    if (pickedUpStack != null && pickedUpStack.what() instanceof AEFluidKey fluidKey) {
                        this.storeFluid(sink, fluidKey, pickedUpStack.amount(), true);
                    }

                    if (!throttleEffect()) {
                        AppEng.instance().sendToAllNearExcept(null, pos.getX(), pos.getY(), pos.getZ(), 64, level,
                                new BlockTransitionEffectPacket(pos, blockstate, side,
                                        BlockTransitionEffectPacket.SoundMode.FLUID));
                    }

                    return Result.PICKED_UP;
                }
                return Result.CANT_STORE;
            }
        }

        // nothing to do here :)
        return Result.CANT_PICKUP;
    }

    @Override
    public void completePickup(IEnergySource energySource, PickupSink sink) {
    }

    private boolean storeFluid(PickupSink sink, AEFluidKey what, long amount, boolean modulate) {
        return sink.insert(what, amount, modulate ? Actionable.MODULATE : Actionable.SIMULATE) >= amount;
    }

    private boolean isFluidBlacklisted(Fluid fluid) {
        return fluid.builtInRegistryHolder().is(AETags.ANNIHILATION_PLANE_FLUID_BLACKLIST);
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
