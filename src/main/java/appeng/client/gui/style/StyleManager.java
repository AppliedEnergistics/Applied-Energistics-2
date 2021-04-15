package appeng.client.gui.style;

import appeng.core.AppEng;
import com.google.common.base.Preconditions;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraft.resources.IResource;
import net.minecraft.resources.IResourceManager;
import net.minecraftforge.resource.IResourceType;
import net.minecraftforge.resource.ISelectiveResourceReloadListener;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

/**
 * Manages AE2 GUI styles found in resource packs.
 */
public final class StyleManager {

    private static final Map<String, ScreenStyle> styleCache = new HashMap<>();

    private static IResourceManager resourceManager;

    private static String getBasePath(String path) {
        int lastSep = path.lastIndexOf('/');
        if (lastSep == -1) {
            return "";
        } else {
            return path.substring(0, lastSep + 1);
        }
    }

    public static ScreenStyle loadStyleDoc(String path) throws IOException {
        ScreenStyle style = loadStyleDocInternal(path);
        // We only require the final style-document to be fully valid,
        // includes are allowed to be partially valid.
        style.validate();
        return style;
    }

    private static ScreenStyle loadStyleDocInternal(String path) throws IOException {
        Preconditions.checkArgument(path.startsWith("/"), "Path needs to start with slash");

        if (resourceManager == null) {
            throw new IllegalStateException("ResourceManager was not set. Was initialize called?");
        }

        ScreenStyle style = styleCache.get(path);
        if (style != null) {
            return style;
        }

        String basePath = getBasePath(path);

        try (IResource resource = resourceManager.getResource(AppEng.makeId(path.substring(1)))) {
            ScreenStyle baseStyle = null;
            style = ScreenStyle.GSON.fromJson(new InputStreamReader(resource.getInputStream()), ScreenStyle.class);

            for (String includePath : style.getIncludes()) {
                // The path should be relative to the currently loading file
                ScreenStyle includedStyle = loadStyleDocInternal(basePath + includePath);
                if (baseStyle == null) {
                    baseStyle = includedStyle;
                } else {
                    baseStyle = baseStyle.merge(includedStyle);
                }
            }

            if (baseStyle != null) {
                style = baseStyle.merge(style);
            }
        }

        styleCache.put(path, style);
        return style;
    }

    public static void initialize(Minecraft minecraft) {
        ((IReloadableResourceManager) minecraft.getResourceManager()).addReloadListener(new ReloadListener());
    }

    private static class ReloadListener implements ISelectiveResourceReloadListener {
        @Override
        public void onResourceManagerReload(IResourceManager resourceManager, Predicate<IResourceType> resourcePredicate) {
            StyleManager.resourceManager = resourceManager;
            StyleManager.styleCache.clear();
        }
    }

}
