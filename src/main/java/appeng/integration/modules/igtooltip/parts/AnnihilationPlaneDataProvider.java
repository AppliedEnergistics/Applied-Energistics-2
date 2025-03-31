package appeng.integration.modules.igtooltip.parts;

import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.Enchantment;

import appeng.api.integrations.igtooltip.TooltipBuilder;
import appeng.api.integrations.igtooltip.TooltipContext;
import appeng.api.integrations.igtooltip.providers.BodyProvider;
import appeng.api.integrations.igtooltip.providers.ServerDataProvider;
import appeng.core.localization.InGameTooltip;
import appeng.parts.automation.AnnihilationPlanePart;

public class AnnihilationPlaneDataProvider
        implements BodyProvider<AnnihilationPlanePart>, ServerDataProvider<AnnihilationPlanePart> {
    private static final String TAG_ENCHANTMENTS = "planeEnchantments";

    @Override
    public void buildTooltip(AnnihilationPlanePart plane, TooltipContext context, TooltipBuilder tooltip) {
        var serverData = context.serverData();
        if (serverData.contains(TAG_ENCHANTMENTS)) {
            tooltip.addLine(InGameTooltip.EnchantedWith.text());

            var enchantments = serverData.getCompoundOrEmpty(TAG_ENCHANTMENTS);
            var enchantmentRegistry = context.registries().lookupOrThrow(Registries.ENCHANTMENT);
            for (var enchantmentId : enchantments.keySet()) {
                var enchantment = enchantmentRegistry.get(ResourceKey.create(
                        Registries.ENCHANTMENT,
                        ResourceLocation.parse(enchantmentId)));
                var level = enchantments.getIntOr(enchantmentId, 0);
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
