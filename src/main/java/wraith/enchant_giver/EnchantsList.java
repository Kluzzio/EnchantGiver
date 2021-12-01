package wraith.enchant_giver;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.Identifier;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class EnchantsList {

    //ItemID -> [Enchant, Level]
    private static final HashMap<Identifier, HashMap<Identifier, Integer>> ENCHANT_LIST = new HashMap<>();

    //Returns a HashMap containing the Enchantments and their Levels for the given item ID
    public static HashMap<Identifier, Integer> getEnchantments(Identifier itemID) {
        return ENCHANT_LIST.getOrDefault(itemID, new HashMap<>());
    }

    //Adds a new Tool -> Enchantment pair inside the HashMap
    public static void addEnchants(Identifier itemID, HashMap<Identifier, Integer> enchants, boolean replace) {
        if (ENCHANT_LIST.containsKey(itemID) && !replace) {
            ENCHANT_LIST.get(itemID).putAll(enchants);
        } else {
            ENCHANT_LIST.put(itemID, enchants);
        }
        saveConfig(true);
    }

    public static void addEnchant(Identifier itemID, Identifier enchantID, int level, boolean replace) {
        HashMap<Identifier, Integer> map = new HashMap<>();
        map.put(enchantID, level);
        addEnchants(itemID, map, replace);
    }

    public static void removeEnchant(Identifier itemID, Identifier enchantID) {
        if (ENCHANT_LIST.containsKey(itemID)) {
            ENCHANT_LIST.get(itemID).remove(enchantID);
        }
        saveConfig(true);
    }

    public static void clearEnchants(Identifier item) {
        ENCHANT_LIST.remove(item);
        saveConfig(true);
    }

    public static void clearEnchantsList() {
        ENCHANT_LIST.clear();
        saveConfig(true);
    }

    public static boolean itemHasEnchantment(Identifier itemID, Identifier enchant) {
        return ENCHANT_LIST.containsKey(itemID) && ENCHANT_LIST.get(itemID).containsKey(enchant) && ENCHANT_LIST.get(itemID).get(enchant) > 0;
    }
    public static boolean itemHasEnchantments(Identifier itemID) {
        return ENCHANT_LIST.containsKey(itemID) && !ENCHANT_LIST.get(itemID).isEmpty();
    }

    public static int getEnchantmentLevel(Identifier itemID, Identifier enchant) {
        return ENCHANT_LIST.getOrDefault(itemID, new HashMap<>()).getOrDefault(enchant, 0);
    }

    public static void loadEnchants() {
        File file = new File("config/enchant_giver/enchants.json");
        if (!file.getName().endsWith(".json")) {
            return;
        }
        JsonObject json = Config.getJsonObject(Config.readFile(file));
        if (json == null) {
            return;
        }
        clearEnchantsList();
        for (Map.Entry<String, JsonElement> element : json.entrySet()) {
            JsonObject enchantments = element.getValue().getAsJsonObject();
            HashMap<Identifier, Integer> enchantMap = new HashMap<>();
            for (Map.Entry<String, JsonElement> enchantment : enchantments.entrySet()) {
                enchantMap.put(new Identifier(enchantment.getKey()), enchantment.getValue().getAsInt());
            }
            addEnchants(new Identifier(element.getKey()), enchantMap, false);
        }
    }

    public static void saveConfig(HashMap<Identifier, HashMap<Identifier, Integer>> contents, boolean overwrite) {
        JsonObject json = new JsonObject();
        for (Map.Entry<Identifier, HashMap<Identifier, Integer>> contentEntry : contents.entrySet()) {
            String tool = contentEntry.getKey().toString();
            JsonObject itemEntry = new JsonObject();
            for (Map.Entry<Identifier, Integer> enchantsEntry : contentEntry.getValue().entrySet()) {
                itemEntry.addProperty(enchantsEntry.getKey().toString(), enchantsEntry.getValue());
            }
            json.add(tool, itemEntry);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Config.createFile("config/enchant_giver/enchants.json", gson.toJson(json), overwrite);
    }

    public static void saveConfig(boolean overwrite) {
        saveConfig(ENCHANT_LIST, overwrite);
    }

    public static void addNBTEnchants(ItemStack stack, HashMap<String, Integer> enchants) {
        for (Map.Entry<String, Integer> enchant : enchants.entrySet()) {
            addNBTEnchant(stack, enchant.getKey(), enchant.getValue());
        }
    }

    public static void addNBTEnchant(ItemStack stack, String enchant, Integer level) {
        NbtCompound tag = stack.getOrCreateSubNbt("EnchantGiver");
        tag.putInt(enchant, level);
    }

    public static void removeNBTEnchant(ItemStack stack, String enchant) {
        NbtCompound tag = stack.getOrCreateSubNbt("EnchantGiver");
        tag.remove(enchant);
    }

    public static void clearNBT(ItemStack stack) {
        stack.setSubNbt("EnchantGiver", new NbtCompound());
    }

}
