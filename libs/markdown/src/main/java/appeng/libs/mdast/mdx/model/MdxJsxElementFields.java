package appeng.libs.mdast.mdx.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.libs.mdast.model.MdAstAnyContent;
import appeng.libs.unist.UnistNode;

public interface MdxJsxElementFields extends UnistNode {
    @Nullable
    String name();

    List<MdxJsxAttributeNode> attributes();

    List<? extends MdAstAnyContent> children();

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
