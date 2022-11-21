package appeng.client.guidebook.compiler;

import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.document.block.LytBlock;
import appeng.client.guidebook.document.block.LytBlockContainer;
import appeng.client.guidebook.document.block.LytDocument;
import appeng.client.guidebook.document.block.LytHeading;
import appeng.client.guidebook.document.block.LytList;
import appeng.client.guidebook.document.block.LytListItem;
import appeng.client.guidebook.document.block.LytParagraph;
import appeng.client.guidebook.document.block.LytThematicBreak;
import appeng.client.guidebook.document.flow.LytFlowBreak;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.client.guidebook.document.flow.LytFlowSpan;
import appeng.client.guidebook.document.flow.LytFlowText;
import appeng.client.guidebook.render.ColorRef;
import appeng.client.guidebook.style.WhiteSpaceMode;
import appeng.libs.mdast.MdAst;
import appeng.libs.mdast.MdAstYamlFrontmatter;
import appeng.libs.mdast.MdastOptions;
import appeng.libs.mdast.YamlFrontmatterExtension;
import appeng.libs.mdast.mdx.MdxMdastExtension;
import appeng.libs.mdast.mdx.model.MdxJsxFlowElement;
import appeng.libs.mdast.mdx.model.MdxJsxTextElement;
import appeng.libs.mdast.model.MdAstBreak;
import appeng.libs.mdast.model.MdAstHeading;
import appeng.libs.mdast.model.MdAstLink;
import appeng.libs.mdast.model.MdAstList;
import appeng.libs.mdast.model.MdAstListItem;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.mdast.model.MdAstParagraph;
import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstPosition;
import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.mdast.model.MdAstStrong;
import appeng.libs.mdast.model.MdAstText;
import appeng.libs.mdast.model.MdAstThematicBreak;
import appeng.libs.mdx.MdxSyntax;
import appeng.libs.micromark.extensions.YamlFrontmatterSyntax;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

public final class PageCompiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageCompiler.class);

    private final String sourcePack;
    private final ResourceLocation id;
    private final String pageContent;

    public PageCompiler(String sourcePack, ResourceLocation id, String pageContent) {
        this.sourcePack = sourcePack;
        this.id = id;
        this.pageContent = pageContent;
    }

    public static ParsedGuidePage parse(String sourcePack, ResourceLocation id, InputStream in) throws IOException {
        String pageContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        var options = new MdastOptions()
                .withSyntaxExtension(MdxSyntax.INSTANCE)
                .withSyntaxExtension(YamlFrontmatterSyntax.INSTANCE)
                .withMdastExtension(MdxMdastExtension.INSTANCE)
                .withMdastExtension(YamlFrontmatterExtension.INSTANCE);

        var astRoot = MdAst.fromMarkdown(pageContent, options);

        return new ParsedGuidePage(sourcePack, id, pageContent, astRoot);
    }

    public static GuidePage compile(ParsedGuidePage parsedPage) {
        // Translate page tree over to layout pages
        var document = new PageCompiler(parsedPage.sourcePack, parsedPage.id, parsedPage.source)
                .compile(parsedPage.astRoot);

        return new GuidePage(parsedPage.sourcePack, document);
    }

    private LytDocument compile(MdAstRoot root) {
        // Find front-matter
        parseFrontmatter(root);

        var document = new LytDocument();
        compileBlockContext(root, document);
        return document;
    }

    private void parseFrontmatter(MdAstRoot root) {
        Object result = null;

        for (var child : root.children()) {
            if (child instanceof MdAstYamlFrontmatter frontmatter) {
                if (result != null) {
                    LOGGER.warn("Found more than one frontmatter!");
                }
                result = frontmatter;
            }
        }
    }

    public void compileBlockContext(MdAstParent<?> markdownParent, LytBlockContainer layoutParent) {
        for (var child : markdownParent.children()) {
            LytBlock layoutChild;
            if (child instanceof MdAstThematicBreak) {
                layoutChild = new LytThematicBreak();
            } else if (child instanceof MdAstList astList) {
                var list = new LytList(astList.ordered, astList.start);
                for (var listContent : astList.children()) {
                    if (listContent instanceof MdAstListItem astListItem) {
                        var listItem = new LytListItem();
                        compileBlockContext(astListItem, listItem);
                        list.append(listItem);
                    } else {
                        list.append(createErrorBlock("Cannot handle list content", (MdAstNode) listContent));
                    }
                }
                layoutChild = list;
            } else if (child instanceof MdAstHeading astHeading) {
                var heading = new LytHeading();
                heading.setDepth(astHeading.depth);
                compileFlowContext(astHeading, heading);
                layoutChild = heading;
            } else if (child instanceof MdAstParagraph astParagraph) {
                var paragraph = new LytParagraph();
                compileFlowContext(astParagraph, paragraph);
                layoutChild = paragraph;
            } else if (child instanceof MdAstYamlFrontmatter) {
                // This is handled by compile directly
                layoutChild = null;
            } else if (child instanceof MdxJsxFlowElement el) {
                var compiler = TagCompilers.get(el.name());
                if (compiler == null) {
                    layoutChild = createErrorBlock("Unhandled MDX element in block context", (MdAstNode) child);
                } else {
                    layoutChild = null;
                    compiler.compileBlockContext(this, layoutParent, el);
                }
            } else {
                layoutChild = createErrorBlock("Unhandled Markdown node in block context", (MdAstNode) child);
            }

            if (layoutChild != null) {
                layoutParent.append(layoutChild);
            }
        }
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
            LytFlowContent layoutChild;
            if (child instanceof MdAstText astText) {
                var text = new LytFlowText();
                text.setText(astText.value);
                layoutChild = text;
            } else if (child instanceof MdAstStrong astStrong) {
                var span = new LytFlowSpan();
                span.modifyStyle(style -> style.bold(true));
                compileFlowContext(astStrong, span);
                layoutChild = span;
            } else if (child instanceof MdAstBreak) {
                layoutChild = new LytFlowBreak();
            } else if (child instanceof MdAstLink astLink) {
                var link = new LytFlowLink();
                link.setUrl(astLink.url);
                link.setTitle(astLink.title);
                compileFlowContext(astLink, link);
                layoutChild = link;
            } else if (child instanceof MdxJsxTextElement el) {
                var compiler = TagCompilers.get(el.name());
                if (compiler == null) {
                    layoutChild = createErrorFlowContent("Unhandled MDX element in flow context", (MdAstNode) child);
                } else {
                    layoutChild = null;
                    compiler.compileFlowContext(this, layoutParent, el);
                }
            } else {
                layoutChild = createErrorFlowContent("Unhandled Markdown node in flow context", (MdAstNode) child);
            }

            if (layoutChild != null) {
                layoutParent.append(layoutChild);
            }
        }
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

}
