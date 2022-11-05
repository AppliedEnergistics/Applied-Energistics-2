package appeng.libs.mdast.mdx.model;

import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstPhrasingContent;
import appeng.libs.mdast.model.MdAstStaticPhrasingContent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MdxJsxTextElement extends MdAstParent<MdAstPhrasingContent> implements MdxJsxElementFields, MdAstStaticPhrasingContent {
    private String name;
    private List<Object> attributes;

    public MdxJsxTextElement() {
        this("", new ArrayList<>());
    }

    public MdxJsxTextElement(String name, List<Object> attributes) {
        super("mdxJsxTextElement");
        this.name = name;
        this.attributes = attributes;
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
    protected Class<MdAstPhrasingContent> childClass() {
        return MdAstPhrasingContent.class;
    }
}
