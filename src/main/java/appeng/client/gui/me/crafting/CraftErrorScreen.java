package appeng.client.gui.me.crafting;

import java.util.ArrayList;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;

import appeng.api.networking.crafting.CraftingSubmitErrorCode;
import appeng.api.networking.crafting.UnsuitableCpus;
import appeng.api.stacks.GenericStack;
import appeng.client.gui.AESubScreen;
import appeng.client.gui.me.common.ClientDisplaySlot;
import appeng.core.localization.GuiText;
import appeng.menu.SlotSemantics;
import appeng.menu.me.crafting.CraftConfirmMenu;

/**
 * Shows detailed error information about a failed attempt at submitting a crafting job.
 */
public class CraftErrorScreen extends AESubScreen<CraftConfirmMenu, CraftConfirmScreen> {
    public CraftErrorScreen(CraftConfirmScreen parent, CraftingSubmitErrorCode errorCode, Object details) {
        super(parent, "/screens/craft_error.json");

        var errorText = switch (errorCode) {
            case INCOMPLETE_PLAN -> GuiText.CraftErrorIncompletePlan.text();
            case NO_CPU_FOUND -> GuiText.CraftErrorNoCpuFound.text();
            case NO_SUITABLE_CPU_FOUND -> {
                MutableComponent text = GuiText.CraftErrorNoSuitableCpu.text();
                if (details instanceof UnsuitableCpus unsuitableCpus) {
                    var stats = new ArrayList<Component>();
                    if (unsuitableCpus.offline() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuOffline.text(unsuitableCpus.offline()));
                    }
                    if (unsuitableCpus.busy() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuBusy.text(unsuitableCpus.busy()));
                    }
                    if (unsuitableCpus.tooSmall() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuTooSmall.text(unsuitableCpus.tooSmall()));
                    }
                    if (unsuitableCpus.excluded() > 0) {
                        stats.add(GuiText.CraftErrorNoSuitableCpuExcluded.text(unsuitableCpus.excluded()));
                    }

                    MutableComponent suffix = new TextComponent("(");
                    for (int i = 0; i < stats.size(); i++) {
                        var stat = stats.get(i);
                        if (i != 0) {
                            suffix = suffix.append(", ");
                        }
                        suffix = suffix.append(stat);
                    }
                    suffix = suffix.append(")");
                    text = text.append(" ").append(suffix);
                }

                yield text;
            }
            case CPU_BUSY -> GuiText.CraftErrorCpuBusy.text();
            case CPU_OFFLINE -> GuiText.CraftErrorCpuOffline.text();
            case CPU_TOO_SMALL -> GuiText.CraftErrorCpuTooSmall.text();
            case MISSING_INGREDIENT -> {
                if (details instanceof GenericStack genericStack) {
                    addClientSideSlot(new ClientDisplaySlot(genericStack), SlotSemantics.MISSING_INGREDIENT);
                }

                yield GuiText.CraftErrorMissingIngredient.text();
            }
        };

        setTextContent("errorText", errorText);

        widgets.addButton("replan", GuiText.CraftErrorReplan.text(), () -> {
            returnToParent();
            menu.replan();
        });
        widgets.addButton("retry", GuiText.CraftErrorRetry.text(), () -> {
            returnToParent();
            menu.startJob();
        });
        widgets.addButton("cancel", GuiText.Cancel.text(), () -> {
            returnToParent();
            menu.goBack();
        });
    }

    @Override
    protected void onReturnToParent() {
        // Ensure we do not immediately return to this screen for the same error
        menu.clearError();
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
    }
}
