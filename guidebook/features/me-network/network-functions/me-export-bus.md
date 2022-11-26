---
categories:
  - ME Network/Network Functions
item_ids:
  - ae2:export_bus
related:
  - Possible Upgrades
navigation:
  title: ME Export Bus
---

![A Image of an Export Bus](../../../assets/large/export_bus.png)

The <ItemLink id="export_bus"/> extracts items from the
[ME Network](../../me-network.md)'s Networked Storage and places them into the inventory it faces.
You must configure which items it will insert, leaving the configuration blank will result in nothing.

The <ItemLink id="export_bus"/> will try to export any of the items on its list
skipping over those it cannot fit into the destination.

The <ItemLink id="export_bus"/> requires a [channel](../channels.md) to function.

This is the functional opposite of the <ItemLink id="import_bus"/>.

<RecipeFor id="export_bus" />
