package appeng.client.integrations.iris;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.irisshaders.iris.pipeline.IrisPipelines;
import net.irisshaders.iris.pipeline.programs.ShaderKey;
import net.neoforged.fml.ModList;

import appeng.client.render.AERenderPipelines;

public final class IrisCompat {
    private static final Logger LOG = LoggerFactory.getLogger(IrisCompat.class);
    private static final boolean IRIS_LOADED = ModList.get().isLoaded("iris");

    private IrisCompat() {
    }

    public static void init() {
        if (!IRIS_LOADED) {
            return;
        }

        try {
            // Assign pipelines to iris so shaders can render them, iris also does this for all vanilla pipelines
            Impl.assignPipelines();
        } catch (Exception | LinkageError e) {
            LOG.error("Failed to apply Iris compatibility", e);
        }
    }

    // Kept in a nested class so the Iris classes are only loaded when Iris is present.
    private static final class Impl {
        static void assignPipelines() {
            IrisPipelines.assignPipeline(AERenderPipelines.STORAGE_CELL_LEDS, ShaderKey.LIGHTNING);
        }
    }
}
