
- Prefer running tests, building the project and finding compilation errors via the IDEs MCP server.
- When unit tests fail with "Components not bound yet" error, it means they need a fully initialized Minecraft (incl. data packs).
  Inject a MinecraftServer into the test constructor to set this up and add `@ExtendWith(EphemeralTestServerProvider.class)` to the test class.
- Game tests use a full game environment and are needed if a level is required for testing.
  Check classes in `appeng.server.testplots` for examples. To run game tests, run the `Gametests` IDE run configuration.
  If the exit code is 0, all tests passed.
