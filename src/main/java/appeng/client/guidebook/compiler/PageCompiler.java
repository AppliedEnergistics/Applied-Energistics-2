package appeng.client.guidebook.compiler;

import appeng.client.guidebook.GuidePage;
import appeng.client.guidebook.document.LytBlock;
import appeng.client.guidebook.document.LytBlockContainer;
import appeng.client.guidebook.document.LytDocument;
import appeng.client.guidebook.document.LytHeading;
import appeng.client.guidebook.document.LytParagraph;
import appeng.client.guidebook.document.LytThematicBreak;
import appeng.client.guidebook.document.flow.LytFlowContainer;
import appeng.client.guidebook.document.flow.LytFlowContent;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowText;
import appeng.libs.mdast.MdAst;
import appeng.libs.mdast.MdastOptions;
import appeng.libs.mdast.mdx.MdxMdastExtension;
import appeng.libs.mdast.model.MdAstHeading;
import appeng.libs.mdast.model.MdAstLink;
import appeng.libs.mdast.model.MdAstNode;
import appeng.libs.mdast.model.MdAstParagraph;
import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstPosition;
import appeng.libs.mdast.model.MdAstRoot;
import appeng.libs.mdast.model.MdAstText;
import appeng.libs.mdast.model.MdAstThematicBreak;
import appeng.libs.mdx.MdxSyntax;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class PageCompiler {
    private static final Logger LOGGER = LoggerFactory.getLogger(PageCompiler.class);

    public static GuidePage compile(String sourcePack, ResourceLocation id, InputStream in) throws IOException {
        String pageContent = new String(in.readAllBytes(), StandardCharsets.UTF_8);

        var options = new MdastOptions().withExtension(MdxSyntax.EXTENSION)
                .withMdastExtension(MdxMdastExtension.INSTANCE);
        var pageTree = MdAst.fromMarkdown(pageContent, options);

        // Translate page tree over to layout pages
        var document = new PageCompiler().compile(pageTree);

        return new GuidePage(sourcePack, document);
    }

    private LytDocument compile(MdAstRoot root) {
        var document = new LytDocument();
        compileBlockContext(root, document);
        return document;
    }

    private void compileBlockContext(MdAstParent<?> markdownParent, LytBlockContainer layoutParent) {
        for (var child : markdownParent.children()) {
            LytBlock layoutChild;
            if (child instanceof MdAstThematicBreak astThematicBreak) {
                layoutChild = new LytThematicBreak();
            } else if (child instanceof MdAstHeading astHeading) {
                var heading = new LytHeading();
                heading.setDepth(astHeading.depth);
                compileFlowContext(astHeading, heading);
                layoutChild = heading;
            } else if (child instanceof MdAstParagraph astParagraph) {
                var paragraph = new LytParagraph();
                compileFlowContext(astParagraph, paragraph);
                layoutChild = paragraph;
            } else {
                var paragraph = new LytParagraph();
                var unhandledNode = new LytFlowText();
                unhandledNode.setText("Unhandled Markdown node in flow context: " + debugNode((MdAstNode) child));
                paragraph.append(unhandledNode);
                layoutChild = paragraph;
                LOGGER.warn("{}", unhandledNode.getText());
            }

            layoutParent.append(layoutChild);
        }
    }

    private void compileFlowContext(MdAstParent<?> markdownParent, LytFlowContainer layoutParent) {
        for (var child : markdownParent.children()) {
            LytFlowContent layoutChild;
            if (child instanceof MdAstText astText) {
                var text = new LytFlowText();
                text.setText(astText.value);
                layoutChild = text;
            } else if (child instanceof MdAstLink astLink) {
                var link = new LytFlowLink();
                link.setUrl(astLink.url);
                link.setTitle(astLink.title);
                compileFlowContext(astLink, link);
                layoutChild = link;
            } else {
                var unhandledNode = new LytFlowText();
                unhandledNode.setText("Unhandled Markdown node in flow context: " + debugNode((MdAstNode) child));
                layoutChild = unhandledNode;
                LOGGER.warn("{}", unhandledNode.getText());
            }

            layoutParent.append(layoutChild);
        }
    }

    private String debugNode(MdAstNode child) {
        return child.type() + " (" + MdAstPosition.stringify(child.position().start()) + ")";
    }
}
