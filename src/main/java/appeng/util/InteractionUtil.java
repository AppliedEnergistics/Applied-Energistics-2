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

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.core.ConventionTags;
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
        return tool.is(ConventionTags.WRENCH);
    }

    /**
     * Checks if the given tool is a wrench capable of rotating.
     */
    public static boolean canWrenchRotate(ItemStack tool) {
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
        var eyePosition = playerIn.getEyePosition(1);
        var viewVector = playerIn.getViewVector(1);
        var rayEnd = eyePosition.add(viewVector.x * reachDistance, viewVector.y * reachDistance,
                viewVector.z * reachDistance);
        return new LookDirection(eyePosition, rayEnd);
    }
}
