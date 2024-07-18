---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Vibration Chamber
  icon: vibration_chamber
  position: 110
categories:
- network infrastructure
item_ids:
- ae2:vibration_chamber
---

# The Vibration Chamber

<BlockImage id="vibration_chamber" p:active="true" scale="8" />

While the primary intended method of providing [energy](../ae2-mechanics/energy.md) to your network is an
<ItemLink id="energy_acceptor" />, the vibration chamber can directly generate small to middling amounts of AE.

By default (no [upgrades](upgrade_cards.md) and default configs) it makes 40 AE/t.

When the network's [energy](../ae2-mechanics/energy.md) storage is full, the vibration chamber throttles down to conserve
fuel, but cannot fully shut off.

## Settings

*   The vibration chamber provides access to the global setting to view energy in AE or E/FE.

## Upgrades

The vibration chamber supports the following [upgrades](upgrade_cards.md):

*   <ItemLink id="energy_card" /> increases the efficiency of the chamber by +50%, for a max of +150%, or 250% of the base efficiency.
*   <ItemLink id="speed_card" /> increases the burn rate of the chamber by +50%, for a max of +150%, or 250% of the base power output.

## Config

The properties of the vibration chamber can be edited in common.json in the ae2 folder in the config folder of your .minecraft\
directory.

*   baseEnergyPerFuelTick sets the base, un-upgraded efficiency of the vibration chamber.
*   minEnergyPerGameTick sets the lowest possible energy generation (the chamber will always slowly use some fuel even if the network
    requires no energy).
*   maxEnergyPerGameTick sets the un-upgraded max output (and speed) of the vibration chamber.

## Recipe

<RecipeFor id="vibration_chamber" />
