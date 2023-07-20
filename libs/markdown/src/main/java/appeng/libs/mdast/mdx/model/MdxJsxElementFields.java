package appeng.libs.mdast.mdx.model;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import appeng.libs.mdast.model.MdAstAnyContent;
import appeng.libs.unist.UnistNode;

public interface MdxJsxElementFields extends UnistNode {
    @Nullable
    String name();

    void setName(String name);

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
        var jsxAttribute = getAttribute(name);
        return jsxAttribute != null ? jsxAttribute.getStringValue() : defaultValue;
    }

    default void setAttribute(String name, String value) {
        for (var attribute : attributes()) {
            if (attribute instanceof MdxJsxAttribute attr && name.equals(attr.name)) {
                attr.setValue(value);
                return;
            }
        }

        addAttribute(name, value);
    }

    default void addAttribute(String name, String value) {
        attributes().add(new MdxJsxAttribute(name, value));
    }

    default void addAttribute(String name, int value) {
        var attr = new MdxJsxAttribute();
        attr.name = name;
        attr.setExpression(Integer.toString(value));
        attributes().add(attr);
    }

    default void addAttribute(String name, float value) {
        var attr = new MdxJsxAttribute();
        attr.name = name;
        attr.setExpression(Float.toString(value));
        attributes().add(attr);
    }

    default void addAttribute(String name, boolean value) {
        var attr = new MdxJsxAttribute();
        attr.name = name;
        attr.setExpression(Boolean.toString(value));
        attributes().add(attr);
    }

    @Nullable
    default MdxJsxAttribute getAttribute(String name) {
        for (var attributeNode : attributes()) {
            if (attributeNode instanceof MdxJsxAttribute jsxAttribute) {
                if (name.equals(jsxAttribute.name)) {
                    return jsxAttribute;
                }
            } else if (attributeNode instanceof MdxJsxExpressionAttribute jsxExpressionAttribute) {
                throw new IllegalStateException("Attribute spreads unsupported!");
            }
        }

        return null;
    }
}
