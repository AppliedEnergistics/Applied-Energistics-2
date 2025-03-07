package appeng.api.util;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

public enum AEColorVariant implements StringRepresentable {
    DARK("dark"),
    MEDIUM("medium"),
    MEDIUM_BRIGHT("medium_bright"),
    BRIGHT("bright");

    public static final Codec<AEColorVariant> CODEC = StringRepresentable.fromEnum(AEColorVariant::values);

    private final String serializedName;

    AEColorVariant(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
