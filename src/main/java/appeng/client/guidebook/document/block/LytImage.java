package appeng.client.guidebook.document.block;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.document.LytRect;
import appeng.client.guidebook.document.interaction.GuideTooltip;
import appeng.client.guidebook.document.interaction.InteractiveElement;
import appeng.client.guidebook.document.interaction.TextTooltip;
import appeng.client.guidebook.layout.LayoutContext;
import appeng.client.guidebook.render.GuidePageTexture;
import appeng.client.guidebook.render.RenderContext;

public class LytImage extends LytBlock implements InteractiveElement {

    private ResourceLocation imageId;
    private GuidePageTexture texture = GuidePageTexture.missing();
    private String title;
    private String alt;

    public ResourceLocation getImageId() {
        return imageId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAlt() {
        return alt;
    }

    public void setAlt(String alt) {
        this.alt = alt;
    }

    public void setImage(ResourceLocation id, byte @Nullable [] imageData) {
        this.imageId = id;
        if (imageData != null) {
            this.texture = GuidePageTexture.load(id, imageData);
        } else {
            this.texture = GuidePageTexture.missing();
        }
    }

    @Override
    protected LytRect computeLayout(LayoutContext context, int x, int y, int availableWidth) {
        if (texture == null) {
            return new LytRect(x, y, 32, 32);
        }

        var size = texture.getSize();
        var width = size.width();
        var height = size.height();

        var scale = Minecraft.getInstance().getWindow().getGuiScale();
        width /= scale;
        height /= scale;

        if (width > availableWidth) {
            var f = availableWidth / (float) width;
            width *= f;
            height *= f;
        }

        return new LytRect(x, y, width, height);
    }

    @Override
    public void renderBatch(RenderContext context, MultiBufferSource buffers) {
    }

    @Override
    public void render(RenderContext context) {
        if (texture == null) {
            var texture = MissingTextureAtlasSprite.getTexture();
            context.fillTexturedRect(getBounds(), texture);
        } else {
            context.fillTexturedRect(getBounds(), texture);
        }
    }

    @Override
    public Optional<GuideTooltip> getTooltip() {
        if (title != null) {
            return Optional.of(new TextTooltip(Component.literal(title)));
        }
        return Optional.empty();
    }
}
