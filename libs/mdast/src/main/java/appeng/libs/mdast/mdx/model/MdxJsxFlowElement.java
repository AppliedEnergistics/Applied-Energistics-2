package appeng.libs.mdast.mdx.model;

import appeng.libs.mdast.model.MdAstFlowContent;
import appeng.libs.mdast.model.MdAstParent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MdxJsxFlowElement extends MdAstParent<MdAstFlowContent> implements MdxJsxElementFields {
    public String name;
    public List<Object> attributes;

    public MdxJsxFlowElement() {
        this("", new ArrayList<>());
    }

    public MdxJsxFlowElement(String name, List<Object> attributes) {
        super("mdxJsxFlowElement");
    }

    @Override
    public @Nullable String name() {
        return name;
    }

    @Override
    public List<Object> attributes() {
        return attributes;
    }

    @Override
    protected Class<MdAstFlowContent> childClass() {
        return MdAstFlowContent.class;
    }
}
