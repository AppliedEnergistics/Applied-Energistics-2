package appeng.siteexport.mdastpostprocess;

import java.nio.file.Path;

import appeng.client.guidebook.compiler.IdUtils;
import appeng.client.guidebook.compiler.tags.FloatingImageCompiler;
import appeng.libs.mdast.MdAstVisitor;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstImage;
import appeng.libs.mdast.model.MdAstNode;
import appeng.siteexport.ResourceExporter;

/**
 * Searches for static images and exports them, while rewriting the referenced path with relation to the base URL.
 */
public class ImageExportVisitor implements MdAstVisitor {
    private final ResourceExporter exporter;

    public ImageExportVisitor(ResourceExporter exporter) {
        this.exporter = exporter;
    }

    @Override
    public Result beforeNode(MdAstNode node) {
        if (node instanceof MdAstImage image) {
            image.url = rewriteAsset(image.url);
        } else if (node instanceof MdxJsxElementFields fields && FloatingImageCompiler.TAG_NAME.equals(fields.name())) {
            var src = fields.getAttributeString("src", null);
            if (src != null) {
                var newSrc = rewriteAsset(src);
                fields.setAttribute("src", newSrc);
            }
        }

        return Result.CONTINUE;
    }

    private String rewriteAsset(String url) {
        // rewrite the URL to be relative to the asset base, which is our output folder
        var assetId = IdUtils.resolveLink(url, exporter.getCurrentPageId());
        Path assetPath = exporter.copyResource(assetId);
        return exporter.getPathRelativeFromOutputFolder(assetPath);
    }

}
