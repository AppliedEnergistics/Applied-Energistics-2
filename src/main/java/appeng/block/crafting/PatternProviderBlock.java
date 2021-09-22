package appeng.block.crafting;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

import appeng.api.util.IOrientable;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.menu.MenuLocator;
import appeng.menu.MenuOpener;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.util.InteractionUtil;

public class PatternProviderBlock extends AEBaseEntityBlock<PatternProviderBlockEntity> {

    private static final BooleanProperty OMNIDIRECTIONAL = BooleanProperty.create("omnidirectional");

    public PatternProviderBlock() {
        super(defaultProps(Material.METAL));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OMNIDIRECTIONAL);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState currentState, PatternProviderBlockEntity be) {
        return currentState.setValue(OMNIDIRECTIONAL, be.isOmniDirectional());
    }

    @Override
    public InteractionResult onActivated(final Level level, final BlockPos pos, final Player p,
            final InteractionHand hand,
            final @Nullable ItemStack heldItem, final BlockHitResult hit) {
        if (InteractionUtil.isInAlternateUseMode(p)) {
            return InteractionResult.PASS;
        }

        final BlockEntity tg = this.getBlockEntity(level, pos);
        if (tg != null) {
            if (!level.isClientSide()) {
                MenuOpener.open(PatternProviderMenu.TYPE, p,
                        MenuLocator.forBlockEntitySide(tg, hit.getDirection()));
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    @Override
    protected boolean hasCustomRotation() {
        return true;
    }

    @Override
    protected void customRotateBlock(final IOrientable rotatable, final Direction axis) {
        if (rotatable instanceof PatternProviderBlockEntity patternProvider) {
            patternProvider.setSide(axis);
        }
    }
}
