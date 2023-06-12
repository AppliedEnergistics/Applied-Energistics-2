---
navigation:
  parent: example-setups/example-setups-index.md
  title: Interface Autostocking
  icon: interface
---

# Interface Autostocking

One might ask "How do I keep a certain amount of various items in stock, crafting more as needed?"

One solution is use of an <ItemLink id="interface" /> and <ItemLink id="crafting_card" /> to automatically request new items
from your network's [autocrafting](../ae2-mechanics/autocrafting.md). This setup is more suited to maintaining a small quantity of a wide
variety of items.

This demonstration setup is cut short so it isn't too wide, it is likely most optimal to use 4 <ItemLink id="interface" />s and 4 <ItemLink id="storage_bus" />ses,
to use all 8 [channels](../ae2-mechanics/channels.md) in a regular [cable](../items-blocks-machines/cables.md).

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/interface_autostocking.snbt" />

<BoxAnnotation color="#dddddd" min="0 0 0" max="2 1 1">
        (1) Interfaces: Set to keep the desired items in themselves. They have Crafting Cards.
        <ItemImage id="crafting_card" scale="2" />
  </BoxAnnotation>

<BoxAnnotation color="#dddddd" min="0 1 0" max="2 1.3 1">
        (2) Storage Busses: "Input/Output Mode" set to "Extract Only".
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The <ItemLink id="interface" />s (1) are set to keep the desired items in themselves, by clicking the desired item into their
   top slots or dragging into the top slots from JEI, then clicking on the wrench icon above the slots to set the amount. They have <ItemLink id="crafting_card" />s.
* The <ItemLink id="storage_bus" />ses (2) are set such that "Input/Output Mode" is set to "Extract Only".

## How It Works

1. If an <ItemLink id="interface" /> cannot retrieve enough of a configured item from [network storage](../ae2-mechanics/import-export-storage.md),
   (and it has a <ItemLink id="crafting_card" />), it will request that the network's [autocrafting](../ae2-mechanics/autocrafting.md) craft more of that item.
2. The <ItemLink id="storage_bus" />ses allow the network to access the contents of the interfaces.