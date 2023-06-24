---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Energy Acceptor
  icon: energy_acceptor
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:energy_acceptor
---

# The Energy Acceptor

<Row gap="20">
<BlockImage id="energy_acceptor" scale="8" /> 

<GameScene zoom="8" background="transparent">
  <ImportStructure src="../assets/blocks/cable_energy_acceptor.snbt" />
</GameScene>
</Row>

The energy acceptor converts common forms of energy from other tech mods into AE2's internal form of [energy](../ae2-mechanics/energy.md),
AE. While the <ItemLink id="controller" /> can also do this, controller faces are valuable so it's often better to use an energy
acceptor instead.

The ratios for conversion of Forge Energy and Techreborn Energy are

*   2 FE = 1 AE (Forge)
*   1 E  = 2 AE (Fabric)

The speed of conversion is entirely dependent on how much AE your network can store, for reasons that are explained on
[this page](../ae2-mechanics/energy.md).

## Variants

Energy acceptors come in 2 different variants: normal and flat/[subpart](../ae2-mechanics/cable-subparts.md). This allows you to make some setups more compact.

Energy acceptors can be swapped between normal and flat in a crafting grid.

## Recipe

<RecipeFor id="energy_acceptor" />

<RecipeFor id="cable_energy_acceptor" />