package appeng.siteexport.model;

import java.util.ArrayList;
import java.util.List;

/**
 * Describes a type of P2P tunnel.
 */
public class P2PTypeInfo {
    // ID of the tunnel part that represents this type
    public String tunnelItemId;
    // IDs of items that cause attunement to this type
    public List<String> attunementItemIds = new ArrayList<>();
    // Class-Names of APIs provided by items that cause attunement to this type
    public List<String> attunementApiClasses = new ArrayList<>();
}
