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

package appeng.entity;

import java.util.Random;

import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import appeng.api.implementations.blockentities.ICrystalGrowthAccelerator;
import appeng.api.implementations.items.IGrowableCrystal;
import appeng.client.EffectType;
import appeng.core.AEConfig;
import appeng.core.AppEng;
import appeng.core.definitions.AEEntities;
import appeng.items.misc.CrystalSeedItem;

public final class GrowingCrystalEntity extends AEBaseItemEntity {

    private static final Random CLIENT_EFFECTS_RNG = new Random();

    // Growth tick progress per tick by number of adjacent accelerators
    private static final int[] GROWTH_TICK_PROGRESS = {
            1, // no accelerators, 20 minutes
            10, // 1 accelerator, 2 minutes
            20, // 2 accelerators, 1 minute
            30, // 3 accelerators, 40 seconds
            40, // 4 accelerators, 30 seconds
            50, // 5 accelerators, 24 seconds
            60 // 6 accelerators, 20 seconds
    };

    public GrowingCrystalEntity(EntityType<? extends GrowingCrystalEntity> type, Level level) {
        super(type, level);
    }

    public GrowingCrystalEntity(final Level level, final double x, final double y, final double z, final ItemStack is) {
        super(AEEntities.GROWING_CRYSTAL, level, x, y, z, is);
        this.setExtendedLifetime();
    }

    @Override
    public void tick() {
        super.tick();

        final ItemStack is = this.getItem();
        final Item gc = is.getItem();

        if (!(gc instanceof IGrowableCrystal)) {
            return;
        }

        applyGrowthTick((IGrowableCrystal) gc, is);
    }

    private void applyGrowthTick(IGrowableCrystal crystal, ItemStack is) {
        if (!AEConfig.instance().isInWorldPurificationEnabled()) {
            return;
        }

        final int x = Mth.floor(this.getX());
        final int y = Mth.floor((this.getBoundingBox().minY + this.getBoundingBox().maxY) / 2.0D);
        final int z = Mth.floor(this.getZ());

        BlockPos pos = new BlockPos(x, y, z);
        final BlockState state = this.level.getBlockState(pos);

        final float multiplier = crystal.getMultiplier(state, level, pos);

        // Crystal is in unsuitable material, reset progress and quit
        if (multiplier <= 0) {
            return;
        }

        final int progressPerTick = (int) Math.max(1, this.getSpeed(pos) * multiplier);

        if (level.isClientSide()) {
            // Upper bound for a random chance to spawn a particle, depends on the growth speed.
            int bound = getTicksBetweenParticleEffects(progressPerTick);

            if (CLIENT_EFFECTS_RNG.nextInt(bound) == 0) {
                AppEng.instance().spawnEffect(EffectType.Vibrant, this.level, this.getX(), this.getY() + 0.2,
                        this.getZ(), null);
            }
        } else {
            // We need to copy the stack or the change detection will not work and not sync this new stack to the client
            ItemStack newItem = is.copy();

            // We still loop here to avoid overflows and can immediately stop once the crystal is fully grown.
            // Should a crystal decide to use a high multiplier for a certain material, it should be possible to go
            // faster. Copy value in case the original is needed again.
            int loopCounter = progressPerTick;
            do {
                newItem = crystal.triggerGrowth(newItem);
                loopCounter--;
            } while (loopCounter > 0 && newItem.getItem() == is.getItem());

            this.setItem(newItem);

            if (is.getItem() != newItem.getItem()
                    && this.getPersistentData().contains(CrystalSeedItem.TAG_PREVENT_MAGNET)) {
                this.getPersistentData().remove(CrystalSeedItem.TAG_PREVENT_MAGNET);
            }
        }
    }

    private static int getTicksBetweenParticleEffects(int progressPerTick) {
        if (progressPerTick >= GROWTH_TICK_PROGRESS[6]) {
            return 1; // on average 20 times per second
        } else if (progressPerTick >= GROWTH_TICK_PROGRESS[5]) {
            return 3;
        } else if (progressPerTick >= GROWTH_TICK_PROGRESS[4]) {
            return 7;
        } else if (progressPerTick >= GROWTH_TICK_PROGRESS[3]) {
            return 10;
        } else if (progressPerTick >= GROWTH_TICK_PROGRESS[2]) {
            return 15;
        } else if (progressPerTick >= GROWTH_TICK_PROGRESS[1]) {
            return 20;
        } else {
            return 40; // on average every 2 seconds
        }
    }

    /**
     * Gets the extra progress per tick in 1/1000th of a growth tick based on the surrounding accelerators.
     */
    private int getSpeed(BlockPos pos) {
        int acceleratorCount = getAcceleratorCount(pos);

        if (acceleratorCount < 0) {
            return GROWTH_TICK_PROGRESS[0];
        } else if (acceleratorCount >= GROWTH_TICK_PROGRESS.length) {
            return GROWTH_TICK_PROGRESS[GROWTH_TICK_PROGRESS.length - 1];
        } else {
            return GROWTH_TICK_PROGRESS[acceleratorCount];
        }
    }

    private int getAcceleratorCount(BlockPos pos) {
        int count = 0;

        MutableBlockPos testPos = new MutableBlockPos();
        for (Direction direction : Direction.values()) {
            if (this.isPoweredAccelerator(testPos.setWithOffset(pos, direction))) {
                count++;
            }
        }

        return count;
    }

    private boolean isPoweredAccelerator(BlockPos pos) {
        final BlockEntity te = this.level.getBlockEntity(pos);

        return te instanceof ICrystalGrowthAccelerator && ((ICrystalGrowthAccelerator) te).isPowered();
    }

    @Override
    protected void setUnderwaterMovement() {
        ItemStack item = getItem();

        // Make ungrown seeds sink, and fully grown seeds bouyant allowing for
        // automation based around dropping seeds between 5 CGAs, then catchiung
        // them on their way up.
        if (item.getItem() instanceof CrystalSeedItem) {
            Vec3 v = this.getDeltaMovement();

            // Apply a much smaller acceleration to make them slowly sink
            double yAccel = this.isNoGravity() ? 0 : -0.002;

            // Apply the x/z slow-down, and the y acceleration
            this.setDeltaMovement(v.x * 0.99, v.y + yAccel, v.z * 0.99);

            return;
        }
        super.setUnderwaterMovement();
    }

}
