
## Grid Services

Each grid provides several services to machines connected to the grid.

AE2 provides some services by default (see sub-interfaces of `IGridService`).
Addons can register their own services using `IGridServiceRegistry`.

Services can be retrieved by calling `IGrid#getService` by passing the grid service's 
interface. For getting AE2's default services, `IGrid` offers several convenience methods.

### Energy

**Service Interface:** `IEnergyGrid`

This service allows energy to be extracted from and injected into the grid's energy storage (i.e. energy cells, the 
grid's internal storage, etc.).

### Ticking

**Service Interface:** `ITickManager`

AE2 offers its grid connected machines an advanced ticking system with the following features:

* Ticking without being a tickable tile entity
* Variable tick rates
* Putting devices to sleep if they run out of work
* Waking sleeping devices in reaction to some event (i.e. neighbors changed)

The grid's `ITickManager` service handles the per-grid aspects of this ticking system.
It offers an API to manage the sleep/wake status of grid nodes.

To participate in the ticking system, your grid node must provide the `IGridTickable` 
grid node service. The `ITickManager` reacts to the presence of this service when your
grid node joins the grid. 

### Storage

**Service Interface**: `IStorageService`

### Auto-Crafting

**Service Interface**: `ICraftingService`

### Security

**Service Interface**: `ISecurityService`

### Pathing

**Service Interface**: `IPathingService`

### Spatial I/O

**Service Interface**: `ISpatialService`


