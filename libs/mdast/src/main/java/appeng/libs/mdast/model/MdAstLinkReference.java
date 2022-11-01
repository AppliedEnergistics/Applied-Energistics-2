package appeng.libs.mdast.model;

import com.google.gson.stream.JsonWriter;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * LinkReference (Parent) represents a hyperlink through association, or its original source if there is no association.
 * <p>
 * LinkReference can be used where phrasing content is expected. Its content model is static phrasing content.
 * <p>
 * LinkReferences should be associated with a Definition.
 * <p>
 * For example, the following markdown:
 * <p>
 * [alpha][Bravo]
 * <p>
 * Yields:
 * <p>
 * <pre>
 * {
 * type: 'linkReference',
 * identifier: 'bravo',
 * label: 'Bravo',
 * referenceType: 'full',
 * children: [{type: 'text', value: 'alpha'}]
 * }
 * </pre>
 */
public class MdAstLinkReference extends MdAstParent<MdAstStaticPhrasingContent> implements MdAstReference, MdAstPhrasingContent {
    public String identifier;
    public String label;
    public MdAstReferenceType referenceType;

    public MdAstLinkReference() {
        super("linkReference");
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
    public MdAstReferenceType referenceType() {
        return referenceType;
    }

    @Override
    protected Class<MdAstStaticPhrasingContent> childClass() {
        return MdAstStaticPhrasingContent.class;
    }

    @Override
    protected void writeJson(JsonWriter writer) throws IOException {
        writer.name("identifier").value(identifier);
        writer.name("label").value(label);
        writer.name("referenceType").value(referenceType.getSerializedName());

        super.writeJson(writer);
    }
}
