package appeng.server.testworld;

import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import appeng.api.parts.IPartItem;
import appeng.api.parts.PartHelper;
import appeng.util.SettingsFrom;

record PlacePart(BoundingBox bb, IPartItem<?> what, @Nullable Direction side,
        @Nullable BiConsumer<HolderLookup.Provider, ItemStack> stackCustomizer) implements BlockPlacingBuildAction {
    PlacePart(BoundingBox bb, IPartItem<?> what, @Nullable Direction side) {
        this(bb, what, side, null);
    }

    @Override
    public BoundingBox getBoundingBox() {
        return bb;
    }

    @Override
    public void placeBlock(ServerLevel level, Player player, BlockPos pos, BlockPos minPos, BlockPos maxPos) {
        var actualSide = Objects.requireNonNullElse(side, Direction.UP);
        var part = PartHelper.setPart(level, pos, actualSide, player, what);
        if (part != null && stackCustomizer != null) {
            // Apply the customized item stack's components (i.e. enchantments) the same way real placement does
            var stack = new ItemStack(what);
            stackCustomizer.accept(level.registryAccess(), stack);
            part.importSettings(SettingsFrom.DISMANTLE_ITEM, stack.getComponents(), player);
        }
    }
}
