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

package appeng.util;

import java.util.List;

import net.minecraft.world.entity.Entity;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraftforge.common.ForgeMod;

import appeng.api.implementations.items.IAEWrench;

/**
 * Utility functions revolving around using or placing items.
 */
public final class InteractionUtil {

    private InteractionUtil() {
    }

    public static boolean isWrench(final Player player, final ItemStack eq, final BlockPos pos) {
        if (!eq.isEmpty()) {
            try {
                // TODO: Build Craft Wrench?
                /*
                 * if( eq.getItem() instanceof IToolWrench ) { IToolWrench wrench = (IToolWrench) eq.getItem(); return
                 * wrench.canWrench( player, x, y, z ); }
                 */

                // FIXME if( eq.getItem() instanceof cofh.api.item.IToolHammer )
                // FIXME {
                // FIXME return ( (cofh.api.item.IToolHammer) eq.getItem() ).isUsable( eq,
                // player, pos );
                // FIXME }
            } catch (final Throwable ignore) { // explodes without BC

            }

            if (eq.getItem() instanceof IAEWrench) {
                final IAEWrench wrench = (IAEWrench) eq.getItem();
                return wrench.canWrench(eq, player, pos);
            }
        }
        return false;
    }

    /**
     * Checks if the given player is in the alternate use mode commonly expressed by "crouching" (holding shift).
     * Although there's also {@link Player#isCrouching()}, this actually is only the visual pose, while
     * {@link Player#isSneaking()} signifies that the player is holding shift.
     */
    public static boolean isInAlternateUseMode(Player player) {
        return player.isShiftKeyDown();
    }

    public static float getEyeOffset(final Player player) {
        assert player.level.isClientSide : "Valid only on client";
        // FIXME: The entire premise of this seems broken
        return (float) (player.getY() + player.getEyeHeight() - /* FIXME player.getDefaultEyeHeight() */ 1.62F);
    }

    public static LookDirection getPlayerRay(final Player playerIn) {
        double reachDistance = playerIn.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        return getPlayerRay(playerIn, reachDistance);
    }

    public static LookDirection getPlayerRay(final Player playerIn, double reachDistance) {
        final double x = playerIn.xo + (playerIn.getX() - playerIn.xo);
        final double y = playerIn.yo + (playerIn.getY() - playerIn.yo) + playerIn.getEyeHeight();
        final double z = playerIn.zo + (playerIn.getZ() - playerIn.zo);

        final float playerPitch = playerIn.xRotO + (playerIn.xRot - playerIn.xRotO);
        final float playerYaw = playerIn.yRotO + (playerIn.yRot - playerIn.yRotO);

        final float yawRayX = Mth.sin(-playerYaw * 0.017453292f - (float) Math.PI);
        final float yawRayZ = Mth.cos(-playerYaw * 0.017453292f - (float) Math.PI);

        final float pitchMultiplier = -Mth.cos(-playerPitch * 0.017453292F);
        final float eyeRayY = Mth.sin(-playerPitch * 0.017453292F);
        final float eyeRayX = yawRayX * pitchMultiplier;
        final float eyeRayZ = yawRayZ * pitchMultiplier;

        final Vec3 from = new Vec3(x, y, z);
        final Vec3 to = from.add(eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance);

        return new LookDirection(from, to);
    }

    public static HitResult rayTrace(final Player p, final boolean hitBlocks, final boolean hitEntities) {
        final Level w = p.getCommandSenderWorld();

        final float f = 1.0F;
        float f1 = p.xRotO + (p.xRot - p.xRotO) * f;
        final float f2 = p.yRotO + (p.yRot - p.yRotO) * f;
        final double d0 = p.xo + (p.getX() - p.xo) * f;
        final double d1 = p.yo + (p.getY() - p.yo) * f + 1.62D - p.getMyRidingOffset();
        final double d2 = p.zo + (p.getZ() - p.zo) * f;
        final Vec3 vec3 = new Vec3(d0, d1, d2);
        final float f3 = Mth.cos(-f2 * 0.017453292F - (float) Math.PI);
        final float f4 = Mth.sin(-f2 * 0.017453292F - (float) Math.PI);
        final float f5 = -Mth.cos(-f1 * 0.017453292F);
        final float f6 = Mth.sin(-f1 * 0.017453292F);
        final float f7 = f4 * f5;
        final float f8 = f3 * f5;
        final double d3 = 32.0D;

        final Vec3 vec31 = vec3.add(f7 * d3, f6 * d3, f8 * d3);

        final AABB bb = new AABB(Math.min(vec3.x, vec31.x), Math.min(vec3.y, vec31.y),
                Math.min(vec3.z, vec31.z), Math.max(vec3.x, vec31.x), Math.max(vec3.y, vec31.y),
                Math.max(vec3.z, vec31.z)).inflate(16, 16, 16);

        Entity entity = null;
        double closest = 9999999.0D;
        if (hitEntities) {
            final List<Entity> list = w.getEntities(p, bb);

            for (final Entity entity1 : list) {
                if (entity1.isAlive() && entity1 != p && !(entity1 instanceof ItemEntity)) {
                    // prevent killing / flying of mounts.
                    if (entity1.hasIndirectPassenger(p)) {
                        continue;
                    }

                    f1 = 0.3F;
                    // FIXME: Three different bounding boxes available, should double-check
                    final AABB boundingBox = entity1.getBoundingBox().inflate(f1, f1, f1);
                    final Vec3 rtResult = boundingBox.clip(vec3, vec31).orElse(null);

                    if (rtResult != null) {
                        final double nd = vec3.distanceToSqr(rtResult);

                        if (nd < closest) {
                            entity = entity1;
                            closest = nd;
                        }
                    }
                }
            }
        }

        HitResult pos = null;
        Vec3 vec = null;

        if (hitBlocks) {
            vec = new Vec3(d0, d1, d2);
            // FIXME: passing p as entity here might be incorrect
            pos = w.clip(new ClipContext(vec3, vec31, Block.COLLIDER,
                    Fluid.ANY, p));
        }

        if (entity != null && pos != null && pos.getLocation().distanceToSqr(vec) > closest) {
            pos = new EntityHitResult(entity);
        } else if (entity != null && pos == null) {
            pos = new EntityHitResult(entity);
        }

        return pos;
    }
}
