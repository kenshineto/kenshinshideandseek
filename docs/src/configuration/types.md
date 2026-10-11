# Types

The following are the types used within the configuation files of the plugin.

## Primitives

| type | description | example |
| ---- | ----------- | ------- |
| int | An integer number. | -3 |
| uint | An positive integer number. | 7 |
| long | A large integer number. | -2147483649 |
| ulong | A large positive integer number. | 4294967326 |
| bool | Either `true` or `false`. | true |
| decimal | A decimal floating point number. | 1.2 |
| string | A string of text. | "example" |
| list | A list of a type, if not specified it will be a list of strings. | ["example"] |

Some types will be marked as `optional.` This just means that they don't need to be specified. Setting these values as `null` is the same as not specifying them. When not set, they may be removed from the configuation file upon plugin reload.

## Map

A map is a mapping from a key to a value. A map has a type for its key, and a type for its value. These docs will show map as `map<key,value>` where `key` and `value` are the types.

An eample map is shown below. This map is from `string` to `uint`. Note the two space indentation.
```yaml
enchantments:
  knockback: 7
  sharpness: 3
```

## Item

An item type represents a physical item given to players that is held in their inventory.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| name | The name of the item. | optional string | null |
| material | The material/type of the item. If invalid the item will not be given to the player. | string | "BLOCK" |
| lore | Lore to be shown when hovering over the item. | list | [] |
| enchantments | Map of enchantment names to their level. | map<string, int> | {} |
| unbreakable | If the item has unlimited durabality. | optional bool | null |
| modelData | The block data field used in minecraft versions prior to 1.13. | optional uint | null |
| owner | The owner of skull given the item is a skull. | optional string | null |
| effect | The potion effect given the item is a potion. | optional string | null |
| slot | The inventory slot this item will always be put in. If not set it will be put in the net availiable slot. | optional uint | null |

## Effect

A potion effect that will be applied to players depending on the situation.

| name | description | type | default |
| ---- | ----------- | ---- | ------- |
| type | The type of potion effect. | string | "NONE" |
| duration | The length in seconds that the effect will last. | uint | 60 |
| amplifier | The amplifier of the potion effect. | uint | 1 |
| ambient | Marks the surrounding particles as semitransparent and less visually intrusive. | bool | true |
| particles | Hides the surrounding particles. | bool | true |
