package appeng.sounds;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.IForgeRegistry;

import appeng.core.AppEng;

public final class AppEngSounds {
    public static final ResourceLocation GUIDE_CLICK_ID = AppEng.makeId("guide.click");
    public static SoundEvent GUIDE_CLICK_EVENT = SoundEvent.createVariableRangeEvent(GUIDE_CLICK_ID);

    public static void register(IForgeRegistry<SoundEvent> registry) {
        registry.register(GUIDE_CLICK_ID, GUIDE_CLICK_EVENT);
    }
}
