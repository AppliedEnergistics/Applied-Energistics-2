package appeng.libs.mdast.mdx.model;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MdxJsxElementFields {
    @Nullable
    String name();

    List<MdxJsxAttributeNode> attributes();
}
