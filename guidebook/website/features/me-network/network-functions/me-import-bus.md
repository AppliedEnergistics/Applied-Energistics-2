---
categories:
  - ME Network/Network Functions
item_ids:
  - ae2:import_bus
related:
  - Possible Upgrades
navigation:
  parent: website/index.md
  title: ME Import Bus
---

![A picture of an Import Bus.](../../../assets/large/import_bus.png)Pulls items from
the inventory it is pointed at and places them into the [ME Network](../../me-network.md)'s Networked Storage.
You can specify which items it will pull out via the UI, else it tries to pull out any item in the adjacent
inventory. The <ItemLink id="import_bus"/> will
attempt to import any possible options, even if 1 or more of the configured
items cannot be stored. The <ItemLink
id="import_bus"/> requires a
[channel](../channels.md) to function.

This is the functional opposite of the <ItemLink
id="export_bus"/>.

<RecipeFor id="import_bus" />
