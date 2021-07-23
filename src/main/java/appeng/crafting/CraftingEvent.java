package appeng.crafting;

import appeng.api.networking.crafting.ICraftingPatternDetails;
import appeng.util.Platform;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class CraftingEvent {

    public static void fireCraftingEvent(Player player,
                                         ItemStack craftedItem,
                                         Container container) {
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(player, craftedItem, container));
    }

    public static void fireAutoCraftingEvent(Level level,
                                             ICraftingPatternDetails pattern,
                                             CraftingContainer container) {
        var craftedItem = pattern.getOutput(container, level);
        fireAutoCraftingEvent(level, pattern, craftedItem, container);
    }

    public static void fireAutoCraftingEvent(Level level,
                                             // NOTE: We want to be able to include the recipe in the event later
                                             @SuppressWarnings("unused") ICraftingPatternDetails pattern,
                                             ItemStack craftedItem,
                                             CraftingContainer container) {
        var serverLevel = (ServerLevel) level;
        var fakePlayer = Platform.getPlayer(serverLevel);
        MinecraftForge.EVENT_BUS.post(new PlayerEvent.ItemCraftedEvent(fakePlayer, craftedItem, container));
    }

}
