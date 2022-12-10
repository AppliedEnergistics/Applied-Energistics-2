---
navigation:
  parent: website/index.md
  title: Customizing AE2
  icon: certus_quartz_wrench
---

This page describes how AE2 can be tweaked by modpack authors or players to their own play-style.

## Configuration

### Channel Modes

If you don't like playing with channels or just want a more laid back experience, see the
[channel modes section](./features/me-network/channels.md#channel-modes) for multiple options
to customize AE2's channels mechanic.

### Faster Crystal Growth in Certain Fluids

AE2 allows a fluid tag to be specified in `improvedFluidTag`, which will increase the speed at which crystal seeds
grow by `improvedFluidMultiplier` (default: 2) when they are submerged in this type of fluid.

## Recipes

AE2 uses standard JSON recipes. The easiest starting point is to download the jar file and unpack it. Recipes are
in `data/ae2/recipes`.

### Special Recipe Types

AE2 introduces a few custom recipe types that use a custom JSON format. They are described in the following sections.

#### Inscriber

Used by the <ItemLink id="inscriber" />. Example recipes can be found in `data/ae2/recipes/inscriber`.

Please note that the inscriber will also allow each recipe to be flipped so that top and bottom slots are reversed, so
two recipes whose top/bottom are the same after flipping would result in a recipe conflict.

The available JSON properties are as follows:

| Property               | Description                                                                               |
| ---------------------- | ----------------------------------------------------------------------------------------- |
| `type`                 | Must be `ae2:inscriber`                                                                   |
| `mode`                 | Defines whether the top and bottom ingredients are consumed (`press`) or not (`inscribe`) |
| `ingredients`.`top`    | Ingredient for the top slot (optional).                                                   |
| `ingredients`.`middle` | Ingredient for the middle slot (required).                                                |
| `ingredients`.`bottom` | Ingredient for the bottom slot (optional).                                                |
| `result`               | Recipe result                                                                             |

#### Entropy Manipulator

The <ItemLink id="entropy_manipulator" /> uses recipes to decide what it can be used on.
Example recipes can be found in `data/ae2/recipes/entropy`.

Right-clicking with the entry manipulator uses recipes of type `heat`, while shift-right-clicking will use `cool`.
Placing an entropy manipulator in a dispenser will try both types (first `cool`, then `heat`).

The available JSON properties are as follows:

| Property        | Description                                                                        |
| --------------- | ---------------------------------------------------------------------------------- |
| `type`          | Must be `ae2:entropy`                                                              |
| `mode`          | The use-mode of the entropy manipulator this recipe applies to (`heat` or `cool`). |
| `input`         | Which in-world block/fluid this recipe applies to.                                 |
| `input`.`block` | Defines which blocks this recipe applies to (see below for details).               |
| `input`.`fluid` | Defines which fluids this recipe applies to (see below for details).               |
| `output`        | Defines the result of using the item on `input`.                                   |

##### Defining Inputs

The input for the entropy recipe type can be a block or fluid, or both at the same time, to match only
specific waterlogged blocks.

Block and fluid inputs can be defined as follows:

```json
{
  "input": {
    "block": {
      "id": "minecraft:cobblestone",
      "property1": "value",
      "property2": ["value1", "value2"],
      "property3": {
        "min": 1,
        "max": 5
      }
    },
    "fluid": {
      "id": "minecraft:water",
      "property1": "value",
      "property2": ["value1", "value2"],
      "property3": {
        "min": 1,
        "max": 5
      }
    }
  }
}
```

The `id` property is mandatory, while additional properties may be specified to match specific block state properties,
either directly, as a list of matching values, or as a range (between `min` and `max`).

##### Defining Output

Applying an entropy manipulator recipe can result in one or all of:

- Changing the block
- Changing the fluid
- Dropping items

```json
{
  "output": {
    "block": {
      "id": "minecraft:cobblestone",
      "keep": true,
      "property1": "value"
    },
    "fluid": {
      "id": "minecraft:water",
      "property2": "value"
    },
    "drops": [
      {
        "item": "minecraft:snowball",
        "count": 1
      }
    ]
  }
}
```

All three properties (block, fluid, drops) are optional, but can also be used together.
The special `keep` property for `block` and `fluid` will copy over the block state properties from the existing
block while changing the block or fluid `id`. Additionally, any extra properties will be interpreted as block state
properties and applied to the new block.

If the operation should drop items, those should be specified as a list in `drops`.

#### Matter Cannon Ammo

The <ItemLink id="matter_cannon" /> uses recipes to decide which items count as ammo, and what their damage value should
be. Example recipes can be found in `data/ae2/recipes/matter_cannon`.

| Property | Description                                                                                                    |
| -------- | -------------------------------------------------------------------------------------------------------------- |
| `type`   | Must be `ae2:matter_cannon`                                                                                    |
| `ammo`   | Ingredient identifying which item this recipe applies to.                                                      |
| `weight` | The weight of the ammo. This affects block penetration and damage. Damage is weight divided by 20, rounded up. |
