---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Facades
  icon: facade
  icon_components:
    "ae2:facade_item": "minecraft:stone"
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:facade
---

# Facades

Facades can be used to make your base appear cleaner.
They can cover up both sizes of cable, and they may be made out of many kinds of blocks.

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/facades_1.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Although facades can cover all sides of a cable, [subparts](../ae2-mechanics/cable-subparts.md) and cable connections
are allowed to protrude though.

<GameScene zoom="6"  interactive={true}>
  <ImportStructure src="../assets/assemblies/facades_2.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Be clever with them to improve your base aesthetic or make blocks with different textures on each side.

<GameScene zoom="4" interactive={true}>
  <ImportStructure src="../assets/assemblies/facades_3.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Hiding Facades

Facades will be hidden while holding a <ItemLink id="network_tool" /> in either hand.

You can interact with blocks behind hidden facades without having to remove the facades first.

## Recipe

To make a facade with the texture of a block, place that block in the middle of 4 <ItemLink id="cable_anchor" />s.

![Facade Recipe](../assets/diagrams/facade_recipe.png)
