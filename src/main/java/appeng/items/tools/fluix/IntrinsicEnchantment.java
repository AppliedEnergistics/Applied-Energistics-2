package appeng.items.tools.fluix;

import java.util.function.Consumer;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.enchantment.Enchantment;

import appeng.core.localization.GuiText;

final class IntrinsicEnchantment {
    private final ResourceKey<Enchantment> enchantment;
    private final int level;

    public IntrinsicEnchantment(ResourceKey<Enchantment> enchantment, int level) {
        this.enchantment = enchantment;
        this.level = level;
    }

    public void appendHoverText(Item.TooltipContext context, Consumer<Component> tooltipComponents) {
        var registries = context.registries();
        if (registries == null) {
            return;
        }

        var registrylookup = registries.lookupOrThrow(Registries.ENCHANTMENT);
        registrylookup.get(enchantment).ifPresent(holder -> {
            tooltipComponents.accept(GuiText.IntrinsicEnchant.text(Enchantment.getFullname(holder, level)));
        });
    }

    public int getLevel(Holder<Enchantment> enchantment) {
        return enchantment.is(this.enchantment) ? level : 0;
    }
}
