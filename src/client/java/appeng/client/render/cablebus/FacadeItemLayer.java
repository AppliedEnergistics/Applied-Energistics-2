package appeng.client.render.cablebus;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;

import java.util.List;

public record FacadeItemLayer(RenderType renderType, List<BakedQuad> quads) {
}
