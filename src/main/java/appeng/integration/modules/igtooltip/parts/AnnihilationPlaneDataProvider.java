package appeng.integration.modules.igtooltip.parts;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class AnnihilationPlaneDataProvider
        implements BodyProvider<AnnihilationPlanePart>, ServerDataProvider<AnnihilationPlanePart> {
    private static final String TAG_ENCHANTMENTS = "planeEnchantments";

    @Override
    public void buildTooltip(AnnihilationPlanePart plane, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();
        if (serverData.contains(TAG_ENCHANTMENTS, Tag.TAG_COMPOUND)) {
            tooltip.addLine(InGameTooltip.EnchantedWith.text());

            var enchantments = serverData.getCompound(TAG_ENCHANTMENTS);
            var enchantmentRegistry = context.registries().lookupOrThrow(Registries.ENCHANTMENT);
            for (var enchantmentId : enchantments.getAllKeys()) {
                var enchantment = enchantmentRegistry.get(ResourceKey.create(
                        Registries.ENCHANTMENT,
                        ResourceLocation.parse(enchantmentId)
                ));
                var level = enchantments.getInt(enchantmentId);
                enchantment.ifPresent(holder -> {
                    tooltip.addLine(Enchantment.getFullname(holder, level));
                });
            }
        }
    }

    @Override
    public void provideServerData(Player player, AnnihilationPlanePart plane, CompoundTag serverData) {
        var enchantments = plane.getEnchantments();
        if (enchantments != null && !enchantments.isEmpty()) {
            var enchantmentsTag = new CompoundTag();
            for (var entry : enchantments.entrySet()) {
                enchantmentsTag.putInt(entry.getKey().getRegisteredName(), entry.getIntValue());
            }
            serverData.put(TAG_ENCHANTMENTS, enchantmentsTag);
        }
    }
}
