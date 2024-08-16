Stack follies provide a set of utilities to make items immune to specific damage types.

In order to make a specific item immune to a specific damage type, you need to register the item within the appropriate item tag, going as such:

* Damage type: `<namespace>:<damage_type>`
* Tag: `c:immune/<namespace>/<damage_type>`

For instance, let's say we want to make dirt items immune to explosions.
For that, we need to make the `minecraft:dirt` item immune to the `minecraft:explosion` damage type.
Hence, we need to add it to the `c:immune/minecraft/explosion` item tag.

:warning: This only affects item entities. Blocks dropping as items when destroyed by an explosion is a whole other matter (but might be covered in a future feature)

Also, Stack Follies provide with a few easy tags for some vanilla damages, which by default also call for the corresponding damage type tags:
* `c:cactus_immune`
* `c:explosion_immune`
* `c:fire_immune`

Additionally, you can also make an item immune to despawning, by adding it to the `c:despawn_immune` item tag!

Please note that by default all these tags exist but are empty. Stack follies does not change anything to the game's default behaviour!