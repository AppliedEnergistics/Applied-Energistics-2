package appeng.libs.mdast.mdx.model;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

import org.jetbrains.annotations.Nullable;

import appeng.libs.mdast.model.MdAstNode;

public class MdxJsxAttribute extends MdAstNode implements MdxJsxAttributeNode {
    public String name = "";
    @Nullable
    private Object value;

    public MdxJsxAttribute() {
        super("mdxJsxAttribute");
    }

    public MdxJsxAttribute(String name, @Nullable Object value) {
        this();
        this.name = name;
        this.value = value;
    }

    @Override
    public void toText(StringBuilder buffer) {
    }

    public void setExpression(String expression) {
        var node = new MdxJsxAttributeValueExpression();
        node.value = expression;
        this.value = node;
    }

    public boolean hasExpressionValue() {
        return value instanceof MdxJsxAttributeValueExpression;
    }

    public boolean hasStringValue() {
        return value instanceof String;
    }

    public String getStringValue() {
        return (String) value;
    }

    public String getExpressionValue() {
        return ((MdxJsxAttributeValueExpression) value).value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        super.writeJson(writer);
        writer.name("name").value(name);
        writer.name("value");
        if (value == null) {
            writer.nullValue();
        } else if (value instanceof String string) {
            writer.value(string);
        } else if (value instanceof MdxJsxAttributeValueExpression expression) {
            expression.toJson(writer);
        } else {
            throw new IllegalStateException("Invalid attribute value type: " + value);
        }
    }
}
