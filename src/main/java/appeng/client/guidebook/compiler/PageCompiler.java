package appeng.client.guidebook.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ResourceLocationException;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.GuideManager;
import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.block.LytDocument;
import appeng.client.guidebook.document.block.LytHeading;
import appeng.client.guidebook.document.block.LytImage;
import appeng.client.guidebook.document.block.LytList;
import appeng.client.guidebook.document.block.LytListItem;
import appeng.client.guidebook.document.block.LytParagraph;
import appeng.client.guidebook.document.block.LytThematicBreak;
import appeng.client.guidebook.document.block.table.LytTable;
import appeng.client.guidebook.document.flow.LytFlowBreak;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowInlineBlock;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.document.flow.LytFlowText;
import appeng.client.guidebook.document.interaction.TextTooltip;
import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.style.TextAlignment;
import appeng.client.guidebook.style.WhiteSpaceMode;
import appeng.libs.mdast.MdAst;
import appeng.libs.mdast.MdAstYamlFrontmatter;
import appeng.libs.mdast.MdastOptions;
import appeng.libs.mdast.YamlFrontmatterExtension;
import appeng.libs.mdast.gfm.GfmTableMdastExtension;
import appeng.libs.mdast.gfm.model.GfmTable;
import appeng.libs.mdast.mdx.MdxMdastExtension;
import appeng.libs.mdast.mdx.model.MdxJsxFlowElement;
import appeng.libs.mdast.mdx.model.MdxJsxTextElement;
import appeng.libs.mdast.model.MdAstAnyContent;
import appeng.libs.mdast.model.MdAstBreak;
import appeng.libs.mdast.model.MdAstHeading;
import appeng.libs.mdast.model.MdAstImage;
import appeng.libs.mdast.model.MdAstLink;
import appeng.libs.mdast.model.MdAstList;
import appeng.libs.mdast.model.MdAstListItem;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.mdast.model.MdAstParagraph;
import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstPhrasingContent;
import appeng.libs.mdast.model.MdAstPosition;
import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.mdast.model.MdAstStrong;
import appeng.libs.mdast.model.MdAstText;
import appeng.libs.mdast.model.MdAstThematicBreak;
import appeng.libs.mdx.MdxSyntax;
import appeng.libs.micromark.extensions.YamlFrontmatterSyntax;
import appeng.libs.micromark.extensions.gfm.GfmTableSyntax;

