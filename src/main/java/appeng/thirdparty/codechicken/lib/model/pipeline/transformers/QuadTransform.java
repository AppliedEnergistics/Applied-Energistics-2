package appeng.thirdparty.codechicken.lib.model.pipeline.transformers;

import net.neoforged.neoforge.client.model.quad.MutableQuad;

public interface QuadTransform {
    boolean transform(MutableQuad quad);
}
