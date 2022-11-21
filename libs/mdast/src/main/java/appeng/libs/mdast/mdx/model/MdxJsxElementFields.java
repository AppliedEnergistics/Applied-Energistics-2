package appeng.libs.mdast.mdx.model;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MdxJsxElementFields {
    @Nullable
    String name();

    List<MdxJsxAttributeNode> attributes();

    default String getAttributeString(String name, String defaultValue) {
        for (var attributeNode : attributes()) {
            if (attributeNode instanceof MdxJsxAttribute jsxAttribute) {
                if (name.equals(jsxAttribute.name)) {
                    return jsxAttribute.getStringValue();
                }
            } else if (attributeNode instanceof MdxJsxExpressionAttribute jsxExpressionAttribute) {
                throw new IllegalStateException("Attribute spreads unsupported!");
            }
        }

        return defaultValue;
    }
}
