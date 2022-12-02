package appeng.libs.mdx;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.symbol.Codes;

import java.util.Collections;
import java.util.List;

public class MdxSyntax {

    public static final Extension INSTANCE = new Extension();

    static {
        INSTANCE.flow.put(Codes.lessThan, List.of(JsxFlow.INSTANCE));
        INSTANCE.text.put(Codes.lessThan, List.of(JsxText.INSTANCE));

        // See https://github.com/micromark/micromark-extension-mdx-md/blob/main/index.js
        Collections.addAll(
                INSTANCE.nullDisable,
                "autolink", "codeIndented", "htmlFlow", "htmlText"
        );
    }

}
