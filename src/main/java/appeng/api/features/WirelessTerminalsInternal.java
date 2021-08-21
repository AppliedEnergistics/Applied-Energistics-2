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

package appeng.api.features;

import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.core.localization.PlayerMessages;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.me.items.WirelessTermMenu;

public final class WirelessTerminalsInternal {

    private WirelessTerminalsInternal() {
    }

    public static void installOpener() {
        WirelessTerminals.opener = new Opener();
    }

    private static class Opener implements WirelessTerminals.Opener {
        @Override
        public void open(ItemStack item, Player player, int inventorySlot) {
            if (checkPreconditions(item, player)) {
                MenuOpener.open(WirelessTermMenu.TYPE, player, MenuLocator.forInventorySlot(inventorySlot));
            }
        }

        @Override
        public void open(ItemStack item, Player player, InteractionHand hand) {
            if (checkPreconditions(item, player)) {
                MenuOpener.open(WirelessTermMenu.TYPE, player, MenuLocator.forHand(player, hand));
            }
        }

        private boolean checkPreconditions(ItemStack item, Player player) {
            var level = player.getCommandSenderWorld();
            if (level.isClientSide()) {
                return false;
            }

            var handler = WirelessTerminals.get(item.getItem());

            if (handler == null) {
                player.sendMessage(PlayerMessages.DeviceNotWirelessTerminal.get(), Util.NIL_UUID);
                return false;
            }

            var key = handler.getGridKey(item);
            if (key.isEmpty()) {
                player.sendMessage(PlayerMessages.DeviceNotLinked.get(), Util.NIL_UUID);
                return false;
            }

            var securityStation = Locatables.securityStations().get(level, key.getAsLong());
            if (securityStation == null) {
                player.sendMessage(PlayerMessages.StationCanNotBeLocated.get(), Util.NIL_UUID);
                return false;
            }

            if (!handler.hasPower(player, 0.5, item)) {
                player.sendMessage(PlayerMessages.DeviceNotPowered.get(), Util.NIL_UUID);
                return false;
            }
            return true;
        }
    }

}
