---
categories:
  - ME Network/Network Storage
item_ids:
  - ae2:storage_bus
related:
  - Other Networked Storage
  - Storage Cells
  - Possible Upgrades
navigation:
  parent: website/index.md
  title: ME Storage Bus
---

The <ItemLink id="storage_bus"/>, when attached
to another inventory block in the world lets you access that inventory via
networked functions. This allows you to use chests, barrels, or other types of
item storage in your networks.

The storage via the <ItemLink id="storage_bus"/>
is bi-directional, it can both insert, or extract items from the inventory
block it is attached to as long as the <ItemLink
id="storage_bus"/> has its required
[channel](../channels.md).

The UI allows you to control which items are selected as storable items, this
selection has no effect on what items can be extracted once they are in the
storage.

The Storage Bus will function with nearly any inventory block, including

<ItemLink id="interface" />, Minefactory Reloaded DSUs, Factorization Barrels,
JABBA Barrels, and Better Storage Crates. They can also be used to route items
passivly into Buildcraft Pipes.

If you place a storage bus on an <ItemLink
id="interface"/> the storage bus will be able to
interact with the full conents of the target network, unless that interface is
configured to store items inside itself, in which case it will see those
stored items.

_ **\* Storage Buses ignore input/output sides for DSUs, Barrels, and Digital
Chests.**_

<RecipeFor id="storage_bus" />
