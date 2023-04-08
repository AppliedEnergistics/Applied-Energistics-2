---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Color Applicator
  icon: color_applicator
  position: 410
item_ids:
- ae2:color_applicator
---
# The Color Applicator

<ItemImage id="color_applicator" scale="4" />

The color applicator is used to paint colorable blocks like [cables](cables.md), wool, terracotta, glass, and concrete. It uses
[paintballs](paintballs.md) or dyes, and snowballs can eb used in order to clean color off of cables and paintball splotches off of blocks.

Its energy can be recharged in a <ItemLink id="charger" />.

Color applicators act like [storage cells](storage_cells.md), and their paint storage can most easily be filled by sticking
the applicator in the storage cell slot in a <ItemLink id="chest" />

To use a color applicator, right click to apply, and shift-scroll to cycle through the stored paintballs and dyes.

# Upgrades

Color Applicators support the following [upgrades](upgrade_cards.md), inserted via a <ItemLink id="cell_workbench" />:

- <ItemLink id="equal_distribution_card" /> allocates the same amount of cell byte space to each type, so one type cannot fill up the entire cell
- <ItemLink id="void_card" /> voids items inserted if the cell is full (or that specific type's allocated space in the
case of an equal distribution card). Be careful to partition this!
- <ItemLink id="energy_card" /> in order to increase their battery capacity

# Recipe

<RecipeFor id="color_applicator" />