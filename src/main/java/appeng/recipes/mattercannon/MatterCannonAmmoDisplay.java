package appeng.recipes.mattercannon;

import appeng.recipes.transform.TransformCircumstance;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

import java.util.List;

public record MatterCannonAmmoDisplay(SlotDisplay ammo, float weight, SlotDisplay craftingStation) implements RecipeDisplay {
    public static final MapCodec<MatterCannonAmmoDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> {
        return builder.group(
                SlotDisplay.CODEC.fieldOf("ammo").forGetter(MatterCannonAmmoDisplay::ammo),
                Codec.FLOAT.fieldOf("weight").forGetter(MatterCannonAmmoDisplay::weight),
                SlotDisplay.CODEC.fieldOf("craftingStation").forGetter(MatterCannonAmmoDisplay::craftingStation)
        ).apply(builder, MatterCannonAmmoDisplay::new);
    });

    public static final StreamCodec<RegistryFriendlyByteBuf, MatterCannonAmmoDisplay> STREAM_CODEC = StreamCodec.composite(
            SlotDisplay.STREAM_CODEC,
            MatterCannonAmmoDisplay::ammo,
            ByteBufCodecs.FLOAT,
            MatterCannonAmmoDisplay::weight,
            SlotDisplay.STREAM_CODEC,
            MatterCannonAmmoDisplay::craftingStation,
            MatterCannonAmmoDisplay::new
    );

    public static final Type<MatterCannonAmmoDisplay> TYPE = new Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay result() {
        return SlotDisplay.Empty.INSTANCE;
    }

    @Override
    public SlotDisplay craftingStation() {
        return SlotDisplay.Empty.INSTANCE;
    }

    @Override
    public Type<MatterCannonAmmoDisplay> type() {
        return TYPE;
    }
}
