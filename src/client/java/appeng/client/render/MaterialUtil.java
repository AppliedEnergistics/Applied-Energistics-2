package appeng.client.render;

import net.minecraft.client.resources.model.geometry.BakedQuad;
import net.minecraft.client.resources.model.sprite.Material;

public final class MaterialUtil {
    private MaterialUtil() {
    }

    public static int getMaterialFlags(Material.Baked firstMaterial, Material.Baked... moreMaterials) {
        var flags = getMaterialFlags(firstMaterial);
        for (var material : moreMaterials) {
            flags |= getMaterialFlags(material);
        }
        return flags;
    }

    public static int getMaterialFlags(Material.Baked material) {
        var flags = 0;
        if (material.forceTranslucent() || material.sprite().contents().transparency().hasTranslucent()) {
            flags |= BakedQuad.FLAG_TRANSLUCENT;
        }
        if (material.sprite().contents().isAnimated()) {
            flags |= BakedQuad.FLAG_ANIMATED;
        }
        return flags;
    }
}
