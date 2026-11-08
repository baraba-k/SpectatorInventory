# Spectator Inventory

Server-side NeoForge mod for Minecraft 1.21.1 that lets operators view and edit
the inventories and ender chests of players, including players who are offline.

A direct port of the well-known InvSee++ Bukkit plugin.

## Requirements

- JDK 21 (Java 21)
- Internet access on first build (downloads NeoForged dependencies)

## Building

Run one of the following from this directory:

Windows:

```
gradlew.bat build
```

Linux / macOS:

```
./gradlew build
```

The built jar will be at:

```
build/libs/spectatorinv-1.21.1-1.0.0.jar
```

No global Gradle installation is required - the wrapper downloads it
automatically.

## Installing

1. Copy `spectatorinv-1.21.1-1.0.0.jar` into the server's `mods/` folder.
2. Start the server.
3. Grant yourself operator status: in the server console type `op <your name>`.

## Commands

All commands require operator permission.

| Command | Description |
| --- | --- |
| `/spinvsee <player>` | Open and edit a player's inventory (alias `spinventorysee`) |
| `/endersee <player>` | Open and edit a player's ender chest (alias `enderchestsee`) |
| `/spinvclear <player> [item] [amount]` | Remove items from an inventory |
| `/enderclear <player> [item] [amount]` | Remove items from an ender chest |
| `/spinvclone <source> [target]` | Copy an inventory (target defaults to you) |
| `/enderclone <source> [target]` | Copy an ender chest |
| `/spinvreload` | Reload configuration (alias `isppreload`) |

Players do not need to install this mod on their client.

## Configuration

A server config file is generated at
`config/spectatorinv-server.toml` after the first run:

- `enableOfflinePlayerSupport` - allow viewing offline players (default true)
- `enableUnknownPlayerSupport` - create empty inventories for unknown players (default true)
- `titles.inventory` - title of the inventory view (`<player>` is replaced)
- `titles.enderchest` - title of the ender chest view (`<player>` is replaced)

## License

MIT