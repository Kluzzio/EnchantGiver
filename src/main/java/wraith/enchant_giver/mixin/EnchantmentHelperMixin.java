package wraith.enchant_giver.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.enchant_giver.EnchantsList;

import java.util.HashMap;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private static void getLevel(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        NbtCompound nbtEnchants = stack.getSubNbt("EnchantGiver");
        Identifier enchant = Registry.ENCHANTMENT.getId(enchantment);
        if (nbtEnchants != null && enchant != null && nbtEnchants.contains(enchant.toString())) {
            int j = 0;
            if (!stack.isEmpty()) {
                Identifier identifier = EnchantmentHelper.getEnchantmentId(enchantment);
                NbtList nbtList = stack.getEnchantments();

                for (int i = 0; i < nbtList.size(); i++) {
                    NbtCompound nbtCompound = nbtList.getCompound(i);
                    Identifier identifier2 = EnchantmentHelper.getIdFromNbt(nbtCompound);
                    if (identifier2 != null && identifier2.equals(identifier)) {
                        j = EnchantmentHelper.getLevelFromNbt(nbtCompound);
                    }
                }

            }
            cir.setReturnValue(Math.max(j, nbtEnchants.getInt(enchant.toString())));
            return;
        }
        if (EnchantsList.itemHasEnchantment(Registry.ITEM.getId(stack.getItem()), enchant)) {
            int j = 0;
            if (!stack.isEmpty()) {
                Identifier identifier = EnchantmentHelper.getEnchantmentId(enchantment);
                NbtList nbtList = stack.getEnchantments();

                for (int i = 0; i < nbtList.size(); i++) {
                    NbtCompound nbtCompound = nbtList.getCompound(i);
                    Identifier identifier2 = EnchantmentHelper.getIdFromNbt(nbtCompound);
                    if (identifier2 != null && identifier2.equals(identifier)) {
                        j = EnchantmentHelper.getLevelFromNbt(nbtCompound);
                    }
                }

            }
            cir.setReturnValue(Math.max(j,EnchantsList.getEnchantmentLevel(Registry.ITEM.getId(stack.getItem()), Registry.ENCHANTMENT.getId(enchantment))));
        }
    }

    @ModifyVariable(method = "forEachEnchantment(Lnet/minecraft/enchantment/EnchantmentHelper$Consumer;Lnet/minecraft/item/ItemStack;)V",
            at = @At(value = "INVOKE_ASSIGN", target = "Lnet/minecraft/item/ItemStack;getEnchantments()Lnet/minecraft/nbt/NbtList;"))
    private static NbtList forEachEnchantment(NbtList listTag, EnchantmentHelper.Consumer consumer, ItemStack stack) {
        NbtList newListTag = new NbtList();
        HashMap<String, Integer> enchantMap = new HashMap<>();
        for (int i = 0; i < listTag.size(); ++i) {
            String ench = listTag.getCompound(i).getString("id");
            int level = listTag.getCompound(i).getInt("lvl");
            enchantMap.put(ench, level);
        }
        for(Map.Entry<Identifier, Integer> enchantEntry : EnchantsList.getEnchantments(Registry.ITEM.getId(stack.getItem())).entrySet()) {
            String ench = enchantEntry.getKey().toString();
            int level = enchantEntry.getValue();
            enchantMap.put(ench, level);
        }
        NbtCompound nbtEnchants = stack.getSubNbt("EnchantGiver");
        if (nbtEnchants != null) {
            for (String enchant : nbtEnchants.getKeys()) {
                enchantMap.put(enchant, nbtEnchants.getInt(enchant));
            }
        }
        for (Map.Entry<String, Integer> enchantEntry : enchantMap.entrySet()) {
            NbtCompound tag = new NbtCompound();
            String ench = enchantEntry.getKey();
            int level = enchantEntry.getValue();
            tag.putString("id", ench);
            tag.putInt("lvl", level);
            newListTag.add(tag);
        }
        return newListTag;
    }

}
