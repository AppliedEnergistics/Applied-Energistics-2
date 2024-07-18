package appeng.client.guidebook.document.flow;

import java.net.URI;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.sounds.AppEngSounds;

public class LytFlowLink extends LytTooltipSpan {
    private static final Logger LOG = LoggerFactory.getLogger(LytFlowLink.class);

    @Nullable
    private Consumer<GuideScreen> clickCallback;

    @Nullable
    private SoundEvent clickSound = AppEngSounds.GUIDE_CLICK_EVENT;

    public LytFlowLink() {
        modifyStyle(style -> style.color(SymbolicColor.LINK));
        modifyHoverStyle(style -> style.underlined(true));
    }

    public void setClickCallback(@Nullable Consumer<GuideScreen> clickCallback) {
        this.clickCallback = clickCallback;
    }

    @Override
    public boolean mouseClicked(GuideScreen screen, int x, int y, int button) {
        if (button == 0 && clickCallback != null) {
            if (clickSound != null) {
                var handler = Minecraft.getInstance().getSoundManager();
                handler.play(SimpleSoundInstance.forUI(clickSound, 1.0F));
            }
            clickCallback.accept(screen);
            return true;
        }
        return false;
    }

    public @Nullable SoundEvent getClickSound() {
        return clickSound;
    }

    public void setClickSound(@Nullable SoundEvent clickSound) {
        this.clickSound = clickSound;
    }

    /**
     * Configures this link to open the given external URL on click.
     */
    public void setExternalUrl(URI uri) {
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("External URLs must be absolute: " + uri);
        }

        setClickCallback(screen -> {
            var mc = Minecraft.getInstance();
            mc.setScreen(new ConfirmLinkScreen(yes -> {
                if (yes) {
                    Util.getPlatform().openUri(uri);
                }

                mc.setScreen(screen);
            }, uri.toString(), true));
        });
    }

    /**
     * Configures this link to open the given page on click.
     */
    public void setPageLink(PageAnchor anchor) {
        setClickCallback(screen -> {
            screen.navigateTo(anchor);
        });
    }
}
