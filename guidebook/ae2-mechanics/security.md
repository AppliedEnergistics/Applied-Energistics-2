---
navigation:
  parent: ae2-mechanics/ae2-mechanics-index.md
  title: Security
  icon: security_station
---

# Security

<BlockImage id="security_station" p:powered="true" scale="8" />

The security system allows you to protect your network and set security permissions for specific players and the general public.

This is done by placing a <ItemLink id="security_station" /> on your network.

This will not physically protect your network, adversarial actors can still break blocks on the network. You will need to
come up with some other method (like chunk claims and protections) to maintain physical security.

## Permissions

Permissions are the specifics of what each player is allowed to do with the network. There are 5 categories:
- Deposit - Allowed to insert items into the network
- Withdraw - Allowed to remove items from the network
- Craft - Allowed to make [autocrafting](../ae2-mechanics/autocrafting.md) requests.
- Build - Allowed to add to the network, and open most [device](../ae2-mechanics/devices.md) GUIs.
- Security - Allowed to change the above permissions in the network's <ItemLink id="security_station" />.

By default, the <ItemLink id="security_station" /> gives everyone all permissions.

The player who places the security terminal on the network has full permissions, and cannot remove permissions from themselves.

In order to define more specific permissions, a <ItemLink id="biometric_card" /> is required.

## Biometric Card

<ItemImage id="biometric_card" scale="4" />

The <ItemLink id="biometric_card" /> is used to specify permissions for specific players.

A blank biometric card (with no player bound) defines the default permissions for all players. Thus, to deny everyone any
permissions, insert a blank biometric card with no permissions into the security terminal.

- Right-click on a player to bind the card to them.
- Shift-right-click to bind to yourself, or clear the card.

Once bound to a player, place the card in the bottom slot of the <ItemLink id="security_station" /> then toggle the
permission buttons to allow or deny those permissions.

Then place the card in one of the top slots in the security terminal.

## Permissions Of Devices

In order to prevent cheeky workarounds like some adversarial actor placing a storage bus on an interface and thus using
the [storage bus-interface interaction](../items-blocks-machines/interface.md#special-interactions) to access the network's
contents though their own little [subnet](subnetworks.md), each [device](../ae2-mechanics/devices.md) inherits its own permissions
from the player that placed it.