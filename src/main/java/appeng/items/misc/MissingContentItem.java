package appeng.items.misc;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

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
            var brokenDataTag = itemStackData.getUnsafe();
            if (!brokenDataTag.contains("id", Tag.TAG_STRING)) {
                return null; // Without any ID this info is worthless
            }
            var missingId = brokenDataTag.getString("id");

            long amount;
            try {
                amount = Math.max(1, brokenDataTag.getLong("count"));
            } catch (Exception ignored) {
                amount = 1;
            }

            return new BrokenStackInfo(Component.literal(missingId), AEKeyType.items(), amount);
        } else if (genericStackData != null && genericStackData.contains("id")) {
            var brokenDataTag = genericStackData.getUnsafe();
            if (!brokenDataTag.contains("id", Tag.TAG_STRING)) {
                return null; // Without any ID this info is worthless
            }
            var missingId = Component.literal(brokenDataTag.getString("id"));

            // Try figuring out which AEKeyType it may have been. If that fails just default to item
            AEKeyType keyType = null;
            try {
                var keyTypeString = brokenDataTag.getString(AEKey.TYPE_FIELD);
                keyType = AEKeyTypesInternal.getRegistry().get(ResourceLocation.parse(keyTypeString));
                if (keyType == null) {
                    missingId.append(" (").append(keyTypeString).append(")");
                }
            } catch (Exception ignored) {
            }

            long amount;
            try {
                amount = Math.max(1, brokenDataTag.getLong(GenericStack.AMOUNT_FIELD));
            } catch (Exception ignored) {
                amount = 1;
            }

            return new BrokenStackInfo(missingId, keyType, amount);
        }

        return null;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag advanced) {
        super.appendHoverText(stack, context, lines, advanced);

        var error = stack.get(AEComponents.MISSING_CONTENT_ERROR);
        if (error != null) {
            lines.add(Component.literal(error).withStyle(ChatFormatting.GRAY));
        }
    }

    public record BrokenStackInfo(Component displayName, @Nullable AEKeyType keyType, long amount) {
    }
}
