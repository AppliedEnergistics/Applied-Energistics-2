package appeng.libs.mdast.mdx.model;

import appeng.libs.mdast.model.MdAstFlowContent;
import appeng.libs.mdast.model.MdAstParent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MdxJsxFlowElement extends MdAstParent<MdAstFlowContent> implements MdxJsxElementFields {
    public MdxJsxFlowElement() {
        super("mdxJsxFlowElement");
    }

    @Override
    public @Nullable String name() {
        return null;
    }

    @Override
    public List<Object> attributes() {
        return null;
    }

    @Override
    protected Class<MdAstFlowContent> childClass() {
        return MdAstFlowContent.class;
    }
}
