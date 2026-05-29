# Hotbar Slot Plus

A Minecraft Fabric 1.21.1 client mod that shows extra hotbar rows from the player's existing inventory.

## Features

- Shows additional hotbar rows above the vanilla hotbar.
- Shows 1-4 hotbar rows beside the inventory screen opened with `E`.
- Lets you change the HUD row count with `[` and `]`.
- Lets the mouse wheel select through the visible hotbar rows.
- Uses `Alt` + mouse wheel to switch hotbar pages when total rows exceed the visible rows.
- Supports left/right inventory panel placement.
- Supports two storage modes:
  - reuse normal inventory rows
  - use dedicated extra hotbar slots
- Adds a Mod Menu + Cloth Config settings screen when those mods are installed.
- Stores client settings in `config/hotbar-slot-plus.json`.

## Build

```powershell
gradle build
```
