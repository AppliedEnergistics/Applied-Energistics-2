package appeng.util;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeMod;

import appeng.api.implementations.items.IAEWrench;

/**
 * Utility functions revolving around using or placing items.
 */
public final class InteractionUtil {

    private InteractionUtil() {
    }

    public static boolean isWrench(final PlayerEntity player, final ItemStack eq, final BlockPos pos) {
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
     * Although there's also {@link PlayerEntity#isCrouching()}, this actually is only the visual pose, while
     * {@link PlayerEntity#isSneaking()} signifies that the player is holding shift.
     */
    public static boolean isInAlternateUseMode(PlayerEntity player) {
        return player.isSneaking();
    }

    public static float getEyeOffset(final PlayerEntity player) {
        assert player.world.isRemote : "Valid only on client";
        // FIXME: The entire premise of this seems broken
        return (float) (player.getPosY() + player.getEyeHeight() - /* FIXME player.getDefaultEyeHeight() */ 1.62F);
    }

    public static LookDirection getPlayerRay(final PlayerEntity playerIn) {
        double reachDistance = playerIn.getAttribute(ForgeMod.REACH_DISTANCE.get()).getValue();
        return getPlayerRay(playerIn, reachDistance);
    }

    public static LookDirection getPlayerRay(final PlayerEntity playerIn, double reachDistance) {
        final double x = playerIn.prevPosX + (playerIn.getPosX() - playerIn.prevPosX);
        final double y = playerIn.prevPosY + (playerIn.getPosY() - playerIn.prevPosY) + playerIn.getEyeHeight();
        final double z = playerIn.prevPosZ + (playerIn.getPosZ() - playerIn.prevPosZ);

        final float playerPitch = playerIn.prevRotationPitch + (playerIn.rotationPitch - playerIn.prevRotationPitch);
        final float playerYaw = playerIn.prevRotationYaw + (playerIn.rotationYaw - playerIn.prevRotationYaw);

        final float yawRayX = MathHelper.sin(-playerYaw * 0.017453292f - (float) Math.PI);
        final float yawRayZ = MathHelper.cos(-playerYaw * 0.017453292f - (float) Math.PI);

        final float pitchMultiplier = -MathHelper.cos(-playerPitch * 0.017453292F);
        final float eyeRayY = MathHelper.sin(-playerPitch * 0.017453292F);
        final float eyeRayX = yawRayX * pitchMultiplier;
        final float eyeRayZ = yawRayZ * pitchMultiplier;

        final Vector3d from = new Vector3d(x, y, z);
        final Vector3d to = from.add(eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance);

        return new LookDirection(from, to);
    }

    public static RayTraceResult rayTrace(final PlayerEntity p, final boolean hitBlocks, final boolean hitEntities) {
        final World w = p.getEntityWorld();

        final float f = 1.0F;
        float f1 = p.prevRotationPitch + (p.rotationPitch - p.prevRotationPitch) * f;
        final float f2 = p.prevRotationYaw + (p.rotationYaw - p.prevRotationYaw) * f;
        final double d0 = p.prevPosX + (p.getPosX() - p.prevPosX) * f;
        final double d1 = p.prevPosY + (p.getPosY() - p.prevPosY) * f + 1.62D - p.getYOffset();
        final double d2 = p.prevPosZ + (p.getPosZ() - p.prevPosZ) * f;
        final Vector3d vec3 = new Vector3d(d0, d1, d2);
        final float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        final float f4 = MathHelper.sin(-f2 * 0.017453292F - (float) Math.PI);
        final float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        final float f6 = MathHelper.sin(-f1 * 0.017453292F);
        final float f7 = f4 * f5;
        final float f8 = f3 * f5;
        final double d3 = 32.0D;

        final Vector3d vec31 = vec3.add(f7 * d3, f6 * d3, f8 * d3);

        final AxisAlignedBB bb = new AxisAlignedBB(Math.min(vec3.x, vec31.x), Math.min(vec3.y, vec31.y),
                Math.min(vec3.z, vec31.z), Math.max(vec3.x, vec31.x), Math.max(vec3.y, vec31.y),
                Math.max(vec3.z, vec31.z)).grow(16, 16, 16);

        Entity entity = null;
        double closest = 9999999.0D;
        if (hitEntities) {
            final List<Entity> list = w.getEntitiesWithinAABBExcludingEntity(p, bb);

            for (final Entity entity1 : list) {
                if (entity1.isAlive() && entity1 != p && !(entity1 instanceof ItemEntity)) {
                    // prevent killing / flying of mounts.
                    if (entity1.isRidingOrBeingRiddenBy(p)) {
                        continue;
                    }

                    f1 = 0.3F;
                    // FIXME: Three different bounding boxes available, should double-check
                    final AxisAlignedBB boundingBox = entity1.getBoundingBox().grow(f1, f1, f1);
                    final Vector3d rtResult = boundingBox.rayTrace(vec3, vec31).orElse(null);

                    if (rtResult != null) {
                        final double nd = vec3.squareDistanceTo(rtResult);

                        if (nd < closest) {
                            entity = entity1;
                            closest = nd;
                        }
                    }
                }
            }
        }

        RayTraceResult pos = null;
        Vector3d vec = null;

        if (hitBlocks) {
            vec = new Vector3d(d0, d1, d2);
            // FIXME: passing p as entity here might be incorrect
            pos = w.rayTraceBlocks(new RayTraceContext(vec3, vec31, RayTraceContext.BlockMode.COLLIDER,
                    RayTraceContext.FluidMode.ANY, p));
        }

        if (entity != null && pos != null && pos.getHitVec().squareDistanceTo(vec) > closest) {
            pos = new EntityRayTraceResult(entity);
        } else if (entity != null && pos == null) {
            pos = new EntityRayTraceResult(entity);
        }

        return pos;
    }
}
