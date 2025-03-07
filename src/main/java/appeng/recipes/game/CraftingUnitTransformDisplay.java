package appeng.recipes.game;

import appeng.core.definitions.AEBlocks;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.display.RecipeDisplay;
import net.minecraft.world.item.crafting.display.SlotDisplay;
import net.minecraft.world.level.block.Block;

public record CraftingUnitTransformDisplay(
        Block upgradedBlock,
        SlotDisplay upgradeItem
) implements RecipeDisplay {

    public static final MapCodec<CraftingUnitTransformDisplay> MAP_CODEC = RecordCodecBuilder.mapCodec(
            builder -> builder.group(
                            Block.CODEC.fieldOf("upgradedBlock").forGetter(CraftingUnitTransformDisplay::upgradedBlock),
                            SlotDisplay.CODEC.fieldOf("upgradeItem").forGetter(CraftingUnitTransformDisplay::upgradeItem)
                    )
                    .apply(builder, CraftingUnitTransformDisplay::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf, CraftingUnitTransformDisplay> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(Registries.BLOCK), CraftingUnitTransformDisplay::upgradedBlock,
            SlotDisplay.STREAM_CODEC, CraftingUnitTransformDisplay::upgradeItem,
            CraftingUnitTransformDisplay::new
    );

    public static final RecipeDisplay.Type<CraftingUnitTransformDisplay> TYPE = new RecipeDisplay.Type<>(MAP_CODEC, STREAM_CODEC);

    @Override
    public SlotDisplay result() {
        return new SlotDisplay.ItemSlotDisplay(upgradedBlock.asItem());
    }

    @Override
    public SlotDisplay craftingStation() {
        return new SlotDisplay.ItemSlotDisplay(AEBlocks.CRAFTING_UNIT.asItem());
    }

    @Override
    public Type<? extends RecipeDisplay> type() {
        return TYPE;
    }
}
