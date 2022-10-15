package appeng.libs.mdast;

public enum MdAstReferenceType {
    /**
     * The reference is implicit, its identifier inferred from its content.
     */
    SHORTCUT("shortcut"),
    /**
     * The reference is explicit, its identifier inferred from its content.
     */
    COLLAPSED("collapsed"),
    /**
     * The reference is explicit, its identifier explicitly set.
     */
    FULL("full");

    private final String serializedName;

    MdAstReferenceType(String serializedName) {
        this.serializedName = serializedName;
    }

    public String getSerializedName() {
        return serializedName;
    }
}
