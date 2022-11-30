package appeng.client.guidebook.document;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.libs.mdast.model.MdAstNode;

public interface LytErrorSink {
    void appendError(PageCompiler compiler, String text, MdAstNode node);
}
