package wraith.enchant_giver;

import com.google.gson.JsonObject;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.command.argument.EnchantmentArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;

import java.io.File;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;

public final class EnchantCommands {
    private EnchantCommands() {}

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> dispatcher.register(CommandManager.literal("enchantgiver")
            .then(CommandManager.literal("reload")
                .requires(source -> source.hasPermissionLevel(1))
                .executes(context -> {
                    EnchantsList.loadEnchants();
                    JsonObject json = Config.getJsonObject(Config.readFile(new File("config/enchant_giver/config.json")));
                    EnchantGiver.SHOW_ENCHANTS = json.get("show_enchants_in_tooltip").getAsBoolean();
                    ServerPlayerEntity player = context.getSource().getPlayer();
                    if (player != null) {
                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3has successfully been reloaded!"), false);
                    }
                    return 1;
                })
            )
            .then(CommandManager.literal("add")
                .then(CommandManager.argument("type", StringArgumentType.word())
                    .suggests(EnchantCommands::enchantTypes)
                    .then(CommandManager.argument("enchant", EnchantmentArgumentType.enchantment())
                        .then(CommandManager.argument("level", IntegerArgumentType.integer(1))
                            .requires(source -> source.hasPermissionLevel(1))
                            .executes(context -> {
                                ServerPlayerEntity player = context.getSource().getPlayer();
                                if (player == null) {
                                    return 1;
                                }
                                ItemStack stack = player.getMainHandStack();
                                if (stack.isEmpty() || stack.getItem() == Items.AIR) {
                                    context.getSource().sendFeedback(new TranslatableText("enchantgiver.error.no_item"), false);
                                    return 1;
                                }
                                Identifier item = Registry.ITEM.getId(player.getMainHandStack().getItem());
                                Identifier enchant = Registry.ENCHANTMENT.getId(EnchantmentArgumentType.getEnchantment(context, "enchant"));
                                int level = IntegerArgumentType.getInteger(context, "level");
                                switch (StringArgumentType.getString(context, "type")) {
                                    case "global" -> {
                                        EnchantsList.addEnchants(item, new HashMap<>() {{
                                            put(enchant, level);
                                        }}, false);
                                        EnchantsList.saveConfig(true);
                                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully added " + enchant + " to " + item + "!"), false);
                                    }
                                    case "current" -> {
                                        EnchantsList.addNBTEnchant(stack, enchant.toString(), level);
                                        player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully added " + enchant + " NBT to " + item + "!"), false);
                                    }
                                }
                                return 1;
                            })
                         )
                     )))
            .then(CommandManager.literal("remove")
                .then(CommandManager.argument("type", StringArgumentType.word())
                    .suggests(EnchantCommands::enchantTypes)
                    .then(CommandManager.argument("enchant", EnchantmentArgumentType.enchantment())
                        .requires(source -> source.hasPermissionLevel(1))
                        .executes(context -> {
                            ServerPlayerEntity player = context.getSource().getPlayer();
                            if (player == null) {
                                return 1;
                            }
                            ItemStack stack = player.getMainHandStack();
                            if (stack.isEmpty() || stack.getItem() == Items.AIR) {
                                context.getSource().sendFeedback(new TranslatableText("enchantgiver.error.no_item"), false);
                                return 1;
                            }
                            Identifier item = Registry.ITEM.getId(player.getMainHandStack().getItem());
                            Identifier enchant = Registry.ENCHANTMENT.getId(EnchantmentArgumentType.getEnchantment(context, "enchant"));
                            switch (StringArgumentType.getString(context, "type")) {
                                case "global" -> {
                                    EnchantsList.removeEnchant(item, enchant);
                                    EnchantsList.saveConfig(true);
                                    player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully removed " + enchant + " from " + item + "!"), false);
                                }
                                case "current" -> {
                                    EnchantsList.removeNBTEnchant(stack, enchant.toString());
                                    player.sendMessage(new LiteralText("§6[§eEnchantGiver§6] §3Successfully removed " + enchant + " NBT from " + item + "!"), false);
                                }
                            }
                            return 1;
                        })
                         )
                     )
                 )
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
        );
    }

    private static CompletableFuture<Suggestions> enchantTypes(CommandContext<ServerCommandSource> ctx, SuggestionsBuilder suggestion) {
        suggestion.suggest("global").suggest("current");
        return suggestion.buildFuture();
    }
}
