package appeng.client.guidebook;

import java.util.Map;
import java.util.stream.Collectors;

import net.minecraft.resources.Identifier;

import guideme.Guide;
import guideme.siteexport.AdditionalResourceExporter;
import guideme.siteexport.ResourceExporter;

import appeng.core.AppEng;

public class AEAdditionalExportData implements AdditionalResourceExporter {
    private static final Identifier DEFAULT_CONFIG_VALUES = AppEng.makeId("default-config-values");

    @Override
    public void addResources(Guide guide, ResourceExporter exporter) {

        // TODO public List<P2PTypeInfo> p2pTunnelTypes = new ArrayList<>();

        // TODO public Map<String, Map<DyeColor, String>> coloredVersions = new HashMap<>();

        var defaultConfigValues = ConfigValueTagExtension.CONFIG_VALUES.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().get()));
        exporter.addExtraData(DEFAULT_CONFIG_VALUES, defaultConfigValues);
    }
}
