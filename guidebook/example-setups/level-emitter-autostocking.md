---
navigation:
  parent: example-setups/example-setups-index.md
  title: Level Emitter Autostocking
  icon: level_emitter
---

# Level Emitter Autostocking

One might ask "How do I keep a certain amount of an item in stock, crafting more as needed?"

One solution is use of an <ItemLink id="export_bus" />, <ItemLink id="level_emitter" />, and <ItemLink id="crafting_card" /> to automatically request new items
from your network's [autocrafting](../ae2-mechanics/autocrafting.md). This setup is for maintaining a large quantity of one item.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/level_emitter_autostocking.snbt" />

  <BoxAnnotation color="#dddddd" min="1 1 0" max="2 1.3 1">
        (1) Export Bus: Filtered to the desired item. Has a Redstone Card and Crafting Card. Redstone mode set to
        "Active with signal", Crafting behavior set to "Do not use stocked items".
        <Row><ItemImage id="redstone_card" scale="2" /> <ItemImage id="crafting_card" scale="2" /></Row>
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="0.7 1 0" max="1 2 1">
        (2) Level Emitter: Configured with the desired item and quantity, set to "Emit when levels are below limit".
  </BoxAnnotation>

  <BoxAnnotation color="#dddddd" min="1 0 0" max="2 1 1">
        (3) Interface: In its default configuration.
  </BoxAnnotation>

<DiamondAnnotation pos="4 0.5 0.5" color="#00ff00">
        To Main Network
    </DiamondAnnotation>

  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Configurations

* The <ItemLink id="export_bus" /> (1) is filtered to the desired item. It has a <ItemLink id="redstone_card" /> and <ItemLink id="crafting_card" />.
  The "Redstone Mode" is set to "Active with signal", The "Crafting Behavior" is set to "Do not use stocked items".
* The <ItemLink id="level_emitter" /> (2) is configured with the desired item and quantity, and set to "Emit when levels are below limit".
* The <ItemLink id="interface" /> (3) is in its default configuration.

## How It Works

1. If the amount of the desired item in [network storage](../ae2-mechanics/import-export-storage.md) is below the quantity specified in the
   <ItemLink id="level_emitter" />, it will emit a redstone signal.
2. Upon recieving a redstone signal (and due to the <ItemLink id="crafting_card" /> and being set to not use stocked items),
   the <ItemLink id="export_bus" /> will request that the network's [autocrafting](../ae2-mechanics/autocrafting.md) craft
   more of the desired item, then export it.
3. Upon having an item pushed into it (and not being configured to have anything in its internal inventory), the <ItemLink id="interface" /> will push that item into network storage.