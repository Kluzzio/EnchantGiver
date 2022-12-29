package wraith.enchant_giver.mixin;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtList;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wraith.enchant_giver.EnchantsList;

import java.util.HashMap;
import java.util.Map;

@Mixin(EnchantmentHelper.class)
public class EnchantmentHelperMixin {

    @Inject(method = "getLevel", at = @At("HEAD"), cancellable = true)
    private static void getLevelFromSubNbt(Enchantment enchantment, ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        NbtCompound nbtEnchants = stack.getSubNbt("EnchantGiver");
        Identifier enchant = Registries.ENCHANTMENT.getId(enchantment);
        if (nbtEnchants != null && enchant != null && nbtEnchants.contains(enchant.toString())) {
            int levelFromNbt = getLevel(stack, enchantment);
            cir.setReturnValue(Math.max(levelFromNbt, nbtEnchants.getInt(enchant.toString())));
            return;
        }
        if (EnchantsList.itemHasEnchantment(Registries.ITEM.getId(stack.getItem()), enchant)) {
            int levelFromNbt = getLevel(stack, enchantment);
            int levelFromStack = EnchantsList.getEnchantmentLevel(
                    Registries.ITEM.getId(stack.getItem()), Registries.ENCHANTMENT.getId(enchantment));

            cir.setReturnValue(Math.max(levelFromNbt,levelFromStack));
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
        for(Map.Entry<Identifier, Integer> enchantEntry : EnchantsList.getEnchantments(Registries.ITEM.getId(stack.getItem())).entrySet()) {
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

    private static int getLevel(ItemStack stack, Enchantment enchantment) {
        if (!stack.isEmpty()) {
            Identifier identifier = EnchantmentHelper.getEnchantmentId(enchantment);
            NbtList enchantList = stack.getEnchantments();

            for (int i = 0; i < enchantList.size(); i++) {
                Identifier nbtId = EnchantmentHelper.getIdFromNbt(enchantList.getCompound(i));
                if (nbtId != null && nbtId.equals(identifier)) {
                    return EnchantmentHelper.getLevelFromNbt(enchantList.getCompound(i));
                }
            }

        }

        return 0;

    }

}
