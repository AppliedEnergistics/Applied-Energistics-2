package appeng.client.guidebook.render;

import appeng.client.guidebook.document.LytSize;
import appeng.core.AppEng;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.stb.STBImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * A texture that is used in the context of a single guide page and is automatically cleared
 * from texture memory when the guide page it was last used on is closed.
 */
public class GuidePageTexture {

    public static GuidePageTexture missing() {
        return new GuidePageTexture(AppEng.makeId("missing"), null);
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(GuidePageTexture.class);

    // Textures in use by the current page
    private static final Map<GuidePageTexture, DynamicTexture> usedTextures = new IdentityHashMap<>();

    private final ResourceLocation id;

    private final ByteBuffer imageContent;

    private final LytSize size;

    private GuidePageTexture(ResourceLocation id, byte @Nullable [] imageContent) {
        this.id = Objects.requireNonNull(id, "id");
        if (imageContent == null) {
            this.imageContent = null;
            this.size = new LytSize(32, 32);
        } else {
            this.imageContent = ByteBuffer.allocateDirect(imageContent.length);
            this.imageContent.put(imageContent).flip();

            var xOut = new int[1];
            var yOut = new int[1];
            var compOut = new int[1];
            if (!STBImage.stbi_info_from_memory(this.imageContent, xOut, yOut, compOut)) {
                throw new IllegalArgumentException("Couldn't determine size of image " + id + ": " + STBImage.stbi_failure_reason());
            }

            // Try to determine the size
            this.size = new LytSize(xOut[0], yOut[0]);
        }
    }

    public ResourceLocation getId() {
        return id;
    }

    public LytSize getSize() {
        return size;
    }

    public static GuidePageTexture load(ResourceLocation id, byte[] imageContent) {
        try {
            return new GuidePageTexture(id, imageContent);
        } catch (Exception e) {
            LOGGER.error("Failed to get image {}: {}", id, e.toString());
            return missing();
        }
    }

    public AbstractTexture use() {
        return usedTextures.computeIfAbsent(this, guidePageTexture -> {
            if (guidePageTexture.imageContent == null) {
                return MissingTextureAtlasSprite.getTexture();
            }

            try {
                var nativeImage = NativeImage.read(guidePageTexture.imageContent);
                return new DynamicTexture(nativeImage);
            } catch (IOException e) {
                LOGGER.error("Failed to read image {}: {}", guidePageTexture.id, e.toString());
                return MissingTextureAtlasSprite.getTexture();
            }
        });
    }

    public static void releaseUsedTextures() {
        for (DynamicTexture texture : usedTextures.values()) {
            if (texture != MissingTextureAtlasSprite.getTexture()) {
                texture.close();
            }
        }
        usedTextures.clear();
    }
}
