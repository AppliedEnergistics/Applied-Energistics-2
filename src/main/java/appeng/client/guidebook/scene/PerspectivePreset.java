package appeng.client.guidebook.scene;

import net.minecraft.util.StringRepresentable;

/**
 * Camera pre-sets to easily change the orientation of a scene.
 */
public enum PerspectivePreset implements StringRepresentable {
    /**
     * An isometric camera where the northeast corner of blocks faces forward.
     */
    ISOMETRIC_NORTH_EAST("isometric-north-east"),
    /**
     * An isometric camera where the northwest corner of blocks faces forward.
     */
    ISOMETRIC_NORTH_WEST("isometric-north-west"),
    /**
     * An isometric camera where the northeast corner of blocks faces up
     */
    UP("up");

    private final String serializedName;

    PerspectivePreset(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
