package appeng.container.implementations;

import appeng.api.storage.ITerminalHost;
import appeng.container.slot.OptionalSlotFake;
import appeng.container.slot.SlotFakeCraftingMatrix;
import appeng.container.slot.SlotPatternOutputs;
import appeng.container.slot.SlotRestrictedInput;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraftforge.items.IItemHandler;

import static appeng.helpers.PatternHelper.PROCESSING_INPUT_LIMIT;
import static appeng.helpers.PatternHelper.PROCESSING_OUTPUT_LIMIT;


public class ContainerExpandedProcessingPatternTerm extends ContainerPatternEncoder {

    public ContainerExpandedProcessingPatternTerm(InventoryPlayer ip, ITerminalHost monitorable) {
        super(ip, monitorable, false);

        this.craftingSlots = new SlotFakeCraftingMatrix[PROCESSING_INPUT_LIMIT];
        this.outputSlots = new OptionalSlotFake[PROCESSING_OUTPUT_LIMIT];

        final IItemHandler patternInv = this.getPatternTerminal().getInventoryByName("pattern");
        final IItemHandler output = this.getPatternTerminal().getInventoryByName("output");

        this.crafting = this.getPatternTerminal().getInventoryByName("crafting");

        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                this.addSlotToContainer(this.craftingSlots[x + y * 4] = new SlotFakeCraftingMatrix(this.crafting, x + y * 4, 4 + x * 18, -85 + y * 18));
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 2; x++) {
                this.addSlotToContainer(this.outputSlots[x + y * 2] = new SlotPatternOutputs(output, this, x + y * 2, 96 + x * 18, -76 + y * 18, 0, 0, 1));
                this.outputSlots[x + y * 2].setRenderDisabled(false);
                this.outputSlots[x + y * 2].setIIcon(-1);
            }
        }

        this.addSlotToContainer(this.patternSlotIN = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.BLANK_PATTERN, patternInv, 0, 147, -72 - 9, this.getInventoryPlayer()));
        this.addSlotToContainer(this.patternSlotOUT = new SlotRestrictedInput(SlotRestrictedInput.PlacableItemType.ENCODED_PATTERN, patternInv, 1, 147, -72 + 34, this.getInventoryPlayer()));

        this.patternSlotOUT.setStackLimit(1);

        this.bindPlayerInventory(ip, 0, 0);

    }

    @Override
    public boolean isSlotEnabled(final int idx) {
        return true;
    }

    @Override
    public boolean isCraftingMode() {
        return false;
    }
}
