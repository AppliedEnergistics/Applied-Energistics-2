package appeng.recipes.handlers;

import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import appeng.core.AppEng;
import appeng.items.tools.powered.EntropyManipulatorItem;

/**
 * A special recipe used for the {@link EntropyManipulatorItem}.
 */
public class EntropyRecipe implements IRecipe<IInventory> {

    public static final ResourceLocation TYPE_ID = AppEng.makeId("entropy");

    public static final IRecipeType<EntropyRecipe> TYPE = IRecipeType.register(TYPE_ID.toString());

    @Nonnull
    private final ResourceLocation id;
    @Nonnull
    private final EntropyMode mode;

    @Nullable
    private final Block inputBlock;
    @Nullable
    private final Fluid inputFluid;

    @Nullable
    private final Block outputBlock;
    @Nullable
    private final Fluid outputFluid;

    @Nonnull
    private final List<ItemStack> drops;

    public EntropyRecipe(ResourceLocation id, EntropyMode mode, Block inputBlock, Fluid inputFluid,
            Block outputBlock, Fluid outputFluid, List<ItemStack> drops) {
        Preconditions.checkArgument(id != null);
        Preconditions.checkArgument(mode != null);
        Preconditions.checkArgument(drops == null || !drops.isEmpty(),
                "drops needs to be either null or a non empty list");

        this.id = id;
        this.mode = mode;

        this.inputBlock = inputBlock;
        this.inputFluid = inputFluid;

        this.outputBlock = outputBlock;
        this.outputFluid = outputFluid;

        this.drops = drops != null ? drops : Collections.emptyList();
    }

    @Override
    public boolean matches(IInventory inv, World worldIn) {
        return false;
    }

    @Override
    public ItemStack getCraftingResult(IInventory inv) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFit(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getRecipeOutput() {
        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public IRecipeSerializer<?> getSerializer() {
        return EntropyRecipeSerializer.INSTANCE;
    }

    @Override
    public IRecipeType<?> getType() {
        return TYPE;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.create();
    }

    @Nonnull
    public EntropyMode getMode() {
        return this.mode;
    }

    @Nullable
    public Block getInputBlock() {
        return this.inputBlock;
    }

    @Nullable
    public Fluid getInputFluid() {
        return this.inputFluid;
    }

    @Nullable
    public Block getOutputBlock() {
        return this.outputBlock;
    }

    @Nullable
    public BlockState getOutputBlockState() {
        if (this.getOutputBlock() == null) {
            return null;
        }

        return getOutputBlock().getDefaultState();
    }

    @Nullable
    public Fluid getOutputFluid() {
        return this.outputFluid;
    }

    @Nullable
    public FluidState getOutputFluidState() {
        if (this.getOutputFluid() == null) {
            return null;
        }

        return getOutputFluid().getDefaultState();
    }

    @Nonnull
    public List<ItemStack> getDrops() {
        return this.drops;
    }

    public static enum EntropyMode {
        HEAT,
        COOL;
    }
}
