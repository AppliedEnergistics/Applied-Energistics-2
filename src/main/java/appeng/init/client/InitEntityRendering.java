package appeng.init.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraftforge.fml.client.registry.RenderingRegistry;

import appeng.core.api.definitions.ApiEntities;
import appeng.entity.TinyTNTPrimedRenderer;

/**
 * Registers custom renderers for our {@link ApiEntities}.
 */
public final class InitEntityRendering {

    private InitEntityRendering() {

    }

    public static void init() {

        RenderingRegistry.registerEntityRenderingHandler(ApiEntities.TINY_TNT_PRIMED, TinyTNTPrimedRenderer::new);
        RenderingRegistry.registerEntityRenderingHandler(ApiEntities.SINGULARITY,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ApiEntities.GROWING_CRYSTAL,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
        RenderingRegistry.registerEntityRenderingHandler(ApiEntities.CHARGED_QUARTZ,
                m -> new ItemRenderer(m, Minecraft.getInstance().getItemRenderer()));
    }

}
