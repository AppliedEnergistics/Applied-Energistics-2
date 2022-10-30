package appeng.libs.mdx;

import appeng.libs.micromark.Extension;
import appeng.libs.micromark.symbol.Codes;

import java.util.Collections;
import java.util.List;

public class Syntax {

    public static final Extension EXTENSION = new Extension();

    static {
        EXTENSION.flow.put(Codes.lessThan, List.of(JsxFlow.INSTANCE));
        EXTENSION.text.put(Codes.lessThan, List.of(JsxText.INSTANCE));

        // See https://github.com/micromark/micromark-extension-mdx-md/blob/main/index.js
        Collections.addAll(
                EXTENSION.nullDisable,
                "autolink", "codeIndented", "htmlFlow", "htmlText"
        );
    }

}
