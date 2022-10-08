package appeng.client.gui.me.common;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import appeng.api.client.AEStackRendering;
import appeng.api.stacks.AEKey;
import appeng.core.AEConfig;
import appeng.core.AELog;
import appeng.core.sync.packets.CraftingJobStatusPacket;
import appeng.items.tools.powered.WirelessTerminalItem;

/**
 * Tracks pending crafting jobs started by this player.
 */
@OnlyIn(Dist.CLIENT)
public final class PendingCraftingJobs {
    private static final Map<String, PendingJob> jobs = new HashMap<>();

    private PendingCraftingJobs() {
    }

    public static boolean hasPendingJob(AEKey what) {
        return jobs.entrySet().stream().anyMatch(s -> s.getValue().what.equals(what));
    }

    public static void jobStatus(String id,
            AEKey what,
            long requestedAmount,
            long remainingAmount,
            CraftingJobStatusPacket.Status status) {

        AELog.debug("Crafting job " + id + " for " + requestedAmount
                + "x" + AEStackRendering.getDisplayName(what).getString() + ". State=" + status);

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
        var playerInv = player.getInventory();
        for (int i = 0; i < playerInv.getContainerSize(); i++) {
            var stack = playerInv.getItem(i);
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

    record PendingJob(String jobId, AEKey what, long requestedAmount, long remainingAmount) {
    }
}
