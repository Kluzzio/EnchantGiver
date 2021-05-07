package wraith.enchant_giver.mixin;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.enchant_giver.EnchantGiver;
import wraith.enchant_giver.EnchantsList;

import java.util.HashMap;
import java.util.Map;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin {

    @Shadow private CompoundTag tag;

    @Shadow public abstract Item getItem();

    @Shadow public @Nullable abstract CompoundTag getSubTag(String key);

    @Inject(method = "getEnchantments", at = @At("HEAD"), cancellable = true)
    public void getEnchantments(CallbackInfoReturnable<ListTag> cir) {
        if (!EnchantGiver.SHOW_ENCHANTS) {
            return;
        }
        Identifier itemID = Registry.ITEM.getId(getItem());
        HashMap<String, Integer> enchants = new HashMap<>();
        if (EnchantsList.itemHasEnchantments(itemID)) {
            if (tag != null && tag.contains("Enchantments")) {
                ListTag list = tag.getList("Enchantments", 10);
                for (int i = list.size() - 1; i >= 0; --i) {
                    String ench = list.getCompound(i).getString("id");
                    int level = list.getCompound(i).getInt("lvl");
                    enchants.put(ench, level);
                }
            }
            for (Map.Entry<Identifier, Integer> entry : EnchantsList.getEnchantments(itemID).entrySet()) {
                String ench = entry.getKey().toString();
                int level = entry.getValue();
                if (!enchants.containsKey(ench) || level > enchants.get(ench)) {
                    enchants.put(ench, level);
                }
            }
        }
        CompoundTag nbtEnchants = getSubTag("EnchantGiver");
        if (nbtEnchants != null) {
            for (String enchant : nbtEnchants.getKeys()) {
                enchants.put(enchant, nbtEnchants.getInt(enchant));
            }
        }
        if (!enchants.isEmpty()) {
            ListTag list = new ListTag();
            for (Map.Entry<String, Integer> entry : enchants.entrySet()) {
                CompoundTag enchantTag = new CompoundTag();
                enchantTag.putString("id", entry.getKey());
                enchantTag.putInt("lvl", entry.getValue());
                list.add(enchantTag);
            }
            cir.setReturnValue(list);
            cir.cancel();
        }
    }
}
