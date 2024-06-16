package appeng.client.guidebook.hotkey;

import java.util.List;

import com.google.common.base.Strings;
import com.mojang.blaze3d.platform.InputConstants;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import appeng.client.guidebook.GuidebookText;
import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.indices.ItemIndex;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.core.AEConfig;
import appeng.core.AppEngClient;

/**
 * Adds a "Hold X to show guide" tooltip
 */
public final class OpenGuideHotkey {
    private static final KeyMapping OPEN_GUIDE_MAPPING = new KeyMapping(
            "key.ae2.guide", KeyConflictContext.GUI, InputConstants.Type.KEYSYM, InputConstants.KEY_G,
            "key.ae2.category");

    private static final Logger LOG = LoggerFactory.getLogger(OpenGuideHotkey.class);

    private static final int TICKS_TO_OPEN = 10;

    private static boolean newTick = true;

    // The last itemstack the tooltip was being shown for
    private static ItemStack lastStack;
    @Nullable
    private static PageAnchor guidebookPage;
    // Full ticks since the button was held (reduces slowly when not held)
    private static int ticksKeyHeld;
    // Is the key to open currently held
    private static boolean holding;

    private OpenGuideHotkey() {
    }

    public static void init() {
        if (AEConfig.instance().isGuideHotkeyEnabled()) {
            NeoForge.EVENT_BUS.addListener(
                    (ItemTooltipEvent evt) -> handleTooltip(evt.getItemStack(), evt.getFlags(), evt.getToolTip()));
            NeoForge.EVENT_BUS.addListener((ClientTickEvent.Post evt) -> newTick = true);
        } else {
            LOG.info("AE2 guide hotkey is disabled via config.");
        }
    }

    private static void handleTooltip(ItemStack itemStack, TooltipFlag tooltipFlag, List<Component> lines) {
        // Player didn't bind the key
        if (!isKeyBound()) {
            holding = false;
            ticksKeyHeld = 0;
            return;
        }

        // This should only update once per client-tick
        if (newTick) {
            newTick = false;
            update(itemStack);
        }

        if (guidebookPage == null) {
            return;
        }

        // Don't do anything if we're already on the target page
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.screen instanceof GuideScreen guideScreen
                && guideScreen.getCurrentPageId().equals(guidebookPage.pageId())) {
            return;
        }

        // Compute the progress value between [0,1]
        float progress = ticksKeyHeld;
        if (holding) {
            progress += minecraft.getTimer().getRealtimeDeltaTicks();
        } else {
            progress -= minecraft.getTimer().getRealtimeDeltaTicks();
        }
        progress /= (float) TICKS_TO_OPEN;
        var component = makeProgressBar(Mth.clamp(progress, 0, 1));
        // It may happen that we're the only line
        if (lines.isEmpty()) {
            lines.add(component);
        } else {
            lines.add(1, component);
        }
    }

    private static Component makeProgressBar(float progress) {
        var minecraft = Minecraft.getInstance();

        var holdW = GuidebookText.HoldToShow
                .text(getHotkey().getTranslatedKeyMessage().copy().withStyle(ChatFormatting.GRAY))
                .withStyle(ChatFormatting.DARK_GRAY);

        var fontRenderer = minecraft.font;
        var charWidth = fontRenderer.width("|");
        var tipWidth = fontRenderer.width(holdW);

        var total = tipWidth / charWidth;
        var current = (int) (progress * total);

        if (progress > 0) {
            var result = Component.literal(Strings.repeat("|", current)).withStyle(ChatFormatting.GRAY);
            if (progress < 1)
                result = result.append(
                        Component.literal(Strings.repeat("|", total - current)).withStyle(ChatFormatting.DARK_GRAY));
            return result;
        }

        return holdW;
    }

    private static void update(ItemStack itemStack) {
        if (itemStack != lastStack) {
            lastStack = itemStack;
            guidebookPage = null;
            ticksKeyHeld = 0;

            var itemId = itemStack.getItemHolder()
                    .unwrapKey()
                    .map(ResourceKey::location)
                    .orElse(null);
            if (itemId == null) {
                return;
            }

            var itemIndex = AppEngClient.instance().getGuide().getIndex(ItemIndex.class);
            guidebookPage = itemIndex.get(itemId);
        }

        // Bump the ticks the key was held
        holding = isKeyHeld();
        if (holding) {
            if (ticksKeyHeld < TICKS_TO_OPEN && ++ticksKeyHeld == TICKS_TO_OPEN) {
                if (guidebookPage != null) {
                    if (Minecraft.getInstance().screen instanceof GuideScreen guideScreen) {
                        guideScreen.navigateTo(guidebookPage);
                    } else {
                        AppEngClient.instance().openGuideAtAnchor(guidebookPage);
                    }
                    // Reset the ticks held immediately to avoid reopening another page if
                    // our cursors lands on an item
                    ticksKeyHeld = 0;
                    holding = false;
                }
            } else if (ticksKeyHeld > TICKS_TO_OPEN) {
                ticksKeyHeld = TICKS_TO_OPEN;
            }
        } else {
            ticksKeyHeld = Math.max(0, ticksKeyHeld - 2);
        }
    }

    /**
     * This circumvents any current UI key handling.
     */
    private static boolean isKeyHeld() {
        int keyCode = getHotkey().getKey().getValue();
        var window = Minecraft.getInstance().getWindow().getWindow();

        return InputConstants.isKeyDown(window, keyCode);
    }

    private static boolean isKeyBound() {
        return !OPEN_GUIDE_MAPPING.isUnbound();
    }

    public static KeyMapping getHotkey() {
        return OPEN_GUIDE_MAPPING;
    }
}
