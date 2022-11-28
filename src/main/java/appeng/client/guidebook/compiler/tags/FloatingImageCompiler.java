package appeng.client.guidebook.compiler.tags;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ResourceLocationException;

import appeng.client.guidebook.compiler.IdUtils;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.block.LytImage;
import appeng.client.guidebook.document.flow.InlineBlockAlignment;
import appeng.client.guidebook.document.flow.LytFlowInlineBlock;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;

public class FloatingImageCompiler extends FlowTagCompiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FloatingImageCompiler.class);

    @Override
    protected void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var src = el.getAttributeString("src", null);
        var align = el.getAttributeString("align", "left");
        var title = el.getAttributeString("title", null);

        var image = new LytImage();
        if (title != null) {
            image.setTitle(title);
        }
        try {
            var imageId = IdUtils.resolveLink(src, compiler.getId());
            var imageContent = compiler.loadAsset(imageId);
            if (imageContent == null) {
                LOGGER.error("Couldn't find image {}", src);
                image.setTitle("Missing image: " + src);
            }
            image.setImage(imageId, imageContent);
        } catch (ResourceLocationException e) {
            LOGGER.error("Invalid image id: {}", src);
            image.setTitle("Invalid image URL: " + src);
        }

        // Wrap it in a flow content inline block
        var inlineBlock = new LytFlowInlineBlock();
        inlineBlock.setBlock(image);
        switch (align) {
            case "left" -> {
                inlineBlock.setAlignment(InlineBlockAlignment.FLOAT_LEFT);
                image.setMarginRight(5);
                image.setMarginBottom(5);
            }
            case "right" -> {
                inlineBlock.setAlignment(InlineBlockAlignment.FLOAT_RIGHT);
                image.setMarginLeft(5);
                image.setMarginBottom(5);
            }
            default -> {
                parent.append(compiler.createErrorFlowContent("Invalid align. Must be left or right.", (MdAstNode) el));
                return;
            }
        }

        parent.append(inlineBlock);
    }
}
