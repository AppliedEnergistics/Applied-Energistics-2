package appeng.client.guidebook;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.core.AppEng;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public final class GuidebookManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(GuidebookManager.class);

    public static final GuidebookManager INSTANCE = new GuidebookManager();
    private Map<ResourceLocation, ParsedGuidePage> pages;

    private GuidebookManager() {
        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(new ReloadListener());
    }

    public static void init() {
        // Guaranteed init order
    }

    @Nullable
    public ParsedGuidePage getPage(ResourceLocation id) {
        if (pages == null) {
            LOGGER.warn("Can't get page {}. Pages not loaded yet.", id);
            return null;
        }

        return pages.get(id);
    }

    class ReloadListener extends SimplePreparableReloadListener<Map<ResourceLocation, ParsedGuidePage>> implements IdentifiableResourceReloadListener {
        @Override
        public ResourceLocation getFabricId() {
            return AppEng.makeId("guidebook");
        }

        @Override
        protected Map<ResourceLocation, ParsedGuidePage> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
            profiler.startTick();
            Map<ResourceLocation, ParsedGuidePage> pages = new HashMap<>();

            var resources = resourceManager.listResources("ae2guide", location -> location.getPath().endsWith(".md"));

            for (var entry : resources.entrySet()) {
                var pageId = new ResourceLocation(
                        entry.getKey().getNamespace(),
                        entry.getKey().getPath().substring("ae2guide/".length())
                );

                String sourcePackId = entry.getValue().sourcePackId();
                try {
                    var page = PageCompiler.parse(sourcePackId, pageId, entry.getValue().open());
                    pages.put(pageId, page);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            profiler.endTick();
            return pages;
        }

        @Override
        protected void apply(Map<ResourceLocation, ParsedGuidePage> pages, ResourceManager resourceManager, ProfilerFiller profiler) {
            profiler.startTick();
            GuidebookManager.this.pages = pages;
            profiler.endTick();
        }

        @Override
        public String getName() {
            return "AE2 Guidebook";
        }
    }

}

