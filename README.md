# ExoticScanMod (ESM)

A Minecraft Forge 1.8.9 client mod for **Hypixel Skyblock** that scans players in a lobby, and item frames / armor stands on an island, for "exotic" (specially recolored) leather armor - a rarity/collectible flex among Skyblock players - and logs what it finds to a player database.

## Commands

| Command | Aliases | What it does |
|---|---|---|
| `/scan` | — | Scans every visible player in the current lobby via the Hypixel API, decodes their armor NBT data, and checks it against known exotic/fairy/crystal color values. |
| `/scanisland` | — | Scans item frames and armor stands within 50 blocks of the player for exotic-colored items. |
| `/scanauction` | — | Placeholder - currently only prints a "Scanning Auction House..." message; not yet implemented. |
| `/ESMconfig` | `esmconfig`, `ESMCONFIG`, `EsMcOnFiG` | Opens the in-game config GUI to set your Hypixel API key, level cap, and the "include fairy" toggle. |

## Requirements

- Minecraft **1.8.9**
- Minecraft Forge (1.8.9-compatible build)
- A [Hypixel API key](https://developer.hypixel.net/), set in-game via `/ESMconfig`

## Building

Standard ForgeGradle build (`./gradlew build`). Besides Forge's bundled Gson, the project depends on:

- **Apache HttpClient** - used by `ESMAPI` for the Firebase Realtime Database sync
- **net.querz NBT** - used to parse decoded item NBT data (`NBTDeserializer`, `NamedTag`)
- **Kotlin** - `Colours.kt` is Kotlin, so the Kotlin Gradle plugin and a mixed Java/Kotlin source set need to be configured

None of these ship with a default Forge project, so make sure `build.gradle` declares them explicitly or a fresh clone won't compile.

## Configuration / secrets

This project talks to a Firebase Realtime Database. **Do not commit real credentials.** The repo's `.gitignore` excludes:

- `DataBase.json` (the local player-data cache)
- Any Firebase service-account / admin SDK key files
- `.env` and `config.local.properties`

Set your actual database URL and any keys through a local, gitignored config rather than editing the placeholder fields directly in source.

## How data is stored

The mod currently maintains a **local** `DataBase.json` next to the run directory, written to after every `/scan`. There's also an `ESMAPI` class set up to sync the same data to a Firebase Realtime Database, with security rules already defined for it - but as of this version, the code path that would call it isn't wired into `/scan`'s write step. If you're expecting cloud sync, this is worth verifying against your live Firebase console rather than assuming it's happening.

## Known issues / in progress

- `/scanauction` is a stub, not yet implemented
- `isSkyblockIsland()` in `ScanIslandCommand` always returns `true` - no real island detection yet
- The Hypixel API request rate limiter counts requests but doesn't stop them from being sent once the 300/min cap is hit — it only stops their responses from being processed
- `ConfigGui` prints a debug chat message on every tick while open — likely leftover debug logging
- `ExoticScanMod.MODID` (`"ExoticsScanmod"`) doesn't match the package/class naming (`ExoticScanMod`) - cosmetic, but worth aligning
- Several duplicate/unused imports throughout (harmless to compile, just messy for a public repo)

## License

