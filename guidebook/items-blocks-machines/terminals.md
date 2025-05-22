---
navigation:
  parent: items-blocks-machines/items-blocks-machines-index.md
  title: Terminals
  icon: crafting_terminal
  position: 210
categories:
- devices
item_ids:
- ae2:terminal
- ae2:crafting_terminal
- ae2:pattern_encoding_terminal
- ae2:pattern_access_terminal
---

# Terminals

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/terminals.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

While <ItemLink id="pattern_provider" />s, <ItemLink id="import_bus" />ses, <ItemLink id="storage_bus" />ses, and the et cetera
are the primary method by which an AE2 network interacts with the world, Terminals are the primary method by which an AE2
network interacts with *you*. There are several variants with differing functions.

Terminals will inherit the color of the [cable](cables.md) they are mounted on.

They are [cable subparts](../ae2-mechanics/cable-subparts.md).

## Terminal Placement

As a terminal is often the first [subpart](../ae2-mechanics/cable-subparts.md) someone might place,
it is common to get it wrong and place the terminal backwards. Here is an example of what to do and what not to do:

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/assemblies/terminal_placement.snbt" />
  <IsometricCamera yaw="195" pitch="30" />

  <LineAnnotation color="#ff3333" from="2.5 .5 .5" to="4.5 2.5 .5" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#ff3333" from="2.5 2.5 .5" to="4.5 .5 .5" alwaysOnTop={true} thickness="0.05"/>

  <LineAnnotation color="#33ff33" from="-.5 2.5 .5" to="1 .5 .5" alwaysOnTop={true} thickness="0.05"/>
  <LineAnnotation color="#33ff33" from="1 .5 .5" to="1.5 1 .5" alwaysOnTop={true} thickness="0.05"/>
</GameScene>

You still have a terminal and an energy acceptor, except now the terminal is the right way around and actually
connected to the network, and it all fits in a smaller space too.

<a name="terminal-ui"></a>

# Terminal

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/blocks/terminal.snbt" />
  <IsometricCamera yaw="180" />
</GameScene>

