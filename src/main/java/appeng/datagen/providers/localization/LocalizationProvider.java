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
import net.minecraft.network.chat.TextComponent;

import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEEntities;
import appeng.core.definitions.AEItems;
import appeng.core.localization.ButtonToolTips;
import appeng.core.localization.GuiText;
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
        for (var text : GuiText.values()) {
            add("gui.ae2." + text.name(), text.getEnglishText());
        }
        for (var text : ButtonToolTips.values()) {
            add("gui.tooltips.ae2." + text.name(), text.getEnglishText());
        }
        for (var text : PlayerMessages.values()) {
            add(text.getTranslationKey(), text.getEnglishText());
        }

        for (var entry : AEEntities.ENTITY_ENGLISH_NAMES.entrySet()) {
            add("entity.ae2." + entry.getKey(), entry.getValue());
        }

        generateLocalizations();

        save(cache, localizations);
    }

    public TextComponent component(String key, String text) {
        add(key, text);
        return new TextComponent(key);
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
        add("gui.ae2.validation.InvalidNumber", "Please enter a number");
        add("gui.ae2.validation.NumberGreaterThanMaxValue", "Please enter a number less than or equal to %d");
        add("gui.ae2.validation.NumberLessThanMinValue", "Please enter a number greater than or equal to %d");
        add("itemGroup.ae2.facades", "Applied Energistics 2 - Facades");
        add("itemGroup.ae2.main", "Applied Energistics 2");
        add("jei.ae2.missing_id", "Cannot identify recipe");
        add("jei.ae2.missing_items", "Missing items will be skipped");
        add("jei.ae2.no_output", "Recipe has no output");
        add("jei.ae2.recipe_too_large", "Recipe larger than 3x3");
        add("jei.ae2.requires_processing_mode", "Requires processing mode");
        add("key.ae2.category", "Applied Energistics 2");
        add("key.toggle_focus.desc", "Toggle search box focus");
        add("rei.ae2.throwing_in_water_category", "Throwing In Water");
        add("rei.ae2.with_crystal_growth_accelerators", "With Crystal Growth Accelerators:");
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
        add("waila.ae2.Channels", "%1$d Channels");
        add("waila.ae2.ChannelsOf", "%1$d of %2$d Channels");
        add("waila.ae2.Charged", "%d%% charged");
        add("waila.ae2.Contains", "Contains: %s");
        add("waila.ae2.Crafting", "Crafting: %s");
        add("waila.ae2.DeviceMissingChannel", "Device Missing Channel");
        add("waila.ae2.DeviceOffline", "Device Offline");
        add("waila.ae2.DeviceOnline", "Device Online");
        add("waila.ae2.ErrorControllerConflict", "Error: Controller Conflict");
        add("waila.ae2.ErrorNestedP2PTunnel", "Error: Nested P2P Tunnel");
        add("waila.ae2.ErrorTooManyChannels", "Error: Too Many Channels");
        add("waila.ae2.Locked", "Locked");
        add("waila.ae2.NetworkBooting", "Network Booting");
        add("waila.ae2.P2PInputManyOutputs", "Linked (Input Side) - %d Outputs");
        add("waila.ae2.P2PInputOneOutput", "Linked (Input Side)");
        add("waila.ae2.P2POutput", "Linked (Output Side)");
        add("waila.ae2.P2PUnlinked", "Unlinked");
        add("waila.ae2.Showing", "Showing");
        add("waila.ae2.Stored", "Stored: %s / %s");
        add("waila.ae2.Unlocked", "Unlocked");
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
