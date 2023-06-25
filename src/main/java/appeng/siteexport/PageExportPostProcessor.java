package appeng.siteexport;

import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.compiler.ParsedGuidePage;
import appeng.client.guidebook.document.block.LytNode;
import appeng.client.guidebook.document.block.LytVisitor;
import appeng.client.guidebook.scene.BlockImageTagCompiler;
import appeng.client.guidebook.scene.LytGuidebookScene;
import appeng.client.guidebook.scene.SceneTagCompiler;
import appeng.libs.mdast.MdAstVisitor;
import appeng.libs.mdast.MdAstYamlFrontmatter;
import appeng.libs.mdast.mdx.model.MdxJsxAttribute;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;
import appeng.libs.mdast.model.MdAstNode;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

/**
 * Post-Processes page content before exporting it.
 */
public final class PageExportPostProcessor {

    private static final int[] BLOCKIMAGE_SCALES = {4, 8};
    private static final int GAMESCENE_PLACEHOLDER_SCALE = 2;

    public static void postprocess(ResourceExporter exporter,
                                   ParsedGuidePage page,
                                   GuidePage compiledPage) {

        // Create a mapping from source node -> compiled node to
        // allow AST postprocessors to attach more exported
        // info to the AST nodes.
        Multimap<MdAstNode, LytNode> nodeMapping = ArrayListMultimap.create();
        compiledPage.document().visit(new LytVisitor() {
            @Override
            public Result beforeNode(LytNode node) {
                if (node.getSourceNode() != null) {
                    nodeMapping.put(node.getSourceNode(), node);
                }
                return Result.CONTINUE;
            }
        }, true /* scenes may be nested within tooltips of scenes */);

        var astRoot = page.getAstRoot();

        // Strip unnecessary frontmatter nodes.
        astRoot.removeChildren(mdAstNode -> mdAstNode instanceof MdAstYamlFrontmatter, true);

        astRoot.visit(new SceneExportVisitor(exporter, nodeMapping));

        astRoot.visit(new RemovePositionVisitor());
    }

    static class SceneExportVisitor implements MdAstVisitor {
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
            if (node instanceof MdxJsxElementFields elFields) {
                var tagName = elFields.name();
                var isBlockImage = BlockImageTagCompiler.TAG_NAME.equals(tagName);
                var isGameScene = SceneTagCompiler.TAG_NAME.equals(tagName);
                if (isBlockImage || isGameScene) {
                    var scenes = nodeMapping.get(node)
                            .stream()
                            .map(lytNode -> lytNode instanceof LytGuidebookScene lytScene ? lytScene : null)
                            .filter(Objects::nonNull)
                            .toList();

                    if (scenes.isEmpty()) {
                        LOG.warn("Found no layout scenes associated with element {} @ {}:{}", tagName, exporter.getCurrentPageId(), node.position());
                        return Result.CONTINUE;
                    } else if (scenes.size() > 1) {
                        LOG.warn("Found multiple layout scenes associated with element {} @ {}:{}", tagName, exporter.getCurrentPageId(), node.position());
                    }

                    var scene = scenes.get(0);

                    var exportNamePrefix = isBlockImage ? "blockimage" : "scene";
                    var exportName = exportNamePrefix + (++index);

                    if (isGameScene) {
                        var relativePath = exportGltfScene(scene, exportName);
                        elFields.addAttribute("src", relativePath);
                    }

                    if (isBlockImage) {
                        // Since block images are non-interactive and have no annotations, we just render them
                        // ahead of time.
                        for (int scale : BLOCKIMAGE_SCALES) {
                            var imagePath = exporter.getPageSpecificPathForWriting(exportName + "@" + scale + ".png");
                            var relativeImagePath = exporter.getPathRelativeFromOutputFolder(imagePath);
                            scene.exportAsPng(imagePath, scale, true);
                            elFields.attributes().add(new MdxJsxAttribute("src@" + scale, relativeImagePath));
                        }
                    } else if (isGameScene) {
                        // For GameScenes, we create a placeholder PNG to show in place of the WebGL scene
                        // while that is still loading.
                        var imagePath = exporter.getPageSpecificPathForWriting(exportName + ".png");
                        var relativeImagePath = exporter.getPathRelativeFromOutputFolder(imagePath);
                        scene.exportAsPng(imagePath, GAMESCENE_PLACEHOLDER_SCALE, true);
                        elFields.attributes().add(new MdxJsxAttribute("placeholder", relativeImagePath));
                    }

                    // Export the preferred size as width/height attributes
                    var preferredSize = scene.getPreferredSize();
                    if (!elFields.hasAttribute("width")) {
                        elFields.addAttribute("width", preferredSize.width());
                    }
                    if (!elFields.hasAttribute("height")) {
                        elFields.addAttribute("height", preferredSize.height());
                    }

                    return Result.CONTINUE;
                }
            }
            return Result.CONTINUE;
        }

        private String exportGltfScene(LytGuidebookScene scene, String baseName) {
            Path assetsFolder = exporter.getOutputFolder().resolve("!textures");
            try {
                Files.createDirectories(assetsFolder);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            var scenePath = exporter.getPageSpecificPathForWriting(baseName + ".gltf.gz");
            scene.exportGltfScene(assetsFolder, scenePath);

            return exporter.getPathRelativeFromOutputFolder(scenePath);
        }
    }

    // Strips all line information, since this is no longer useful
    static class RemovePositionVisitor implements MdAstVisitor {
        @Override
        public Result beforeNode(MdAstNode node) {
            node.position = null;
            return Result.CONTINUE;
        }
    }

}
