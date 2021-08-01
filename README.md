# EnchantGiver

https://www.curseforge.com/minecraft/mc-mods/enchantgiver

#### EnchantGiver adds the ability to permanently give enchantments to items.

These work from the moment the item is obtained, without being able to remove them via grindstone.
This also means you can enchant the item normally, even though it has pre-defined enchantments.
This works on any item (modded or vanilla) with any set of enchantments (modded or vanilla).
This mod should work server side only.
The level of the enchantments can also be higher than the vanilla allowed ones (goes up to 2147483647).

<details>

- To add enchantments to all items of the same type (eg. all iron pickaxes) while ingame, hold the item in your hand and run
`/enchantgiver add_enchant modname:enchantname level`
and to add it to just one item run
`/enchantgiver add_nbt modname:enchantname level`.
- To remove enchants, for item groups, use
`/enchantgiver remove_enchant modname:enchantname`
and for individual items use
`/enchantgiver remove_nbt modname:enchantname`.
- To remove all the enchants (individual and group enchants) from an item, use
`/enchantgiver clear`.
- To add enchantments to items manually, you can do it by going inside `config/enchant_helper/` and reading the `readme.yaml`
After that, you can reload the values ingame by restarting the game or running `/enchantgiver reload`.
- To add group-enchants via code, you do so by calling
`wraith.enchant_giver.EnchantsList#addEnchants(Identifier itemID, HashMap<Identifier, Integer> enchants, boolean replace)`
- for multiple enchants, or for individual enchants:
`wraith.enchant_giver.EnchantsList#addEnchants(Identifier itemID, Identifier enchantID, int level, boolean replace)`.
- To add enchants to individual items instead of groups, for multiple enchants use:
`wraith.enchant_giver.EnchantsList#addNBTEnchants(ItemStack stack, HashMap<String, Integer> enchants)`
- and for single enchants use:
`wraith.enchant_giver.EnchantsList#addNBTEnchants(ItemStack stack, String enchant, int level)`.

</details>
