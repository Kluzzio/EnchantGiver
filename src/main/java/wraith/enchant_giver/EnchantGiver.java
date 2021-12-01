package wraith.enchant_giver;

import com.google.gson.JsonObject;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;

public class EnchantGiver implements ModInitializer {

    public static final String MOD_ID = "enchant_giver";
    public static final Logger LOGGER = LogManager.getLogger();
    public static boolean SHOW_ENCHANTS = true;

    @Override
    public void onInitialize() {
        EnchantCommands.registerCommands();
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

}
