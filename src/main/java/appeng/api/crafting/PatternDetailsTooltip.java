package appeng.api.crafting;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.network.chat.Component;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.core.localization.GuiText;

/**
 * Properties shown in the tooltip of an encoded pattern. Used for both valid and invalid encoded patterns. For invalid
 * patterns, only partial information might be given.
 */
public class PatternDetailsTooltip {
    /**
     * The text to use when the pattern uses Vanilla crafting as its method of producing the item. Usually reserved for
     * patterns used in molecular assemblers.
     */
    public static final Component OUTPUT_TEXT_CRAFTS = GuiText.Crafts.text();

    /**
     * The text to use when the pattern uses some other form of processing to produce the output.
     */
    public static final Component OUTPUT_TEXT_PRODUCES = GuiText.Produces.text();

    private Component outputMethod;

    private final List<Property> additionalProperties = new ArrayList<>();

    private final List<Entry> inputs = new ArrayList<>();

    private final List<Entry> outputs = new ArrayList<>();

    /**
     * @param outputMethod The method of producing the outputs of this pattern ({@link #OUTPUT_TEXT_CRAFTS} or
     *                     {@link #OUTPUT_TEXT_PRODUCES}). Usually this will depend on the type of pattern and not so
     *                     much the individual NBT data.
     */
    public PatternDetailsTooltip(Component outputMethod) {
        setOutputMethod(outputMethod);
    }

    /**
     * @param outputMethod The method of producing the outputs of this pattern ({@link #OUTPUT_TEXT_CRAFTS} or
     *                     {@link #OUTPUT_TEXT_PRODUCES}). Usually this will depend on the type of pattern and not so
     *                     much the individual NBT data.
     */
    public void setOutputMethod(Component outputMethod) {
        this.outputMethod = Objects.requireNonNull(outputMethod, "outputMethod");
    }

    public List<Property> getProperties() {
        return additionalProperties;
    }

    public List<Entry> getInputs() {
        return inputs;
    }

    public List<Entry> getOutputs() {
        return outputs;
    }

    public void addInput(Entry entry) {
        inputs.add(entry);
    }

    public void addInput(AEKey what, long amount) {
        inputs.add(new ValidEntry(what, amount));
    }

    public void addInvalidInput(Component description) {
        inputs.add(new InvalidEntry(description, null, 0));
    }

    public void addInvalidInput(Component description, long amount) {
        inputs.add(new InvalidEntry(description, null, amount));
    }

    /**
     * Adds an invalid entry to this tooltip, for which the amount and the {@linkplain AEKeyType type} of ingredient was
     * known. The type is used to format the amount appropriate for the given type (i.e. shown as Buckets for fluids).
     */
    public void addInvalidInput(Component description, AEKeyType type, long amount) {
        inputs.add(new InvalidEntry(description, type, amount));
    }

    public void addOutput(AEKey what, long amount) {
        outputs.add(new ValidEntry(what, amount));
    }

    public void addOutput(Entry entry) {
        outputs.add(entry);
    }

    public void addInvalidOutput(Component description) {
        outputs.add(new InvalidEntry(description, null, 0));
    }

    public void addInvalidOutput(Component description, long amount) {
        outputs.add(new InvalidEntry(description, null, amount));
    }

    /**
     * Adds an invalid entry to this tooltip, for which the amount and the {@linkplain AEKeyType type} of ingredient was
     * known. The type is used to format the amount appropriate for the given type (i.e. shown as Buckets for fluids).
     */
    public void addInvalidOutput(Component description, AEKeyType type, long amount) {
        outputs.add(new InvalidEntry(description, type, amount));
    }

    public void addProperty(Component name, Component value) {
        this.additionalProperties.add(new Property(name, value));
    }

    public void addProperty(Component description) {
        this.additionalProperties.add(new Property(description, null));
    }

    public void addInputsAndOutputs(IPatternDetails details) {
        for (var input : details.getInputs()) {
            if (input == null) {
                continue;
            }

            addInput(input.getPossibleInputs()[0].what(),
                    input.getPossibleInputs()[0].amount() * input.getMultiplier());
        }

        for (var output : details.getOutputs()) {
            if (output == null) {
                continue;
            }

            addOutput(output.what(), output.amount());
        }
    }

    /**
     * An additional property to display on the patterns tooltip, such as whether substitutions of items was enabled or
     * not.
     */
    public record Property(Component name, @Nullable Component value) {
    }

    public Component getOutputMethod() {
        return outputMethod;
    }

    public sealed interface Entry permits ValidEntry,InvalidEntry {
    }

    public record ValidEntry(AEKey what, long amount) implements Entry {
        public ValidEntry(GenericStack stack) {
            this(stack.what(), stack.amount());
        }
    }

    public record InvalidEntry(
            Component name,
            @Nullable AEKeyType type,
            long amount) implements Entry {
    }
}
