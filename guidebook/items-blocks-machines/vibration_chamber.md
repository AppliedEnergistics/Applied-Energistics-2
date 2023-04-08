---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Vibration Chamber
  icon: vibration_chamber
  position: 110
item_ids:
- ae2:vibration_chamber
---
# The Vibration Chamber

<BlockImage id="vibration_chamber" p:active="true" scale="8" /> 

While the primary intended method of providing [energy](../ae2-mechanics/energy.md) to your network is an
<ItemLink id="energy_acceptor"/>, the vibration chamber can directly generate AE. With the default settings it is weak and
inefficient, so it is strongly recommended to use another mod's energy generation equipment.

# Settings

- The vibration chamber provides access to the global setting to view energy in AE or E/FE.

# Config

The properties of the vibration chamber can be edited in common.json in the ae2 folder in the config folder of your .minecraft\
directory.

- energyPerFuelTick sets the efficiency of the vibration chamber.
- minEnergyPerGameTick sets the lowest possible energy generation (the chamber will always slowly use some fuel even if the network
requires no energy).
- maxEnergyPerGameTick sets the max output (and speed) of the vibration chamber.

# Recipe

<RecipeFor id="vibration_chamber" />