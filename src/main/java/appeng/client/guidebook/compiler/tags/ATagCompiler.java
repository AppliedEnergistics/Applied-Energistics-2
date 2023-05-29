package appeng.client.guidebook.compiler.tags;

import java.net.URI;
import java.util.Set;

import appeng.client.guidebook.PageAnchor;
import appeng.client.guidebook.compiler.LinkParser;
import appeng.client.guidebook.compiler.PageCompiler;
import appeng.client.guidebook.document.flow.LytFlowAnchor;
import appeng.client.guidebook.document.flow.LytFlowLink;
import appeng.client.guidebook.document.flow.LytFlowParent;
import appeng.client.guidebook.document.interaction.TextTooltip;
import appeng.libs.mdast.mdx.model.MdxJsxElementFields;

public class ATagCompiler extends FlowTagCompiler {
    @Override
    public Set<String> getTagNames() {
        return Set.of("a");
    }

    @Override
    protected void compile(PageCompiler compiler, LytFlowParent parent, MdxJsxElementFields el) {
        var anchorName = el.getAttributeString("name", "");
        if (!anchorName.isEmpty()) {
            parent.append(new LytFlowAnchor(anchorName));
        }

        // The link only materializes if it has a HREF attribute, otherwise we compile the children directly
        var href = el.getAttributeString("href", "");
        var title = el.getAttributeString("title", "");
        if (!href.isEmpty() || !title.isEmpty()) {
            var link = new LytFlowLink();
            if (!title.isEmpty()) {
                link.setTooltip(new TextTooltip(title));
            }
            if (!href.isEmpty()) {
                LinkParser.parseLink(compiler, href, new LinkParser.Visitor() {
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
                        parent.appendError(compiler, error, el);
                    }
                });
            }
            compiler.compileFlowContext(el.children(), link);
            parent.append(link);
        } else {
            compiler.compileFlowContext(el.children(), parent);
        }
    }
}
