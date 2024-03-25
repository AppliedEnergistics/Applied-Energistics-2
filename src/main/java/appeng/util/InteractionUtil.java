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

import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import appeng.datagen.providers.tags.ConventionTags;
import appeng.items.tools.NetworkToolItem;

/**
 * Utility functions revolving around using or placing items.
 */
public final class InteractionUtil {

    private InteractionUtil() {
    }

    /**
     * Checks if the given tool is a wrench capable of disassembling.
     */
    public static boolean canWrenchDisassemble(ItemStack tool) {
        // TODO FABRIC 117 Currently Fabric cannot dynamically distinguish tools / tool actions
        return tool.is(ConventionTags.WRENCH);
    }

    /**
     * Checks if the given tool is a wrench capable of rotating.
     */
    public static boolean canWrenchRotate(ItemStack tool) {
        // TODO FABRIC 117 Currently Fabric cannot dynamically distinguish tools / tool actions
        // Special case to stop the network tool from rotating things instead of opening the appropriate UI
        if (tool.getItem() instanceof NetworkToolItem) {
            return false;
        }

        return tool.is(ConventionTags.WRENCH);
    }

    /**
     * Checks if the given player is in the alternate use mode commonly expressed by "crouching" (holding shift).
     * Although there's also {@link Player#isCrouching()}, this actually is only the visual pose, while
     * {@link Player#isShiftKeyDown()} signifies that the player is holding shift.
     */
    public static boolean isInAlternateUseMode(Player player) {
        return player.isShiftKeyDown();
    }

    public static LookDirection getPlayerRay(Player playerIn, double reachDistance) {
        var x = playerIn.xo + (playerIn.getX() - playerIn.xo);
        var y = playerIn.yo + (playerIn.getY() - playerIn.yo) + playerIn.getEyeHeight();
        var z = playerIn.zo + (playerIn.getZ() - playerIn.zo);

        var playerPitch = playerIn.xRotO + (playerIn.getXRot() - playerIn.xRotO);
        var playerYaw = playerIn.yRotO + (playerIn.getYRot() - playerIn.yRotO);

        var yawRayX = Mth.sin(-playerYaw * 0.017453292f - (float) Math.PI);
        var yawRayZ = Mth.cos(-playerYaw * 0.017453292f - (float) Math.PI);

        var pitchMultiplier = -Mth.cos(-playerPitch * 0.017453292F);
        var eyeRayY = Mth.sin(-playerPitch * 0.017453292F);
        var eyeRayX = yawRayX * pitchMultiplier;
        var eyeRayZ = yawRayZ * pitchMultiplier;

        var from = new Vec3(x, y, z);
        var to = from.add(eyeRayX * reachDistance, eyeRayY * reachDistance, eyeRayZ * reachDistance);

        return new LookDirection(from, to);
    }
}
