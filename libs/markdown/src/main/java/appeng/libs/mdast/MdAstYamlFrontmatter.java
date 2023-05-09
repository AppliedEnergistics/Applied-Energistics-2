package appeng.libs.mdast;

import java.io.IOException;

import com.google.gson.stream.JsonWriter;

import appeng.libs.mdast.model.MdAstAnyContent;
import appeng.libs.mdast.model.MdAstNode;

public class MdAstYamlFrontmatter extends MdAstNode implements MdAstAnyContent {
    public String value = "";

    public MdAstYamlFrontmatter() {
        super("yamlFrontmatter");
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        writer.name("value").value(value);
    }

    @Override
    public void toText(StringBuilder buffer) {
    }
}