Your basic terminal, allowing you to view and access the contents of your [network's storage](../ae2-mechanics/import-export-storage.md)
and request things from your [autocrafting](../ae2-mechanics/autocrafting.md) setup.

## The UI

There are several sections of a basic terminal's UI

The center section gives access to your network's storage. You can put things in and take things out. There are several
mouse/key shortcuts:

*   Left-click grabs a stack, right-click grabs half a stack.
*   If an item or fluid or etc. is able to be [autocrafted](../ae2-mechanics/autocrafting.md),
    whatever you have bound to "pick block" (usually middle-click) brings up a UI to specify the amount to be crafted. You can also input formulas like `3*64/2`,
    or type `=32` to only craft the number of items needed to reach 32 in your storage.
*   Holding shift will freeze the displayed items in-place, stopping them from re-organizing themselves when quantities change or new items enter the system.
*   Right-clicking with a bucket or other fluid container will deposit the fluid, left-clicking a fluid in the terminal with
    an empty fluid container will withdraw the fluid.

The left section has settings buttons to:

*   Sort by different attributes like name, mod, and quantity
*   View stored, craftable, or both
*   View items, fluids, or both
*   Change the sort order
*   Open the detailed terminal settings window
*   Change the height of the terminal UI

On the right there are slots for <ItemLink id="view_cell" />s

The top-right of the center section (hammer button) brings up the [autocrafting](../ae2-mechanics/autocrafting.md) status
UI, allowing you to see the progress of your autocrafts and what each [crafting CPU](crafting_cpu_multiblock.md) is doing.

## Recipe

<RecipeFor id="terminal" />

<a name="crafting-terminal-ui"></a>

# Crafting Terminal

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/blocks/crafting_terminal.snbt" />
  <IsometricCamera yaw="180" />
</GameScene>

The Crafting Terminal is similar to a regular terminal, with all the same settings and sections, but with an added crafting grid that will be automatically
refilled from [network storage](../ae2-mechanics/import-export-storage.md). Be careful when shift-clicking the output!

You should upgrade your terminal into a crafting terminal ASAP.

## The UI

The crafting terminal has the same UI as the regular terminal, but with an added crafting grid in the middle.

There are 2 additional buttons, to empty the crafting grid into network storage or your inventory.

## Recipe

<RecipeFor id="crafting_terminal" />

<a name="pattern-encoding-terminal-ui"></a>

# Pattern Encoding Terminal

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/blocks/pattern_encoding_terminal.snbt" />
  <IsometricCamera yaw="180" />
</GameScene>

The Pattern Encoding Terminal is similar to a regular terminal, with all the same settings and sections, but with an added
[pattern](patterns.md) encoding interface. It looks similar to a crafting terminal's UI but this crafting grid doesn't actually
perform crafts.

You should have one of these in addition to a crafting terminal.

## The UI

The crafting terminal has the same UI as the regular terminal, added [pattern](patterns.md) encoding interface.

The pattern encoding interface has several sections:

A slot to insert <ItemLink id="blank_pattern" />s.

A big arrow to encode the pattern.

A slot for encoded patterns. Place a pattern that has already been encoded in this slot in order to edit it, then click the "encode" arrow.

4 tabs on the right to swap the type of pattern to be encoded between

*   Crafting
*   Processing
*   Smithing
*   Stonecutting

The central UI changes depending on the type of pattern to be encoded:

*   In crafting mode:
    *   Left-click in or drag from JEI/REI the ingredients to form the recipe. Right-click to remove the ingredient.
    *   Enabling substitiutions allows things like crafting sticks from any plank type. This should only be used
        when absolutely necessary.
    *   Fluid substitutions allows using stored fluids in place of buckets of fluids.
    *   You can also directly encode a pattern from the JEI/REI recipe screen.

*   In processing mode:
    * Left-click or right-click in or drag from JEI/REI the ingredients to specify the inputs and outputs of the recipe.
    * Right-click with a fluid container (like a bucket or fluid tank) to set that fluid as an ingredient instead of the bucket or tank item.
    * When holding a stack, left-click places the whole stack, right-click places one item. Left-click on an existing ingredient stack to
        remove the whole stack and right-click to decrement the stack by 1. Whatever you have bound to "pick block" (usually middle-click)
        lets you specify a precise amount of the item or fluid.
    * The output slots have a primary output and space for any secondary outputs you might want the autocrafting algorithm to know about.
    * Both input and output slots scroll, so you can have 81 different ingredients and 26 secondary outputs
    * You can also directly encode a pattern from the JEI/REI recipe screen.

*   The smithing and stonecutting mode UIs work similarly to a smithing table and stonecutter respectively.

## Recipe

<RecipeFor id="pattern_encoding_terminal" />

<a name="pattern-access-terminal-ui"></a>

# Pattern Access Terminal

<GameScene zoom="6" background="transparent">
  <ImportStructure src="../assets/blocks/pattern_access_terminal.snbt" />
  <IsometricCamera yaw="180" />
</GameScene>

The Pattern Access Terminal serves to solve a specific issue: in a dense tower of <ItemLink id="pattern_provider" />s
and <ItemLink id="molecular_assembler" />s, you can't physically access the providers to insert new patterns. Additionally,
perhaps you're lazy and don't want to walk across your base to insert a [pattern](patterns.md). The pattern access terminal
allows access to all pattern providers on the network.

## The UI

This terminal has a different UI to all the other terminals.

It has settings for terminal height and which pattern providers to show.

Each row in the terminal corresponds to a specific pattern provider.

Pattern providers in the terminal are sorted by what blocks they are connected to, or what name you have given them (in an anvil or
with a <ItemLink id="name_press" />).

## Recipe

<RecipeFor id="pattern_access_terminal" />
