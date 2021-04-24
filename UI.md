# User Interface

Basic knowledge about the Vanilla UI system is a prerequisite to understanding this document.

The following subsections try to recap some of the basics.

## Anatomy of Opening a UI screen

### Server-Side

When a GUI is opened (i.e. when an AE2 part is right-clicked), a *menu* (MCP: container)
is created server-side, and stored in the `ServerPlayerEntity`.

The server generates a per-player unique id for each menu and passes this id to the menu when it is created. When a new
menu is opened, its id is also stored in the player as the current menu id. Network packets that refer to the current
menu need to include this id. This is necessary since packets can still be in-flight when the server or client decides
to close a menu. Server and client will silently discard network packets if they refer to a menu id that doesn't match
the current menu id.

To open the UI on the client, the server will send a network packet immediately after creating the server-side menu.

It contains:

- The string identifier used to register the `MenuType` used to instantiate the server-side menu, since menu types are
  registered on both sides, the client uses this to create a menu of the same type as the server.
- The id of the menu.
- The menu's title (as a `ITextComponent`), which can be empty
- Arbitrary additional data that was supplied when opening the menu

Forge abstracts this process using the utility method `net.minecraftforge.fml.network.NetworkHooks.openGui`.

For AE2, we introduce another layer on top of this in `appeng.container.ContainerOpener.openContainer`. This class will
ensure that a menu type can declare a function to serialize and deserialize additional data for opening a menu safely,
and also associate a menu with a specific game object so that the menu can access that game object easily.

### Client-Side

When the client receives the packet to open a new menu, it creates the menu object based on the menu type it received.

When registering a `MenuType` in Forge, a factory has to be supplied that will also receive the additional, arbitrary
data that was received from the server.

Since the menu objects exist on both the server and client, their only concern is data synchronization between the two,
and not the actual user interface. In Vanilla the exception to this rule are `Slots`, which do have their position in
the UI determined by the container (AE2 deviates from this).

To get an actual UI to appear, the client uses a `Screen`. When a menu is opened, the menu type is used to look up a
screen factory in the `MenuScreens` class. This means that for every menu type, a corresponding screen factory has to be
registered on the client.

NOTE: Since both the server and client only deal with `MenuType`, menu and screen classes can be reused for multiple
different user interfaces. This can be achieved by registering them multiple times under different IDs and putting any
custom logic in the respective factory functions.

## Data Synchronization

### Server to Client

The server will tick a menu on the server during each server tick and allow it to detect any changes that need to be
synchronized to the client. In particular this is done for `Slots`. These represent inventory slots, and are identified
by their order of creation (which means that server and client must create them in the same way). The server knows the
last ItemStack it sent to the client for each Slot, and will compare this with the Slot content every tick. If it
detects any differences, it will send slot update packets (`SSetSlotPacket` in Forge) to the client to update the state.
When the menu is opened, a more efficient packet will be used (`SWindowItemsPacket`) to send the current itemstack of
all slots to the client.

In addition to slots, menus can also synchronize an arbitrary number of 16-bit integers between the server and client.
A menu has a list of tracked integer objects which will also be checked on every tick. Every tracked integer that
is marked as dirty will be sent to the client using a `SWindowPropertyPacket` packet, which contains the menu id,
the new value, and the index of the "tracked value" to update. Similar to slots this requires the tracked values to
be created in the same order on both server and client.

## Client to Server

When the client wants to perform inventory actions, the client will send a packet back to the server, containing both
the id of the menu, and the index of the slot (in order of creation) that the action refers to. (see
`CClickWindowPacket` in Forge). 
