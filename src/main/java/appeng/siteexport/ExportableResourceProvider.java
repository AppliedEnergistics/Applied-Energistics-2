package appeng.siteexport;

import appeng.client.guidebook.document.block.LytNode;
import appeng.client.guidebook.document.block.LytVisitor;
import appeng.client.guidebook.document.flow.LytFlowContent;

/**
 * Parts of the document tree should implement this interface to export resources for being shown in the guidebook *Web*
 * application. This is used to export item descriptions, item icons, etc.
 */
public interface ExportableResourceProvider {
    void exportResources(ResourceExporter exporter);

    static void visit(LytNode node, ResourceExporter exporter) {
        node.visit(new LytVisitor() {
            @Override
            public Result beforeNode(LytNode node) {
                if (node instanceof ExportableResourceProvider provider) {
                    provider.exportResources(exporter);
                }
                return Result.CONTINUE;
            }

            @Override
            public Result beforeFlowContent(LytFlowContent content) {
                if (content instanceof ExportableResourceProvider provider) {
                    provider.exportResources(exporter);
                }
                return Result.CONTINUE;
            }
        }, true);
    }
}
