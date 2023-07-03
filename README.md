# BlossomTpa

BlossomTpa is a Minecraft Fabric mod in the Blossom-series mods that provides /tpa command and utilities

## Table of contents

- [Dependencies](#dependencies)
- [Config](#config)
- [Commands & their permissions](#commands--their-permissions)
- [Translation keys](#translation-keys)

## Dependencies

* [BlossomLib](https://github.com/BlossomMods/BlossomLib)
* [fabric-permissions-api](https://github.com/lucko/fabric-permissions-api) / [LuckPerms](https://luckperms.net/) /
  etc. (Optional)

## Config

This mod's config file can be found at `config/BlossomMods/BlossomTPA.json`, after running the server with
the mod at least once.

`teleportation`: [TeleportationConfig](https://github.com/BlossomMods/BlossomLib/blob/main/README.md#teleportationconfig) -
teleportation settings  
`timeout`: int - (seconds), how long a tpa/tpahere request is active before it timeouts  
`standStill`: int - (seconds), how long the player has to stand still before being teleported  
`cooldown`: int - (seconds), how long the player has to wait after teleporting using this command, before being able to
teleport again

## Commands & their permissions

- `/tpa <target>` - initiates a tpa request to `<target>`  
  Permission: `blossom.tpa` (default: true)
- `/tpahere <target>` - initiates a tpahere request for `<target>`  
  Permission: `blossom.tpa.here` (default: true)
- `/tpaaccept [<target>]` - accepts a tpa/tpahere request from `<target>`  
  Permission: `blossom.tpa` (default: true)
- `/tpadeny [<target>]` - denies a tpa/tpahere request from `<target>`  
  Permission: `blossom.tpa` (default: true)
- `/tpacancel [<target>]` - cancels a tpa/tpahere request to `<target>`  
  Permission: `blossom.tpa` (default: true)

A player with the permission `blossom.tpa.disallowed` will not be able to receive tpa requests.
The player sending the tpa request will receive a warning message, while the player receiving the request will see nothing.

## Translation keys

Only keys with available arguments are shown, for full list, please see
[`src/main/resources/data/blossom/lang/en_us.json`](src/main/resources/data/blossom/lang/en_us.json)

Note on terms used here:  
"initiator" - player who initiated the tpa/tpahere request and can /tpacancel it  
"receiver" - player who's received a tpa/tpahere request and has to /tpaaccept or /tpadeny it

- `blossom.tpa.fail.to-self`: 0 arguments
- `blossom.tpa.fail.disallowed`: 1 argument - receiver
- `blossom.tpa.fail.similar`: 1 argument - receiver
- `blossom.tpa.fail.multiple`: 0 arguments
- `blossom.tpa.fail.none`: 0 arguments
- `blossom.tpa.fail.none-from`: 1 argument - initiator
- `blossom.tpa.fail.cancel.none-to`: 1 argument - receiver
- `blossom.tpa.to.start.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.start.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.timeout.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.timeout.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.accept.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.accept.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.deny.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.deny.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.cancel.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.to.cancel.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.start.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.start.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.timeout.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command,
  /tpaaccept command, /tpadeny command
- `blossom.tpa.here.timeout.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.accept.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.accept.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.deny.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.deny.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.cancel.initiator`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command
- `blossom.tpa.here.cancel.receiver`: 6 arguments - timeout length, initiator, receiver, /tpacancel command, /tpaaccept
  command, /tpadeny command

`zh_cn` (Chinese, Simplified), `zh_tw` (Chinese, Traditional) - added by @BackWheel
