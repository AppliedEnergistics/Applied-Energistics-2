package appeng.libs.mdast.mdx.model;

import appeng.libs.unist.UnistNode;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface MdxJsxElementFields extends UnistNode {
    @Nullable
    String name();

    List<MdxJsxAttributeNode> attributes();

    List<? extends UnistNode> children();

    default boolean hasAttribute(String name) {
        for (var attributeNode : attributes()) {
            if (attributeNode instanceof MdxJsxAttribute jsxAttribute) {
                if (name.equals(jsxAttribute.name)) {
                    return true;
                }
            } else if (attributeNode instanceof MdxJsxExpressionAttribute jsxExpressionAttribute) {
                throw new IllegalStateException("Attribute spreads unsupported!");
            }
        }

        return false;
    }

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
