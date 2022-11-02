package appeng.libs.mdast.mdx.model;

import appeng.libs.mdast.model.MdAstNode;
import org.jetbrains.annotations.Nullable;

public class MdxJsxAttribute extends MdAstNode {
    public String name;
    @Nullable
    public Object value;

    public MdxJsxAttribute() {
        super("mdxJsxAttribute");
    }

    @Override
    public void toText(StringBuilder buffer) {

    }
}
