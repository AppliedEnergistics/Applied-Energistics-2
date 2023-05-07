package appeng.client.guidebook.document.flow;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvent;

import appeng.client.guidebook.render.SymbolicColor;
import appeng.client.guidebook.screen.GuideScreen;
import appeng.sounds.AppEngSounds;

public class LytFlowLink extends LytTooltipSpan {
    @Nullable
    private Consumer<GuideScreen> clickCallback;

    @Nullable
    private SoundEvent clickSound = AppEngSounds.GUIDE_CLICK_EVENT;

    public LytFlowLink() {
        modifyStyle(style -> style.color(SymbolicColor.LINK.ref()));
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
}
