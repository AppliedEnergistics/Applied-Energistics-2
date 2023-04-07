---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Level Emitter
  icon: level_emitter
  position: 220
item_ids:
- ae2:level_emitter
- ae2:energy_level_emitter
---
# The Level Emitter

The Level Emitter emits a redstone signal depending on the quantity of an item in 
[network storage](../ae2-mechanics/import-export-storage.md).

There is also a version that emits a redstone signal depending on the [energy](../ae2-mechanics/energy.md) stored
in your network.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

Unlike other [devices](../ae2-mechanics/devices.md), level emitters *do not* require a [channel](../ae2-mechanics/channels.md).

# Settings

- The Level Emitter can be set to either "greater than/equal to" or "less than" mode
- When a <ItemLink id="crafting_card" /> is inserted, it can be set to "emit redstone while item is crafting" or
  "emit redstone to craft item"

# Upgrades

The level emitter supports the following [upgrades](upgrade_cards.md):

- <ItemLink id="fuzzy_card" /> lets the emitter filter by damage level and/or ignore item NBT
- <ItemLink id="crafting_card" /> enables the crafting functionality

# Crafting Functionality

If a <ItemLink id="crafting_card" /> is inserted, the emitter will be switched into crafting mode.

This enables two options: 

The first option, "emit redstone while item is crafting", makes the emitter emit a redstone signal while your [autocrafting](../ae2-mechanics/autocrafting.md)
is crafting some specific item through <ItemLink id="pattern_provider" />s. This is useful for only turning on specific
power-hungry automation setups while they are actually being used.

The second option, "emit redstone to craft item", is extremely useful for specific use cases like infinite farms and
automation setups that only have a chance of making an output, instead of a guranteed output.
This setting creates a virtual [pattern](patterns.md) for [autocrafting](../ae2-mechanics/autocrafting.md) to use, for whatever item
is in the emitter's filter slot.
(For correct functionality, an actual pattern for the same item **should not exist** in your <ItemLink id="pattern_provider" />s)

 This "pattern" does not define, or even care about ingredients.
All it says is "If you emit redstone from this level emitter, the ME system will recieve this item at some point in the
near or distant future". This is usually used to activate and deactivate infinite farms which require no input ingredients,
or to activate a system that handles recursive recipes (which standard autocafting cannot understand) like, for example, "1 cobblestone = 2 cobblestone"
if you have a machine that duplicates cobblestone.

# Recipe

<RecipeFor id="level_emitter" />
<RecipeFor id="energy_level_emitter" />