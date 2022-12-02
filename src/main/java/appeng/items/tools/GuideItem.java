package appeng.items.tools;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.core.AppEng;
import appeng.items.AEBaseItem;

/**
 * Shows the guidebook when used.
 */
public class GuideItem extends AEBaseItem {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuideItem.class);

    public GuideItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            openGuide();
        }

        return new InteractionResultHolder<>(InteractionResult.FAIL, stack);
    }

    private static void openGuide() {
        try {
            var screen = GuideScreen.openAtPreviousPage(PageAnchor.page(AppEng.makeId("index.md")));
            Minecraft.getInstance().setScreen(screen);
        } catch (Exception e) {
            LOGGER.error("Failed to open guide.", e);
        }
    }
}
