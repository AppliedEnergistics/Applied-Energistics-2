package appeng.items.misc;

import java.util.function.Consumer;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypesInternal;
import appeng.api.stacks.GenericStack;

public class MissingContentItem extends Item {
    public MissingContentItem(Properties properties) {
        super(properties);
    }

    @Nullable
    public BrokenStackInfo getBrokenStackInfo(ItemStack stack) {
        // This is a broken pattern entry, try to recover the ID and use it as the tooltip line
        var itemStackData = stack.get(AEComponents.MISSING_CONTENT_ITEMSTACK_DATA);
        var genericStackData = stack.get(AEComponents.MISSING_CONTENT_AEKEY_DATA);

        // "id" is just the most common ID field for key types
        if (itemStackData != null && itemStackData.contains("id")) {
            var brokenDataTag = itemStackData.copyTag();
            if (!brokenDataTag.contains("id")) {
                return null; // Without any ID this info is worthless
            }
            var missingId = brokenDataTag.getStringOr("id", "");

            long amount;
            try {
                amount = Math.max(1, brokenDataTag.getLongOr("count", 0));
            } catch (Exception ignored) {
                amount = 1;
            }

            return new BrokenStackInfo(Component.literal(missingId), AEKeyType.items(), amount);
        } else if (genericStackData != null && genericStackData.contains("id")) {
            var brokenDataTag = genericStackData.copyTag();
            if (!brokenDataTag.contains("id")) {
                return null; // Without any ID this info is worthless
            }
            var missingId = Component.literal(brokenDataTag.getStringOr("id", ""));

            // Try figuring out which AEKeyType it may have been. If that fails just default to item
            AEKeyType keyType = null;
            try {
                var keyTypeString = brokenDataTag.getStringOr(AEKey.TYPE_FIELD, "");
                keyType = AEKeyTypesInternal.getRegistry().getValue(ResourceLocation.parse(keyTypeString));
                if (keyType == null) {
                    missingId.append(" (").append(keyTypeString).append(")");
                }
            } catch (Exception ignored) {
            }

            long amount;
            try {
                amount = Math.max(1, brokenDataTag.getLongOr(GenericStack.AMOUNT_FIELD, 0));
            } catch (Exception ignored) {
                amount = 1;
            }

            return new BrokenStackInfo(missingId, keyType, amount);
        }

        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay tooltipDisplay,
            Consumer<Component> lines, TooltipFlag advanced) {
        super.appendHoverText(stack, context, tooltipDisplay, lines, advanced);

        var error = stack.get(AEComponents.MISSING_CONTENT_ERROR);
        if (error != null) {
            lines.accept(Component.literal(error).withStyle(ChatFormatting.GRAY));
        }
    }

    public record BrokenStackInfo(Component displayName, @Nullable AEKeyType keyType, long amount) {
    }
}
