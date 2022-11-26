---
categories:
  - ME Network/Misc
item_ids:
  - ae2:security_station
navigation:
  title: ME Security Terminal
---

![A picture of a security terminal.](../../../assets/large/security_terminal.png)

Allows you to configure which users, and what permissions the users have with
the ME System. By existing it enforces permissions on the usage of the system.

The security system does not prevent destructive tampering, removing cables /
machines or breaking of drives is not directly provided by the security
Terminal. If you need to protect your system from physical vandalism you will
need another form of physical security. This block provides Network level
security.

The player who places the <ItemLink
id="security_station"/> has full control over
the network and cannot exclude himself any rights. By adding a blank <ItemLink
id="biometric_card"/> you define a default
behavior for every player who has no own <ItemLink
id="biometric_card"/> registered.

Other than adding security on software layer, you can link up your <ItemLink
id="wireless_terminal"/> with the network and
access it wirelessly.

### The GUI

![Security Terminal GUI](../../../assets/content/securityTerminalGUI.png) |

A. **Sort Order**: Toggle sorting direction

B. **Search Box Mode**: Auto Search

C. **<ItemLink id="biometric_card" />**

D. **Deposit**: User is allowed to store new items into storage

E. **Withdraw**: User is allowed to remove items from storage

F. **Craft**: User can inititate new crafting jobs

G. **Build**: User can modify the physical structure of the network and make
configuration changes.

H. **Security**: User can access and modify the security terminal of the network

I. **Wireless Access Terminal**: Links up the WAT to the network

J. **Linked up WAT**

---|---

<RecipeFor id="security_station" />
