package appeng.client.gui.me.common;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

import appeng.api.client.AEKeyRendering;
import appeng.api.stacks.AEKey;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.packets.CraftingJobStatusPacket;
import appeng.items.tools.powered.WirelessTerminalItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * Tracks pending crafting jobs started by this player.
 */
@Environment(EnvType.CLIENT)
public final class PendingCraftingJobs {
    private static final Map<UUID, PendingJob> jobs = new HashMap<>();

    private PendingCraftingJobs() {
    }

    public static boolean hasPendingJob(AEKey what) {
        return jobs.entrySet().stream().anyMatch(s -> s.getValue().what.equals(what));
    }

    public static void jobStatus(UUID id,
            AEKey what,
            long requestedAmount,
            long remainingAmount,
            CraftingJobStatusPacket.Status status) {

        AELog.debug("Crafting job " + id + " for " + requestedAmount
                + "x" + AEKeyRendering.getDisplayName(what).getString() + ". State=" + status);

        var existing = jobs.get(id);
        switch (status) {
            case STARTED -> {
                if (existing == null) {
                    jobs.put(id, new PendingJob(id, what, requestedAmount, remainingAmount));
                }
            }
            case CANCELLED -> jobs.remove(id);
            case FINISHED -> {
                jobs.remove(id);
                // Only toast if no terminal is open (i.e. REI/JEI or no screen at all)
                // and a wireless terminal is in the player inv
                var minecraft = Minecraft.getInstance();
                if (AEConfig.instance().isNotifyForFinishedCraftingJobs()
                        && !(minecraft.screen instanceof MEStorageScreen<?>)
                        && minecraft.player != null && hasNotificationEnablingItem(minecraft.player)) {
                    minecraft.getToasts().addToast(new FinishedJobToast(what, requestedAmount));
                }
            }
        }
    }

    private static boolean hasNotificationEnablingItem(LocalPlayer player) {
        var inventories = WirelessTerminalEvent.getInventories(player);
        for (ItemStack stack : inventories) {
            if (!stack.isEmpty()
                    && stack.getItem() instanceof WirelessTerminalItem wirelessTerminal
                    // Should have some power
                    && wirelessTerminal.getAECurrentPower(stack) > 0
                    // Should be linked (we don't know if it's linked to the grid for which we get notifications)
                    && wirelessTerminal.getGridKey(stack).isPresent()) {
                return true;
            }
        }
        return false;
    }

    record PendingJob(UUID jobId, AEKey what, long requestedAmount, long remainingAmount) {
    }

    public static class WirelessTerminalEvent {
        private static final List<handler> handlers = new ArrayList<>();
        public static void registerHandler(handler handler) {
            handlers.add(handler);
        }

        public static List<ItemStack> getInventories(Player player) {
            var event = new WirelessTerminalEvent(player);
            for (var handler : handlers) {
                handler.handle(event);
            }
            return event.getItems();
        }

        @FunctionalInterface
        interface handler {
            void handle(WirelessTerminalEvent event);
        }

        private final Player player;
        private final List<ItemStack> items = new ArrayList<>();

        public WirelessTerminalEvent(Player player) {
            this.player = player;
        }

        public Player getPlayer() {
            return player;
        }

        public List<ItemStack> getItems() {
            return items;
        }
    }
}
