package appeng.siteexport.mdastpostprocess;

import java.io.IOException;
import java.util.Objects;

import com.google.common.collect.Multimap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import appeng.client.guidebook.document.block.LytNode;
import appeng.client.guidebook.scene.BlockImageTagCompiler;
import appeng.client.guidebook.scene.LytGuidebookScene;
import appeng.client.guidebook.scene.SceneTagCompiler;
import appeng.client.guidebook.scene.export.SceneExporter;
import appeng.libs.mdast.MdAstVisitor;
import appeng.libs.mdast.mdx.model.MdxJsxAttribute;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import appeng.siteexport.CacheBusting;
import appeng.siteexport.ResourceExporter;

/**
 * Visits all Markdown AST nodes that have a corresponding {@link LytGuidebookScene} and exports that scene.
 */
class SceneExportVisitor implements MdAstVisitor {
    private static final int[] BLOCKIMAGE_SCALES = { 2, 4, 8 };
    private static final int GAMESCENE_PLACEHOLDER_SCALE = 2;

    private static final Logger LOG = LoggerFactory.getLogger(SceneExportVisitor.class);

    private final ResourceExporter exporter;
    private final Multimap<MdAstNode, LytNode> nodeMapping;

    private int index;

    public SceneExportVisitor(ResourceExporter exporter, Multimap<MdAstNode, LytNode> nodeMapping) {
        this.exporter = exporter;
        this.nodeMapping = nodeMapping;
    }

    @Override
    public Result beforeNode(MdAstNode node) {
        if (!(node instanceof MdxJsxElementFields elFields)) {
            return Result.CONTINUE;
        }

        var tagName = elFields.name();

        var isBlockImage = BlockImageTagCompiler.TAG_NAME.equals(tagName);
        var isGameScene = SceneTagCompiler.TAG_NAME.equals(tagName);
        if (isBlockImage || isGameScene) {
            try {
                handleScene(node, elFields, tagName, isBlockImage, isGameScene);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return Result.CONTINUE;
    }

    private void handleScene(MdAstNode node, MdxJsxElementFields elFields, String tagName, boolean isBlockImage,
            boolean isGameScene) throws IOException {
        var scenes = nodeMapping.get(node)
                .stream()
                .map(lytNode -> lytNode instanceof LytGuidebookScene lytScene ? lytScene : null)
                .filter(Objects::nonNull)
                .toList();

        if (scenes.isEmpty()) {
            LOG.warn("Found no layout scenes associated with element {} @ {}:{}", tagName,
                    exporter.getCurrentPageId(), node.position());
        } else {
            if (scenes.size() > 1) {
                LOG.warn("Found multiple layout scenes associated with element {} @ {}:{}", tagName,
                        exporter.getCurrentPageId(), node.position());
            }
            var scene = scenes.get(0);
            var exportNamePrefix = isBlockImage ? "blockimage" : "scene";
            var exportName = exportNamePrefix + (++index);
            if (isGameScene) {
                var relativePath = exportScene(scene, exportName);
                elFields.addAttribute("src", relativePath);
            }
            if (isBlockImage) {
                // Export animated scenes as full scenes instead of pre-rendered images.
                // Convert the block image to a scene for this purpose. This saves a lot of bandwidth...
                if (scene.getScene() != null && SceneExporter.isAnimated(scene.getScene())) {
                    elFields.setName("GameScene");
                    elFields.attributes().clear();
                    elFields.addAttribute("background", "transparent");

                    var relativePath = exportScene(scene, exportName);
                    elFields.addAttribute("src", relativePath);

                    addPlaceholder(elFields, scene, exportName);
                } else {
                    // Since block images are non-interactive and have no annotations, we just render them
                    // ahead of time.
                    for (int scale : BLOCKIMAGE_SCALES) {
                        var imagePath = exporter.getPageSpecificPathForWriting(exportName + "@" + scale + ".png");
                        byte[] imageContent = scene.exportAsPng(scale, true);
                        if (imageContent != null) {
                            imagePath = CacheBusting.writeAsset(imagePath, imageContent);
                            var relativeImagePath = exporter.getPathRelativeFromOutputFolder(imagePath);
                            elFields.attributes().add(new MdxJsxAttribute("src@" + scale, relativeImagePath));
                        }
                    }
                }
            } else if (isGameScene) {
                addPlaceholder(elFields, scene, exportName);
            } // Export the preferred size as width/height attributes
            var preferredSize = scene.getPreferredSize();
            if (!elFields.hasAttribute("width")) {
                elFields.addAttribute("width", preferredSize.width());
            }
            if (!elFields.hasAttribute("height")) {
                elFields.addAttribute("height", preferredSize.height());
            }
        }
    }

    private void addPlaceholder(MdxJsxElementFields elFields, LytGuidebookScene scene, String exportName)
            throws IOException {
        // For GameScenes, we create a placeholder PNG to show in place of the WebGL scene
        // while that is still loading.
        var imagePath = exporter.getPageSpecificPathForWriting(exportName + ".png");
        byte[] imageContent = scene.exportAsPng(GAMESCENE_PLACEHOLDER_SCALE, true);
        if (imageContent != null) {
            imagePath = CacheBusting.writeAsset(imagePath, imageContent);
            var relativeImagePath = exporter.getPathRelativeFromOutputFolder(imagePath);
            elFields.attributes().add(new MdxJsxAttribute("placeholder", relativeImagePath));
        }
    }

    private String exportScene(LytGuidebookScene scene, String baseName) throws IOException {
        var scenePath = exporter.getPageSpecificPathForWriting(baseName + ".scene.gz");
        var exporter = new SceneExporter(this.exporter);
        var sceneContent = exporter.export(scene.getScene());
        scenePath = CacheBusting.writeAsset(scenePath, sceneContent);

        return this.exporter.getPathRelativeFromOutputFolder(scenePath);
    }
}
