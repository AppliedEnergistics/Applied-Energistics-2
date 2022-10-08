package appeng.client.render.cablebus;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.renderer.v1.mesh.QuadEmitter;
import net.fabricmc.fabric.api.renderer.v1.model.SpriteFinder;
import net.fabricmc.fabric.impl.client.indigo.renderer.IndigoRenderer;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.EncodingFormat;
import net.fabricmc.fabric.impl.client.indigo.renderer.mesh.MutableQuadViewImpl;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.ModelManager;

class BakedQuadBuilder extends MutableQuadViewImpl implements QuadEmitter {
    private final List<BakedQuad> output;

    private final SpriteFinder finder;

    public BakedQuadBuilder(List<BakedQuad> output) {
        this(output, Minecraft.getInstance() != null ? Minecraft.getInstance().getModelManager() : null);
    }

    public BakedQuadBuilder(List<BakedQuad> output, @Nullable ModelManager modelManager) {
        this.output = output;

        data = new int[EncodingFormat.TOTAL_STRIDE];
        material(IndigoRenderer.MATERIAL_STANDARD);

        if (modelManager != null) {
            this.finder = SpriteFinder.get(modelManager.getAtlas(TextureAtlas.LOCATION_BLOCKS));
        } else {
            this.finder = null;
        }
    }

    @Override
    public QuadEmitter emit() {
        var limit = material().spriteDepth();

        for (int l = 0; l < limit; l++) {
            output.add(toBakedQuad(l, finder != null ? finder.find(this, l) : null, false));
        }

        clear();
        return this;
    }
}