public final class PageCompiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageCompiler.class);

    /**
     * Default gap between block-level elements. Set as margin.
     */
    private static final int DEFAULT_ELEMENT_SPACING = 5;

    private final Function<ResourceLocation, byte[]> assetLoader;
    private final String sourcePack;
    private final ResourceLocation id;
    private final String pageContent;

    public PageCompiler(Function<ResourceLocation, byte[]> assetLoader,
            String sourcePack,
            ResourceLocation id,
            String pageContent) {
        this.assetLoader = assetLoader;
        this.sourcePack = sourcePack;
        this.id = id;
        this.pageContent = pageContent;
    }

    public static ParsedGuidePage parse(String sourcePack, ResourceLocation id, InputStream in) throws IOException {
        String pageContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        var options = new MdastOptions()
                .withSyntaxExtension(MdxSyntax.INSTANCE)
                .withSyntaxExtension(YamlFrontmatterSyntax.INSTANCE)
                .withSyntaxExtension(GfmTableSyntax.INSTANCE)
                .withMdastExtension(MdxMdastExtension.INSTANCE)
                .withMdastExtension(YamlFrontmatterExtension.INSTANCE)
                .withMdastExtension(GfmTableMdastExtension.INSTANCE);

        var astRoot = MdAst.fromMarkdown(pageContent, options);

        // Find front-matter
        var frontmatter = parseFrontmatter(id, astRoot);

        return new ParsedGuidePage(sourcePack, id, pageContent, astRoot, frontmatter);
    }

    public static GuidePage compile(Function<ResourceLocation, byte[]> resourceLookup, ParsedGuidePage parsedPage) {
        // Translate page tree over to layout pages
        var document = new PageCompiler(resourceLookup, parsedPage.sourcePack, parsedPage.id, parsedPage.source)
                .compile(parsedPage.astRoot);

        return new GuidePage(parsedPage.sourcePack, parsedPage.id, document);
    }

    private LytDocument compile(MdAstRoot root) {
        var document = new LytDocument();
        compileBlockContext(root, document);
        return document;
    }

    private static Frontmatter parseFrontmatter(ResourceLocation pageId, MdAstRoot root) {
        Frontmatter result = null;

        for (var child : root.children()) {
            if (child instanceof MdAstYamlFrontmatter frontmatter) {
                if (result != null) {
                    LOGGER.error("Found more than one frontmatter!"); // TODO: proper debugging
                    continue;
                }
                try {
                    result = Frontmatter.parse(pageId, frontmatter.value);
                } catch (Exception e) {
                    LOGGER.error("Failed to parse frontmatter for page {}", pageId, e);
                    break;
                }
            }
        }

        return Objects.requireNonNullElse(result, new Frontmatter(null, Map.of()));
    }

    public void compileBlockContext(MdAstParent<?> markdownParent, LytBlockContainer layoutParent) {
        for (var child : markdownParent.children()) {
            LytBlock layoutChild;
            if (child instanceof MdAstThematicBreak) {
                layoutChild = new LytThematicBreak();
            } else if (child instanceof MdAstList astList) {
                layoutChild = compileList(astList);
            } else if (child instanceof MdAstHeading astHeading) {
                var heading = new LytHeading();
                heading.setDepth(astHeading.depth);
                compileFlowContext(astHeading, heading);
                layoutChild = heading;
            } else if (child instanceof MdAstParagraph astParagraph) {
                var paragraph = new LytParagraph();
                compileFlowContext(astParagraph, paragraph);
                paragraph.setMarginTop(DEFAULT_ELEMENT_SPACING);
                paragraph.setMarginBottom(DEFAULT_ELEMENT_SPACING);
                layoutChild = paragraph;
            } else if (child instanceof MdAstYamlFrontmatter) {
                // This is handled by compile directly
                layoutChild = null;
            } else if (child instanceof GfmTable astTable) {
                layoutChild = compileTable(astTable);
            } else if (child instanceof MdxJsxFlowElement el) {
                var compiler = TagCompilers.get(el.name());
                if (compiler == null) {
                    layoutChild = createErrorBlock("Unhandled MDX element in block context", (MdAstNode) child);
                } else {
                    layoutChild = null;
                    compiler.compileBlockContext(this, layoutParent, el);
                }
            } else if (child instanceof MdAstPhrasingContent phrasingContent) {
                // Wrap in a paragraph with no margins
                var paragraph = new LytParagraph();
                compileFlowContent(paragraph, phrasingContent);
                layoutChild = paragraph;
            } else {
                layoutChild = createErrorBlock("Unhandled Markdown node in block context", (MdAstNode) child);
            }

            if (layoutChild != null) {
                layoutParent.append(layoutChild);
            }
        }
    }

    private LytList compileList(MdAstList astList) {
        var list = new LytList(astList.ordered, astList.start);
        for (var listContent : astList.children()) {
            if (listContent instanceof MdAstListItem astListItem) {
                var listItem = new LytListItem();
                compileBlockContext(astListItem, listItem);

                // Fix up top/bottom margin for list item children
                var children = listItem.getChildren();
                if (!children.isEmpty()) {
                    var firstChild = children.get(0);
                    if (firstChild instanceof LytBlock firstBlock) {
                        firstBlock.setMarginTop(0);
                        firstBlock.setMarginBottom(0);
                    }
                }
                list.append(listItem);
            } else {
                list.append(createErrorBlock("Cannot handle list content", (MdAstNode) listContent));
            }
        }
        return list;
    }

    private LytBlock compileTable(GfmTable astTable) {
        var table = new LytTable();
        table.setMarginBottom(DEFAULT_ELEMENT_SPACING);

        boolean firstRow = true;
        for (var astRow : astTable.children()) {
            var row = table.appendRow();
            if (firstRow) {
                row.modifyStyle(style -> style.bold(true));
                firstRow = false;
            }

            var astCells = astRow.children();
            for (int i = 0; i < astCells.size(); i++) {
                var cell = row.appendCell();
                // Apply alignment
                if (astTable.align != null && i < astTable.align.size()) {
                    switch (astTable.align.get(i)) {
                        case CENTER -> cell.modifyStyle(style -> style.alignment(TextAlignment.CENTER));
                        case RIGHT -> cell.modifyStyle(style -> style.alignment(TextAlignment.RIGHT));
                    }
                }

                compileBlockContext(astCells.get(i), cell);
            }
        }

        return table;
    }

    /**
     * Converts formatted Minecraft text into our flow content.
     */
    public void compileComponentToFlow(FormattedText formattedText, LytFlowParent layoutParent) {
        formattedText.visit((style, text) -> {
            if (style.isEmpty()) {
                layoutParent.appendText(text);
            } else {
                var span = new LytFlowSpan();
                // TODO: Convert style
                span.appendText(text);
                layoutParent.append(span);
            }
            return Optional.empty();
        }, Style.EMPTY);
    }

    public void compileFlowContext(MdAstParent<?> markdownParent, LytFlowParent layoutParent) {
        for (var child : markdownParent.children()) {
            compileFlowContent(layoutParent, child);
        }
    }

    private void compileFlowContent(LytFlowParent layoutParent, MdAstAnyContent content) {
        LytFlowContent layoutChild;
        if (content instanceof MdAstText astText) {
            var text = new LytFlowText();
            text.setText(astText.value);
            layoutChild = text;
        } else if (content instanceof MdAstStrong astStrong) {
            var span = new LytFlowSpan();
            span.modifyStyle(style -> style.bold(true));
            compileFlowContext(astStrong, span);
            layoutChild = span;
        } else if (content instanceof MdAstBreak) {
            layoutChild = new LytFlowBreak();
        } else if (content instanceof MdAstLink astLink) {
            layoutChild = compileLink(astLink);
        } else if (content instanceof MdAstImage astImage) {
            var inlineBlock = new LytFlowInlineBlock();
            inlineBlock.setBlock(compileImage(astImage));
            layoutChild = inlineBlock;
        } else if (content instanceof MdxJsxTextElement el) {
            var compiler = TagCompilers.get(el.name());
            if (compiler == null) {
                layoutChild = createErrorFlowContent("Unhandled MDX element in flow context", (MdAstNode) content);
            } else {
                layoutChild = null;
                compiler.compileFlowContext(this, layoutParent, el);
            }
        } else {
            layoutChild = createErrorFlowContent("Unhandled Markdown node in flow context", (MdAstNode) content);
        }

        if (layoutChild != null) {
            layoutParent.append(layoutChild);
        }
    }

    private LytFlowContent compileLink(MdAstLink astLink) {
        var link = new LytFlowLink();
        if (astLink.title != null && !astLink.title.isEmpty()) {
            link.setTooltip(new TextTooltip(astLink.title));
        }

        // Internal vs. external links
        var uri = URI.create(astLink.url);
        if (uri.isAbsolute()) {
            link.setClickCallback(screen -> {
                var mc = Minecraft.getInstance();
                mc.setScreen(new ConfirmLinkScreen(yes -> {
                    if (yes) {
                        Util.getPlatform().openUri(uri);
                    }

                    mc.setScreen(screen);
                }, astLink.url, false));
            });
        } else {

            // Determine the page id, account for relative paths
            ResourceLocation pageId;
            try {
                pageId = IdUtils.resolveLink(uri.getPath(), id);
            } catch (ResourceLocationException ignored) {
                return createErrorFlowContent("Invalid page link", astLink);
            }

            if (!GuideManager.INSTANCE.pageExists(pageId)) {
                LOGGER.error("Broken link to page '{}' in page {}", astLink.url, id);
            } else {
                var anchor = new PageAnchor(pageId, uri.getFragment());
                link.setClickCallback(screen -> {
                    screen.navigateTo(anchor);
                });
            }
        }

        compileFlowContext(astLink, link);
        return link;
    }

    @NotNull
    private LytImage compileImage(MdAstImage astImage) {
        var image = new LytImage();
        image.setTitle(astImage.title);
        image.setAlt(astImage.alt);
        try {
            var imageId = IdUtils.resolveLink(astImage.url, id);
            var imageContent = assetLoader.apply(imageId);
            if (imageContent == null) {
                LOGGER.error("Couldn't find image {}", astImage.url);
                image.setTitle("Missing image: " + astImage.url);
            }
            image.setImage(imageId, imageContent);
        } catch (ResourceLocationException e) {
            LOGGER.error("Invalid image id: {}", astImage.url);
            image.setTitle("Invalid image URL: " + astImage.url);
        }
        return image;
    }

    public LytBlock createErrorBlock(String text, MdAstNode child) {
        var paragraph = new LytParagraph();
        paragraph.append(createErrorFlowContent(text, child));
        return paragraph;
    }

    public LytFlowContent createErrorFlowContent(String text, MdAstNode child) {
        LytFlowSpan span = new LytFlowSpan();
        span.modifyStyle(style -> {
            style.color(new ColorRef(0xFFFF0000))
                    .whiteSpace(WhiteSpaceMode.PRE);
        });

        // Find the position in the source
        var pos = child.position().start();
        var startOfLine = pageContent.lastIndexOf('\n', pos.offset()) + 1;
        var endOfLine = pageContent.indexOf('\n', pos.offset() + 1);
        if (endOfLine == -1) {
            endOfLine = pageContent.length();
        }
        var line = pageContent.substring(startOfLine, endOfLine);

        text += " " + child.type() + " (" + MdAstPosition.stringify(pos) + ")";

        span.appendText(text);
        span.appendBreak();

        span.appendText(line);
        span.appendBreak();

        span.appendText("~".repeat(pos.column() - 1) + "^");
        span.appendBreak();

        LOGGER.warn("{}\n{}\n{}\n", text, line, "~".repeat(pos.column() - 1) + "^");

        return span;
    }

    public ResourceLocation resolveId(String idText) {
        return IdUtils.resolveId(idText, id.getNamespace());
    }

    public ResourceLocation getId() {
        return id;
    }

    public byte[] loadAsset(ResourceLocation imageId) {
        return assetLoader.apply(imageId);
    }
}
