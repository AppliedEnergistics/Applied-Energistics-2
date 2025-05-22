package appeng.hooks;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import appeng.api.parts.IPartHost;
import appeng.core.sync.network.NetworkHandler;
import appeng.core.sync.packets.PartLeftClickPacket;
import appeng.util.InteractionUtil;

/**
 * Handles the client->server interaction when a player left-clicks on an {@link appeng.api.parts.IPart} attached to a
 * {@link appeng.api.parts.IPartHost}, and that part implements {@link appeng.api.parts.IPart#onClicked(Player, Vec3)}
 * or {@link appeng.api.parts.IPart#onShiftClicked(Player, Vec3)}.
 */
@OnlyIn(Dist.CLIENT)
public final class BlockAttackHook {
    private BlockAttackHook() {
    }

    public static void install() {
        MinecraftForge.EVENT_BUS.addListener(BlockAttackHook::onBlockAttackedOnClientEvent);
    }

    /**
     * We intercept when the player attacks a cable bus and send an interaction handling packet instead.
     */
    private static void onBlockAttackedOnClientEvent(PlayerInteractEvent.LeftClickBlock event) {

        var level = event.getLevel();

        // Do not process this event on the server since we're handling the server-side ourselves
        if (!level.isClientSide()) {
            return;
        }

        var result = onBlockAttackedOnClient(event.getEntity(), level);
        if (result != InteractionResult.PASS) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    private static InteractionResult onBlockAttackedOnClient(Player player, Level level) {
        // This shouldn't happen as the attack block logic should only be called if the player is pointing at a block
        // to begin with.
        if (!(Minecraft.getInstance().hitResult instanceof BlockHitResult hitResult)) {
            return InteractionResult.PASS;
        }

        if (BlockAttackHook.onBlockAttackedOnClient(player, level, hitResult)) {
            // Prevent this from being called again immediately on the next tick
            // Vanilla also uses a 5 tick delay (-> 250ms)
            Minecraft.getInstance().gameMode.destroyDelay = 5;

            // Consume the action, but do not send a packet to the server,
            // since we're handling that ourselves.
            return InteractionResult.FAIL;
        }
        return InteractionResult.PASS;
    }

    private static boolean onBlockAttackedOnClient(Player player, Level level, BlockHitResult hitResult) {
        var pos = hitResult.getBlockPos();

        // Compute the hit location within the block's space. hitResult should never be null at this point,
        // since the event itself gets the blockPos from it.
        var localPos = hitResult.getLocation().subtract(
                pos.getX(),
                pos.getY(),
                pos.getZ());

        var blockEntity = level.getBlockEntity(pos);

        if (!(blockEntity instanceof IPartHost partHost)) {
            return false;
        }

        var p = partHost.selectPartLocal(localPos);
        if (p.part != null) {
            boolean alternateUseMode = InteractionUtil.isInAlternateUseMode(player);
            boolean activated;
            if (alternateUseMode) {
                activated = p.part.onShiftClicked(player, localPos);
            } else {
                activated = p.part.onClicked(player, localPos);
            }

            if (activated) {
                NetworkHandler.instance().sendToServer(new PartLeftClickPacket(hitResult, alternateUseMode));
                // Do not perform the default action (of spawning break particles and breaking the block)
                return true;
            }
        }

        return false;
    }
}
