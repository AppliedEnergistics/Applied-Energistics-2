package appeng.libs.mdx;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.symbol.Codes;

import java.util.List;

public class Syntax {

    public static final Extension EXTENSION = new Extension();

    static {
        EXTENSION.flow.put(Codes.lessThan, List.of(JsxFlow.INSTANCE));
        EXTENSION.text.put(Codes.lessThan, List.of(JsxText.INSTANCE));
    }

}
