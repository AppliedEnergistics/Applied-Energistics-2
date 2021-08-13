package appeng.api.features;

import net.minecraft.Util;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import appeng.core.Api;
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
            if (player.getCommandSenderWorld().isClientSide()) {
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

            final ILocatable securityStation = Api.instance().registries().locatable().getLocatableBy(key.getAsLong());
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
