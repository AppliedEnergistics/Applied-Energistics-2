package appeng.libs.mdast.mdx.model;

import appeng.libs.mdast.model.MdAstParent;
import appeng.libs.mdast.model.MdAstPhrasingContent;
import appeng.libs.mdast.model.MdAstStaticPhrasingContent;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MdxJsxTextElement extends MdAstParent<MdAstPhrasingContent> implements MdxJsxElementFields, MdAstStaticPhrasingContent {
    public MdxJsxTextElement() {
        super("mdxJsxTextElement");
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
    protected Class<MdAstPhrasingContent> childClass() {
        return MdAstPhrasingContent.class;
    }
}
