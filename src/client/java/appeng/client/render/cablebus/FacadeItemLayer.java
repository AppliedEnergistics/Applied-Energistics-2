package appeng.client.render.cablebus;

import java.util.List;

import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.resources.model.geometry.BakedQuad;

public record FacadeItemLayer(RenderType renderType, List<BakedQuad> quads) {
}
