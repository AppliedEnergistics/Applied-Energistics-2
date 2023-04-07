---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Matter Cannon
  icon: matter_cannon
  position: 410
item_ids:
- ae2:matter_cannon
---
# The Matter Cannon

<ItemImage id="matter_cannon" scale="4" />

The matter cannon is a portable railgun that can fire small items as projectiles, like <ItemLink id="matter_ball" />s and metal nuggets. The damage
depends on the item being fired, with "heavier" items like gold nuggets (10 damage) doing more damage than light items like matter balls (2 damage).
It consumes a base energy of 1600 AE per shot.

When the config option "matterCannonBlockDamage" is true, the cannon will break blocks depending on their hardness and
the damage of the ammunition.

Its energy can be recharged in a <ItemLink id="charger" />.

Matter cannons act like [storage cells](storage_cells.md), and their ammunition magazine can most easily be filled by sticking
the cannon in the storage cell slot in a <ItemLink id="chest" />

# Upgrades

Matter cannons support the following [upgrades](upgrade_cards.md), inserted via a <ItemLink id="cell_workbench" />:

- <ItemLink id="fuzzy_card" /> lets the cell be partitioned by damage level and/or ignore item NBT
- <ItemLink id="inverter_card" /> switches the filter from a whitelist to a blacklist
- <ItemLink id="speed_card" /> increases the energy used each shot, making it fire with more power.
- <ItemLink id="void_card" /> voids items inserted if the cell is full. Be careful to partition this!
- <ItemLink id="energy_card" /> in order to increase the battery capacity

# Recipe

<RecipeFor id="matter_cannon" />