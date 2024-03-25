---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Growth Accelerator
  icon: growth_accelerator
  position: 310
categories:
- machines
item_ids:
- ae2:growth_accelerator
---

# The Growth Accelerator

<BlockImage id="growth_accelerator" p:powered="true" scale="8"/>

The Growth Accelerator massively accelerates [the growth of](../ae2-mechanics/certus-growth.md) certus or amethyst when placed adjacent to the budding block.

Curiously, it can *also* accelerate the growth of various plants.

It does this by applying "random ticks" to the adjacent blocks, in addition to the random ticks that happen naturally.
In theory this means  1 accelerator should make things grow ~90x faster than normal, and the effect stacks additively.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="../assets/assemblies/growth_accelerator.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

Power can be provided via the top or bottom, via either AE2's [cables](cables.md), or other mod power cables. It can
accept either AE2's power (AE) or Forge Energy (FE).

To power it manually, place a <ItemLink id="crank" /> on the top or bottom and right-click it.

The top and the bottom can be identified by the pink flux greebles on them.

<GameScene zoom="6" background="transparent">
<ImportStructure src="../assets/assemblies/accelerator_connections.snbt" />
<IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Recipe

<RecipeFor id="growth_accelerator" />
