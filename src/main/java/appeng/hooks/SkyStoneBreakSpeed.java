package appeng.hooks;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.event.entity.player.PlayerEvent;

import appeng.core.definitions.AEBlocks;

/**
 * This hook is intended to essentially make sky stone blocks found in meteorites minable with iron tools while
 * multiplying their destroy time by 10. To accomplish this, the blocks are created with destroy time 50, and their
 * destroy time is divided by 10 if a tool _better_ than iron is used.
 */
public final class SkyStoneBreakSpeed {
    public static final int SPEEDUP_FACTOR = 10;

    private SkyStoneBreakSpeed() {
    }

    public static void handleBreakFaster(PlayerEvent.BreakSpeed event) {
        var blockState = event.getState();
        if (blockState.getBlock() == AEBlocks.SKY_STONE_BLOCK.block()) {
            var tool = event.getPlayer().getItemBySlot(EquipmentSlot.MAINHAND);
            if (tool.getDestroySpeed(blockState) > Tiers.IRON.getSpeed()) {
                event.setNewSpeed(event.getNewSpeed() * SPEEDUP_FACTOR);
            }
        }
    }
}
