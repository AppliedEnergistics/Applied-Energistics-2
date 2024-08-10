package appeng.hooks;

import appeng.core.AEConfig;
import appeng.core.AppEng;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public final class DarkModeHook {
    private static final String DARKMODE_SUFFIX = "_darkmode";

    private DarkModeHook() {
    }

    public static boolean isDarkModeEnabledNamespace(String id) {
        return AppEng.MOD_ID.equals(id);
    }


    public static TextureAtlasSprite getReplacedSprite(ResourceLocation id,
                                                       Map<ResourceLocation, TextureAtlasSprite> textures) {
        if (DarkModeHook.isDarkModeEnabledNamespace(id.getNamespace())
            && AEConfig.instance().isDarkModeEnabled()
            && !id.getPath().endsWith(DARKMODE_SUFFIX)) {
            var alternate = ResourceLocation.fromNamespaceAndPath(id.getNamespace(), id.getPath() + DARKMODE_SUFFIX);
            return textures.get(alternate);
        }

        return null;
    }
}
