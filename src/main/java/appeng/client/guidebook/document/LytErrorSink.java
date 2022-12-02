package appeng.client.guidebook.document;

import appeng.client.guidebook.compiler.PageCompiler;
import appeng.libs.unist.UnistNode;

public interface LytErrorSink {
    void appendError(PageCompiler compiler, String text, UnistNode node);
}
