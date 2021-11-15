package wraith.enchant_giver;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.HashMap;

public class EnchantGiver implements ModInitializer {

    public static final String MOD_ID = "enchant_giver";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean SHOW_ENCHANTS = true;

    @Override
    public void onInitialize() {
        registerEvents();
        String exampleConfig =
                "// Enchant Giver example file\n" +
                "// Adding enchants to tools is done in the following way:\n" +
                "// \"mod_name:item_name\": {\n" +
                "//   \"mod_name:enchant_name\": level\n" +
                "// }\n" +
                "// Example:\n" +
                "{\n" +
                "  \"minecraft:iron_pickaxe\": {\n" +
                "    \"minecraft:fortune\": 3,\n" +
                "    \"minecraft:efficiency\": 2\n" +
                "  },\n" +
                "  \"minecraft:stick\": {\n" +
                "    \"minecraft:sharpness\": 10\n" +
                "  }\n" +
                "}\n" +
                "// You can create multiple files for different configs.\n" +
                "// It is recommended to name the files \"your_mod_name.json\" so as to not cause any incompatibilities with other mods using EnchantGiver.";
        Config.createFile("config/enchant_giver/example.yaml", exampleConfig, true);
        Config.createDefaultConfig();
        JsonObject json = Config.getJsonObject(Config.readFile(new File("config/enchant_giver/config.json")));
        SHOW_ENCHANTS = json.get("show_enchants_in_tooltip").getAsBoolean();
        EnchantsList.loadEnchants();
        LOGGER.info("[Enchant Giver] has been initiated.");
    }

    private void registerEvents() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("enchantgiver")
            .then(CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(context -> {
                    EnchantsList.loadEnchants();
                    JsonObject json = Config.getJsonObject(Config.readFile(new File("config/enchant_giver/config.json")));
                    SHOW_ENCHANTS = json.get("show_enchants_in_tooltip").getAsBoolean();
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3has successfully been reloaded!"), false);
                    }
                    return 1;
                }
            ))
            .then(CommandManager.literal("add_enchant")
                .then(CommandManager.argument("enchant", EnchantmentArgumentType.enchantment())
                .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                    .requires(source -> source.hasPermissionLevel(1))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) {
                            return 1;
                        }
                        Identifier item = Registry.ITEM.getId(player.getMainHandStack().getItem());
                        Identifier enchant = Registry.ENCHANTMENT.getId(EnchantmentArgumentType.getEnchantment(context, "enchant"));
                        int level = IntegerArgumentType.getInteger(context, "level");
                        EnchantsList.addEnchants(item, new HashMap<Identifier, Integer>(){{put(enchant, level);}}, false);
                        EnchantsList.saveConfig(true);
                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully added " + enchant + " to " + item + "!"), false);
                        return 1;
                    })
                )
            ))
            .then(CommandManager.literal("add_nbt")
                .then(CommandManager.argument("enchant", EnchantmentArgumentType.enchantment())
                .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                    .requires(source -> source.hasPermissionLevel(1))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) {
                            return 1;
                        }
                        ItemStack stack = player.getMainHandStack();
                        Identifier item = Registry.ITEM.getId(player.getMainHandStack().getItem());
                        Identifier enchant = Registry.ENCHANTMENT.getId(EnchantmentArgumentType.getEnchantment(context, "enchant"));
                        int level = IntegerArgumentType.getInteger(context, "level");
                        EnchantsList.addNBTEnchant(stack, enchant.toString(), level);
                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully added " + enchant + " NBT to " + item + "!"), false);
                        return 1;
                })
            )))
            .then(CommandManager.literal("remove_enchant")
                .then(CommandManager.argument("enchant", EnchantmentArgumentType.enchantment())
                    .requires(source -> source.hasPermissionLevel(1))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) {
                            return 1;
                        }
                        Identifier item = Registry.ITEM.getId(player.getMainHandStack().getItem());
                        Identifier enchant = Registry.ENCHANTMENT.getId(EnchantmentArgumentType.getEnchantment(context, "enchant"));
                        EnchantsList.removeEnchant(item, enchant);
                        EnchantsList.saveConfig(true);
                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully removed " + enchant + " from " + item + "!"), false);
                        return 1;
                })
            ))
            .then(CommandManager.literal("remove_nbt")
                .then(CommandManager.argument("enchant", EnchantmentArgumentType.enchantment())
                    .requires(source -> source.hasPermissionLevel(1))
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) {
                            return 1;
                        }
                        Identifier item = Registry.ITEM.getId(player.getMainHandStack().getItem());
                        ItemStack stack = player.getMainHandStack();
                        Identifier enchant = Registry.ENCHANTMENT.getId(EnchantmentArgumentType.getEnchantment(context, "enchant"));
                        EnchantsList.removeNBTEnchant(stack, enchant.toString());
                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully removed " + enchant + " NBT from " + item + "!"), false);
                        return 1;
                    })
            ))
            .then(CommandManager.literal("clear")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(context -> {
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player == null) {
                        return 1;
                    }
                    Identifier item = Registry.ITEM.getId(player.getMainHandStack().getItem());
                    ItemStack stack = player.getMainHandStack();
                    EnchantsList.clearNBT(stack);
                    EnchantsList.clearEnchants(item);
                    EnchantsList.saveConfig(true);
                    player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully removed all enchants from " + item + "!"), false);
                    return 1;
                })
            )
        ));
    }

}
