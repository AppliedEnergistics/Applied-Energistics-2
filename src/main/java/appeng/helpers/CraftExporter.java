package appeng.helpers;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;

import appeng.core.AELog;
import appeng.core.localization.PlayerMessages;

public class CraftExporter {
    public static void exportCraft(JsonObject jso, LocalPlayer player, ExportType type) {
        String date = DateTimeFormatter.ofPattern("yyy-MM-dd.mm.ss").format(LocalDateTime.now());
        var file = new File(Minecraft.getInstance().gameDirectory + "/ae2/exports/" + type.type + date + ".json");
        file.getParentFile().mkdirs();
        try {
            var writer = new FileWriter(file);
            writer.write(new GsonBuilder().setPrettyPrinting().create().toJson(jso));
            writer.close();
        } catch (IOException e) {
            AELog.error("Failed to export crafting status", e);
        }
        var component = Component.literal(file.getName()).withStyle(ChatFormatting.UNDERLINE).withStyle(
                (s) -> s.withClickEvent(new ClickEvent(ClickEvent.Action.OPEN_FILE, file.getAbsolutePath())));
        player.displayClientMessage(PlayerMessages.CraftExported.text(component), false);
    }

    public enum ExportType {
        CRAFTING_STATUS("status_"),
        CRAFTING_PLAN("plan_");

        public final String type;

        ExportType(String type) {
            this.type = type;
        }
    }
}
