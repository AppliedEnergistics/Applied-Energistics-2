package appeng.datagen.providers.localization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
import appeng.core.localization.InGameTooltip;
import appeng.core.localization.ItemModText;
import appeng.core.localization.LocalizationEnum;
import appeng.core.localization.PlayerMessages;
import appeng.datagen.providers.IAE2DataProvider;

public class LocalizationProvider implements IAE2DataProvider {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private final Map<String, String> localizations = new HashMap<>();

    private final DataGenerator generator;

    private boolean wasSaved = false;

    public LocalizationProvider(DataGenerator generator) {
        this.generator = generator;
    }

    @Override
    public final void run(HashCache cache) {
        for (var block : AEBlocks.getBlocks()) {
            add("block.ae2." + block.id().getPath(), block.getEnglishName());
        }
        for (var item : AEItems.getItems()) {
            add("item.ae2." + item.id().getPath(), item.getEnglishName());
        }
        for (var entry : AEEntities.ENTITY_ENGLISH_NAMES.entrySet()) {
            add("entity.ae2." + entry.getKey(), entry.getValue());
        }

        addEnum(GuiText.class);
        addEnum(ButtonToolTips.class);
        addEnum(PlayerMessages.class);
        addEnum(InGameTooltip.class);
        addEnum(ItemModText.class);

        generateLocalizations();

        save(cache, localizations);
    }

    public <T extends Enum<T> & LocalizationEnum> void addEnum(Class<T> localizedEnum) {
        for (var enumConstant : localizedEnum.getEnumConstants()) {
            add(enumConstant.getTranslationKey(), enumConstant.getEnglishText());
        }
    }

    public Component component(String key, String text) {
        add(key, text);
        return new TranslatableComponent(key);
    }

    public void add(String key, String text) {
        Preconditions.checkState(!wasSaved, "Cannot add more translations after they were already saved");
        var previous = localizations.put(key, text);
        if (previous != null) {
            throw new IllegalStateException("Localization key " + key + " is already translated to: " + previous);
        }
    }

    private void generateLocalizations() {
        add("ae2.permission_denied", "You lack permission to access this.");
        add("biome.ae2.spatial_storage", "Spatial Storage");
        add("commands.ae2.ChunkLoggerOff", "Chunk Logging is now off");
        add("commands.ae2.ChunkLoggerOn", "Chunk Logging is now on");
        add("commands.ae2.permissions", "You do not have adequate permissions to run this command.");
        add("commands.ae2.usage",
                "Commands provided by Applied Energistics 2 - use /ae2 list for a list, and /ae2 help _____ for help with a command.");
        add("gui.ae2.PatternEncoding.primary_processing_result_hint",
                "Can be requested through the automated crafting system.");
        add("gui.ae2.PatternEncoding.primary_processing_result_tooltip", "Primary Processing Result");
        add("gui.ae2.PatternEncoding.secondary_processing_result_hint",
                "Can not be directly requested through the automated crafting system, but will be used before stored items in multi-step recipes.");
        add("gui.ae2.PatternEncoding.secondary_processing_result_tooltip", "Secondary Processing Result");
        add("gui.ae2.security.build.name", "Build");
        add("gui.ae2.security.build.tip",
                "User can modify the physical structure of the network, and make configuration changes.");
        add("gui.ae2.security.craft.name", "Craft");
        add("gui.ae2.security.craft.tip", "User can initiate new crafting jobs.");
        add("gui.ae2.security.extract.name", "Withdraw");
        add("gui.ae2.security.extract.tip", "User is allowed to remove items from storage.");
        add("gui.ae2.security.inject.name", "Deposit");
        add("gui.ae2.security.inject.tip", "User is allowed to store new items into storage.");
        add("gui.ae2.security.security.name", "Security");
        add("gui.ae2.security.security.tip", "User can access and modify the security terminal of the network.");
        add("gui.ae2.units.appliedenergstics", "AE");
        add("gui.ae2.units.tr", "E");
        add("itemGroup.ae2.facades", "Applied Energistics 2 - Facades");
        add("itemGroup.ae2.main", "Applied Energistics 2");
        add("key.ae2.category", "Applied Energistics 2");
        add("key.ae2.portable_fluid_cell", "Open Portable Fluid Cell");
        add("key.ae2.portable_item_cell", "Open Portable Item Cell");
        add("key.ae2.wireless_terminal", "Open Wireless Terminal");
        add("key.toggle_focus.desc", "Toggle search box focus");
        add("stat.ae2.items_extracted", "Items extracted from ME Storage");
        add("stat.ae2.items_inserted", "Items added to ME Storage");
        add("theoneprobe.ae2.channels", "%1$d Channels");
        add("theoneprobe.ae2.channels_of", "%1$d of %2$d Channels");
        add("theoneprobe.ae2.contains", "Contains");
        add("theoneprobe.ae2.crafting", "Crafting: %1$s");
        add("theoneprobe.ae2.device_missing_channel", "Device Missing Channel");
        add("theoneprobe.ae2.device_offline", "Device Offline");
        add("theoneprobe.ae2.device_online", "Device Online");
        add("theoneprobe.ae2.locked", "Locked");
        add("theoneprobe.ae2.nested_p2p_tunnel", "Error: Nested P2P Tunnel");
        add("theoneprobe.ae2.p2p_frequency", "Frequency: %1$s");
        add("theoneprobe.ae2.p2p_input_many_outputs", "Linked (Input Side) - %d Outputs");
        add("theoneprobe.ae2.p2p_input_one_output", "Linked (Input Side)");
        add("theoneprobe.ae2.p2p_output", "Linked (Output Side)");
        add("theoneprobe.ae2.p2p_unlinked", "Unlinked");
        add("theoneprobe.ae2.showing", "Showing");
        add("theoneprobe.ae2.stored_energy", "%1$d / %2$d");
        add("theoneprobe.ae2.unlocked", "Unlocked");
    }

    private void save(HashCache cache, Map<String, String> localizations) {
        wasSaved = true;

        try {
            var path = this.generator.getOutputFolder().resolve("assets/ae2/lang/en_us.json");

            // Dump the translation in ascending order
            var sorted = new TreeMap<>(localizations);
            var jsonLocalization = new JsonObject();
            for (var entry : sorted.entrySet()) {
                jsonLocalization.addProperty(entry.getKey(), entry.getValue());
            }

            DataProvider.save(GSON, cache, jsonLocalization, path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getName() {
        return "Localization (en_us)";
    }
}
