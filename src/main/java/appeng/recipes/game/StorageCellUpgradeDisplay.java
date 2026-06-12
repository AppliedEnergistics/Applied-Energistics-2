package appeng.recipes.game;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;

public record StorageCellUpgradeDisplay(
        SlotDisplay inputCell,
        SlotDisplay inputComponent,
        SlotDisplay result,
        SlotDisplay resultComponent) implements RecipeDisplay {

    public static final MapCodec<StorageCellUpgradeDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                    SlotDisplay.CODEC.fieldOf("inputCell").forGetter(StorageCellUpgradeDisplay::inputCell),
                    SlotDisplay.CODEC.fieldOf("inputComponent").forGetter(StorageCellUpgradeDisplay::inputComponent),
                    SlotDisplay.CODEC.fieldOf("result").forGetter(StorageCellUpgradeDisplay::result),
                    SlotDisplay.CODEC.fieldOf("resultComponent").forGetter(StorageCellUpgradeDisplay::resultComponent))
                    .apply(builder, StorageCellUpgradeDisplay::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, StorageCellUpgradeDisplay> STREAM_CODEC = StreamCodec
            .composite(
                    SlotDisplay.STREAM_CODEC, StorageCellUpgradeDisplay::inputCell,
                    SlotDisplay.STREAM_CODEC, StorageCellUpgradeDisplay::inputComponent,
                    SlotDisplay.STREAM_CODEC, StorageCellUpgradeDisplay::result,
                    SlotDisplay.STREAM_CODEC, StorageCellUpgradeDisplay::resultComponent,
                    StorageCellUpgradeDisplay::new);

    public static final RecipeDisplay.Type<StorageCellUpgradeDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC,
            STREAM_CODEC);

    @Override
    public SlotDisplay result() {
        return result;
    }

    @Override
    public SlotDisplay craftingStation() {
        return new SlotDisplay.ItemSlotDisplay(Items.CRAFTING_TABLE);
    }

    @Override
    public Type<? extends RecipeDisplay> type() {
        return TYPE;
    }
}
