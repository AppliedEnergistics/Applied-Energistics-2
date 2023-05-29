package appeng.api.ids;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;

import appeng.core.AppEng;

/**
 * IDs of the AE2 creative tabs.
 */
public final class AECreativeTabIds {
    private AECreativeTabIds() {
    }

    public static final ResourceKey<CreativeModeTab> MAIN = create("main");

    public static final ResourceKey<CreativeModeTab> FACADES = create("facades");

    private static ResourceKey<CreativeModeTab> create(String path) {
        return ResourceKey.create(Registries.CREATIVE_MODE_TAB, AppEng.makeId(path));
    }
}
