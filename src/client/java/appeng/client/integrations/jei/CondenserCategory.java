package appeng.client.integrations.jei;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;

import appeng.api.config.CondenserOutput;
import appeng.api.implementations.items.IStorageComponent;
import appeng.blockentity.misc.CondenserBlockEntity;
import appeng.core.AppEng;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;
import appeng.util.Icon;

class CondenserCategory implements IRecipeCategory<CondenserOutput> {

    private static final String TITLE_TRANSLATION_KEY = "block.ae2.condenser";

    public static final IRecipeType<CondenserOutput> RECIPE_TYPE = IRecipeType.create(AppEng.MOD_ID, "condenser",
            CondenserOutput.class);

    private final IDrawable arrow;
    private final IDrawable progress;
    private final IDrawableAnimated progressOverlay;

    private final IDrawable iconButton;
    private final IDrawable iconTrash;
    private final IDrawable icon;

    private final Map<CondenserOutput, IDrawable> buttonIcons;

    public CondenserCategory(IGuiHelper guiHelper) {
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, AEBlocks.CONDENSER.stack());

        this.arrow = guiHelper.getRecipeArrow();

        // This is shown on the "input slot" for condenser operations to indicate that any item can be used
        this.iconTrash = new IconDrawable(Icon.BACKGROUND_TRASH);
        this.iconButton = new IconDrawable(Icon.TOOLBAR_BUTTON_BACKGROUND);

        Identifier location = AppEng.makeId("textures/guis/jei.png");
        this.progress = guiHelper.createDrawable(location, 72, 0, 6, 18);
        var progressDrawable = guiHelper.drawableBuilder(location, 78, 0, 6, 18).build();
        this.progressOverlay = guiHelper.createAnimatedDrawable(progressDrawable, 40,
                IDrawableAnimated.StartDirection.BOTTOM,
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
    public IRecipeType<CondenserOutput> getRecipeType() {
        return RECIPE_TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable(TITLE_TRANSLATION_KEY);
    }

    @Override
    public int getWidth() {
        return 94;
    }

    @Override
    public int getHeight() {
        return 48;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void draw(CondenserOutput recipe, IRecipeSlotsView recipeSlotsView, GuiGraphicsExtractor guiGraphics,
            double mouseX,
            double mouseY) {
        this.progress.draw(guiGraphics, 70, 0);
        this.progressOverlay.draw(guiGraphics, 70, 0);
        this.iconTrash.draw(guiGraphics, 1, 27);
        this.iconButton.draw(guiGraphics, 78, 26);
        this.arrow.draw(guiGraphics, 23, 27);

        var buttonIcon = this.buttonIcons.get(recipe);
        if (buttonIcon != null) {
            buttonIcon.draw(guiGraphics, 1, 1);
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CondenserOutput recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 1, 27)
                .setSlotName("trash")
                .setStandardSlotBackground()
                .setOverlay(iconTrash, 0, 0);

        builder.addSlot(RecipeIngredientRole.OUTPUT, 55, 27)
                .setSlotName("output")
                .setOutputSlotBackground()
                .add(getOutput(recipe));

        // Get all storage cells and cycle them through a catalyst slot
        builder.addSlot(RecipeIngredientRole.CRAFTING_STATION, 51, 1)
                .setSlotName("storage_cell")
                .setStandardSlotBackground()
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
    public void getTooltip(ITooltipBuilder tooltip, CondenserOutput recipe, IRecipeSlotsView recipeSlotsView,
            double mouseX, double mouseY) {
        if (mouseX >= 78 && mouseX < 78 + 16 && mouseY >= 28 && mouseY < 28 + 16) {
            Component key;

            switch (recipe) {
                case MATTER_BALLS:
                    key = ButtonToolTips.MatterBalls.text(CondenserOutput.MATTER_BALLS.requiredPower);
                    break;
                case SINGULARITY:
                    key = ButtonToolTips.Singularity.text(CondenserOutput.SINGULARITY.requiredPower);
                    break;
                default:
                    return;
            }

            var lines = Minecraft.getInstance().font.getSplitter().splitLines(key, Integer.MAX_VALUE, key.getStyle());
            tooltip.addAll(lines);
        }
    }

}
