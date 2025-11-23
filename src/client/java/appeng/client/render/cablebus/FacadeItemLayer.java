package appeng.client.render.cablebus;

import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.rendertype.RenderType;

public record FacadeItemLayer(RenderType renderType, List<BakedQuad> quads) {
}
