package appeng.libs.mdast.model;

import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * ImageReference (Node) represents an image through association, or its original source if there is no association.
 * <p>
 * ImageReference can be used where phrasing content is expected. It has no content model, but is described by its alt field.
 * <p>
 * ImageReference should be associated with a Definition.
 * <p>
 * For example, the following markdown:
 * <p>
 * ![alpha][bravo]
 * <p>
 * Yields:
 * <p>
 * <pre>
 * {
 * type: 'imageReference',
 * identifier: 'bravo',
 * label: 'bravo',
 * referenceType: 'full',
 * alt: 'alpha'
 * }
 * </pre>
 */
public class MdAstImageReference extends MdAstNode implements MdAstReference, MdAstAlternative, MdAstStaticPhrasingContent {
    public String alt;
    public String identifier;
    public String label;
    public MdAstReferenceType referenceType;

    public MdAstImageReference() {
        super("imageReference");
    }

    @Override
    public @Nullable String alt() {
        return alt;
    }

    @Override
    public String identifier() {
        return identifier;
    }

    @Override
    public @Nullable String label() {
        return label;
    }

    @Override
    public void toText(StringBuilder buffer) {
    }

    @Override
    public MdAstReferenceType referenceType() {
        return referenceType;
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        if (alt != null) {
            writer.name("alt").value(alt);
        }
        writer.name("identifier").value(identifier);
        writer.name("label").value(label);
        writer.name("referenceType").value(referenceType.getSerializedName());

        super.writeJson(writer);
    }
}
