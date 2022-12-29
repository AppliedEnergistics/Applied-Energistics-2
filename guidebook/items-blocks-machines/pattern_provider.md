---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Pattern Provider
  icon: pattern_provider
item_ids:
- ae2:pattern_provider
---
# The Pattern Provider

![Pattern Providers](../assets/assemblies/pattern_provider_variants.png)

Pattern providers are the primary way in which your autocrafting system interacts with the world. They push the ingredients in
their patterns to adjacent inventories, and items can be inserted into them in order to insert them into the network. Often
a channel can be saved by piping the output of a machine back into a nearby pattern provider (often the one that pushed the ingredients)
instead of using an <ItemLink id="import_bus" /> to pull the output of the machine into the network.

# Variants

Pattern Providers come in 3 different variants: normal, directional, and flat. This affects which specific sides they push
ingredients to, receive items from, and provide a network connection to.

- Normal pattern providers push ingredients to all sides, receive inputs from all sides, and, like most AE2 machines, act
like a cable providing network connection to all sides.

- Directional pattern providers are made by using a <ItemLink id="certus_quartz_wrench" /> on a normal pattern provider to change its
direction. They only push ingredients to the selected side, receive inputs from all sides, and specifically don't provide a network
connection on the selected side. This allows them to push to AE2 machines without connecting networks, if you want to make a subnetwork.

- Flat pattern providers are a cable subpart, and so multiple can be placed on the same cable, allowing for compact setups.
They act similar to the selected side on a directional pattern provider, providing patterns, receiving inputs, and not
providing a network connection on their face.

Pattern providers can be swapped between normal and flat in a crafting grid.

Pattern providers have a variety of modes. Blocking mode stops the provider from pushing a new batch of ingredients if there are already
ingredients in the machine. There are also options to lock the provider under various redstone conditions, or until the result of the
previous craft is inserted into that specific pattern provider.

# Recipe

<RecipeFor id="pattern_provider" />
<RecipeFor id="cable_pattern_provider" />