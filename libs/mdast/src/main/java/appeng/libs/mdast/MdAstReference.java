package appeng.libs.mdast;

/**
 * Reference represents a marker that is associated to another node.
 */
public interface MdAstReference extends MdAstAssociation {
    /**
     * The explicitness of the reference.
     */
    MdAstReferenceType referenceType();
}
