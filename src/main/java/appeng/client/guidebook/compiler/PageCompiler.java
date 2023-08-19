package appeng.client.guidebook.compiler;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.PageCollection;
import appeng.client.guidebook.color.SymbolicColor;
import appeng.client.guidebook.document.LytErrorSink;
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
import appeng.client.guidebook.extensions.Extension;
import appeng.client.guidebook.extensions.ExtensionCollection;
import appeng.client.guidebook.extensions.ExtensionPoint;
import appeng.client.guidebook.indices.PageIndex;
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
import appeng.libs.mdast.model.MdAstCode;
import appeng.libs.mdast.model.MdAstEmphasis;
import appeng.libs.mdast.model.MdAstHeading;
import appeng.libs.mdast.model.MdAstImage;
import appeng.libs.mdast.model.MdAstInlineCode;
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
import appeng.libs.unist.UnistNode;

@ApiStatus.Internal
public final class PageCompiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageCompiler.class);

    /**
     * Default gap between block-level elements. Set as margin.
     */
    private static final int DEFAULT_ELEMENT_SPACING = 5;

    private final PageCollection pages;
    private final ExtensionCollection extensions;
    private final String sourcePack;
    private final ResourceLocation pageId;
    private final String pageContent;

    private final Map<String, TagCompiler> tagCompilers = new HashMap<>();

    // Data associated with the current page being compiled, this is used by
    // compilers to communicate with each other within the current page.
    private final Map<State<?>, Object> compilerState = new IdentityHashMap<>();

    public PageCompiler(PageCollection pages, ExtensionCollection extensions, String sourcePack,
            ResourceLocation pageId,
            String pageContent) {
        this.pages = pages;
        this.extensions = extensions;
        this.sourcePack = sourcePack;
        this.pageId = pageId;
        this.pageContent = pageContent;

        // Index available tag-compilers
        for (var tagCompiler : extensions.get(TagCompiler.EXTENSION_POINT)) {
            for (String tagName : tagCompiler.getTagNames()) {
                tagCompilers.put(tagName, tagCompiler);
            }
        }
    }

    public static ParsedGuidePage parse(String sourcePack, ResourceLocation id, InputStream in) throws IOException {
        String pageContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);
        return parse(sourcePack, id, pageContent);
    }

    public static ParsedGuidePage parse(String sourcePack, ResourceLocation id, String pageContent) {
        // Normalize line ending
        pageContent = pageContent.replaceAll("\\r\\n?", "\n");

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

    public static GuidePage compile(PageCollection pages, ExtensionCollection extensions, ParsedGuidePage parsedPage) {
        // Translate page tree over to layout pages
        var document = new PageCompiler(pages, extensions, parsedPage.sourcePack, parsedPage.id, parsedPage.source)
                .compile(parsedPage.astRoot);

        return new GuidePage(parsedPage.sourcePack, parsedPage.id, document);
    }

    public ExtensionCollection getExtensions() {
        return extensions;
    }

    public <T extends Extension> List<T> getExtensions(ExtensionPoint<T> extensionPoint) {
        return extensions.get(extensionPoint);
    }

    private LytDocument compile(MdAstRoot root) {
        var document = new LytDocument();
        document.setSourceNode(root);
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
        compileBlockContext(markdownParent.children(), layoutParent);
    }

    public void compileBlockContext(List<? extends MdAstAnyContent> children, LytBlockContainer layoutParent) {
        LytBlock previousLayoutChild = null;
        for (var child : children) {
            LytBlock layoutChild;
            if (child instanceof MdAstThematicBreak) {
                layoutChild = new LytThematicBreak();
            } else if (child instanceof MdAstList astList) {
                layoutChild = compileList(astList);
            } else if (child instanceof MdAstCode astCode) {
                var paragraph = new LytParagraph();
                paragraph.modifyStyle(style -> style.italic(true).whiteSpace(WhiteSpaceMode.PRE));
                paragraph.setMarginLeft(5);
                paragraph.appendText(astCode.value);
                layoutChild = paragraph;
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
                var compiler = tagCompilers.get(el.name());
                if (compiler == null) {
                    layoutChild = createErrorBlock("Unhandled MDX element in block context", child);
                } else {
                    layoutChild = null;
                    compiler.compileBlockContext(this, layoutParent, el);
                }
            } else if (child instanceof MdAstPhrasingContent phrasingContent) {
                // Wrap in a paragraph with no margins, but try appending to an existing paragraph before this
                if (previousLayoutChild instanceof LytParagraph paragraph) {
                    compileFlowContent(paragraph, phrasingContent);
                    continue;
                } else {
                    var paragraph = new LytParagraph();
                    compileFlowContent(paragraph, phrasingContent);
                    layoutChild = paragraph;
                }
            } else {
                layoutChild = createErrorBlock("Unhandled Markdown node in block context", child);
            }

            if (layoutChild != null) {
                if (child instanceof MdAstNode astNode) {
                    layoutChild.setSourceNode(astNode);
                }
                layoutParent.append(layoutChild);
            }
            previousLayoutChild = layoutChild;
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
                list.append(createErrorBlock("Cannot handle list content", listContent));
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

    public void compileFlowContext(MdAstParent<?> markdownParent, LytFlowParent layoutParent) {
        compileFlowContext(markdownParent.children(), layoutParent);
    }

    public void compileFlowContext(Collection<? extends MdAstAnyContent> children, LytFlowParent layoutParent) {
        for (var child : children) {
            compileFlowContent(layoutParent, child);
        }
    }

    private void compileFlowContent(LytFlowParent layoutParent, MdAstAnyContent content) {
        LytFlowContent layoutChild;
        if (content instanceof MdAstText astText) {
            var text = new LytFlowText();
            text.setText(astText.value);
            layoutChild = text;
        } else if (content instanceof MdAstInlineCode astCode) {
            var text = new LytFlowText();
            text.setText(astCode.value);
            text.modifyStyle(style -> style.italic(true).whiteSpace(WhiteSpaceMode.PRE));
            layoutChild = text;
        } else if (content instanceof MdAstStrong astStrong) {
            var span = new LytFlowSpan();
            span.modifyStyle(style -> style.bold(true));
            compileFlowContext(astStrong, span);
            layoutChild = span;
        } else if (content instanceof MdAstEmphasis astEmphasis) {
            var span = new LytFlowSpan();
            span.modifyStyle(style -> style.italic(true));
            compileFlowContext(astEmphasis, span);
            layoutChild = span;
        } else if (content instanceof MdAstBreak) {
            layoutChild = new LytFlowBreak();
        } else if (content instanceof MdAstLink astLink) {
            layoutChild = compileLink(astLink, layoutParent);
        } else if (content instanceof MdAstImage astImage) {
            var inlineBlock = new LytFlowInlineBlock();
            inlineBlock.setBlock(compileImage(astImage));
            layoutChild = inlineBlock;
        } else if (content instanceof MdxJsxTextElement el) {
            var compiler = tagCompilers.get(el.name());
            if (compiler == null) {
                layoutChild = createErrorFlowContent("Unhandled MDX element in flow context", content);
            } else {
                layoutChild = null;
                compiler.compileFlowContext(this, layoutParent, el);
            }
        } else {
            layoutChild = createErrorFlowContent("Unhandled Markdown node in flow context", content);
        }

        if (layoutChild != null) {
            layoutParent.append(layoutChild);
        }
    }

    private LytFlowContent compileLink(MdAstLink astLink, LytErrorSink errorSink) {
        var link = new LytFlowLink();
        if (astLink.title != null && !astLink.title.isEmpty()) {
            link.setTooltip(new TextTooltip(astLink.title));
        }
        if (astLink.url != null && !astLink.url.isEmpty()) {
            LinkParser.parseLink(this, astLink.url, new LinkParser.Visitor() {
                @Override
                public void handlePage(PageAnchor page) {
                    link.setPageLink(page);
                }

                @Override
                public void handleExternal(URI uri) {
                    link.setExternalUrl(uri);
                }

                @Override
                public void handleError(String error) {
                    errorSink.appendError(PageCompiler.this, error, astLink);
                }
            });
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
            var imageId = IdUtils.resolveLink(astImage.url, pageId);
            var imageContent = pages.loadAsset(imageId);
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

    public LytBlock createErrorBlock(String text, UnistNode child) {
        var paragraph = new LytParagraph();
        paragraph.append(createErrorFlowContent(text, child));
        return paragraph;
    }

    public LytFlowContent createErrorFlowContent(String text, UnistNode child) {
        LytFlowSpan span = new LytFlowSpan();
        span.modifyStyle(style -> {
            style.color(SymbolicColor.ERROR_TEXT).whiteSpace(WhiteSpaceMode.PRE);
        });

        // Find the position in the source
        var position = child.position();
        if (position != null) {
            var pos = position.start();
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
        } else {
            LOGGER.warn("{}\n", text);
        }

        return span;
    }

    public ResourceLocation resolveId(String idText) {
        return IdUtils.resolveId(idText, pageId.getNamespace());
    }

    /**
     * Get the current page id.
     */
    public ResourceLocation getPageId() {
        return pageId;
    }

    public PageCollection getPageCollection() {
        return pages;
    }

    public byte @Nullable [] loadAsset(ResourceLocation imageId) {
        return pages.loadAsset(imageId);
    }

    public <T extends PageIndex> T getIndex(Class<T> clazz) {
        return pages.getIndex(clazz);
    }

    public <T> T getCompilerState(State<T> state) {
        var current = compilerState.getOrDefault(state, state.defaultValue);
        return state.dataClass.cast(current);
    }

    public <T> void setCompilerState(State<T> state, T value) {
        compilerState.put(state, value);
    }

    public <T> void clearCompilerState(State<T> state) {
        compilerState.remove(state);
    }

    public record State<T> (String name, Class<T> dataClass, T defaultValue) {
    }
}
