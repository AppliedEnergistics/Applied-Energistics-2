package appeng.integration.modules.jei;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import appeng.integration.modules.jei.throwinginwater.ThrowingInWaterDisplay;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;

import appeng.api.config.CondenserOutput;
import appeng.api.implementations.items.IStorageComponent;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.client.gui.Icon;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;

class CondenserCategory implements IRecipeCategory<CondenserOutput> {

    private static final String TITLE_TRANSLATION_KEY = "block.ae2.condenser";

    public static final RecipeType<CondenserOutput> RECIPE_TYPE = RecipeType.create(AppEng.MOD_ID, "condenser",
            CondenserOutput.class);

    private final IDrawable background;
    private final IDrawableAnimated progress;

    private final IDrawable iconButton;
    private final IDrawable iconTrash;
    private final IDrawable icon;

    private final Map<CondenserOutput, IDrawable> buttonIcons;

    public CondenserCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, AEBlocks.CONDENSER.stack());

        ResourceLocation location = new ResourceLocation(AppEng.MOD_ID, "textures/guis/condenser.png");
        this.background = guiHelper.createDrawable(location, 50, 25, 94, 48);

        // This is shown on the "input slot" for condenser operations to indicate that any item can be used
        this.iconTrash = new IconDrawable(Icon.BACKGROUND_TRASH, 1, 27);
        this.iconButton = new IconDrawable(Icon.TOOLBAR_BUTTON_BACKGROUND, 78, 26);

        IDrawableStatic progressDrawable = guiHelper.drawableBuilder(location, 178, 25, 6, 18).addPadding(0, 0, 70, 0)
                .build();
        this.progress = guiHelper.createAnimatedDrawable(progressDrawable, 40, IDrawableAnimated.StartDirection.BOTTOM,
                false);

        this.buttonIcons = new EnumMap<>(CondenserOutput.class);

        this.buttonIcons.put(CondenserOutput.MATTER_BALLS,
                new IconDrawable(Icon.CONDENSER_OUTPUT_MATTER_BALL, 78, 26));
        this.buttonIcons.put(CondenserOutput.SINGULARITY,
                new IconDrawable(Icon.CONDENSER_OUTPUT_SINGULARITY, 78, 26));
    }

    private ItemStack getOutput(CondenserOutput recipe) {
        return switch (recipe) {
            case MATTER_BALLS -> AEItems.MATTER_BALL.stack();
            case SINGULARITY -> AEItems.SINGULARITY.stack();
            default -> ItemStack.EMPTY;
        };
    }

    @Override
    public RecipeType<CondenserOutput> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return new TranslatableComponent(TITLE_TRANSLATION_KEY);
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(CondenserOutput recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX,
            double mouseY) {
        this.progress.draw(stack);
        this.iconTrash.draw(stack);
        this.iconButton.draw(stack);

        var buttonIcon = this.buttonIcons.get(recipe);
        if (buttonIcon != null) {
            buttonIcon.draw(stack);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CondenserOutput recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 27)
                .setSlotName("output")
                .addItemStack(getOutput(recipe));

        // Get all storage cells and cycle them through a catalyst slot
        builder.addSlot(RecipeIngredientRole.CATALYST, 51, 1)
                .setSlotName("storage_cell")
                .addItemStacks(getViableStorageComponents(recipe));
    }

    private List<ItemStack> getViableStorageComponents(CondenserOutput condenserOutput) {
        List<ItemStack> viableComponents = new ArrayList<>();
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_1K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_4K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_16K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_64K.stack());
        this.addViableComponent(condenserOutput, viableComponents, AEItems.CELL_COMPONENT_256K.stack());
        return viableComponents;
    }

    private void addViableComponent(CondenserOutput condenserOutput, List<ItemStack> viableComponents,
            ItemStack itemStack) {
        IStorageComponent comp = (IStorageComponent) itemStack.getItem();
        int storage = comp.getBytes(itemStack) * CondenserBlockEntity.BYTE_MULTIPLIER;
        if (storage >= condenserOutput.requiredPower) {
            viableComponents.add(itemStack);
        }
    }

    @Override
    public List<Component> getTooltipStrings(CondenserOutput recipe, IRecipeSlotsView recipeSlotsView, double mouseX,
            double mouseY) {

        if (mouseX >= 28 && mouseX < 28 + 16 && mouseY >= 78 && mouseY < 78 + 16) {
            String key;

            switch (recipe) {
                case MATTER_BALLS:
                    key = "gui.tooltips.appliedenergistics2.MatterBalls";
                    break;
                case SINGULARITY:
                    key = "gui.tooltips.appliedenergistics2.Singularity";
                    break;
                default:
                    return Collections.emptyList();
            }

            return Lists.newArrayList(new TranslatableComponent(key));
        }
        return Collections.emptyList();
    }

    @Override
    public ResourceLocation getUid() {
        return getRecipeType().getUid();
    }

    @Override
    public Class<? extends CondenserOutput> getRecipeClass() {
        return getRecipeType().getRecipeClass();
    }
}
