package defeatedcrow.dispenser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.Mod.EventHandler;
import cpw.mods.fml.common.Mod.Instance;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraft.block.BlockDispenser;
import net.minecraft.init.Items;
import net.minecraft.item.Item;

@Mod(
		modid = DispenserHervestDC.MOD_ID,
		name = DispenserHervestDC.MOD_NAME,
		version = DispenserHervestDC.MOD_MEJOR + "." + DispenserHervestDC.MOD_MINOR + "."
				+ DispenserHervestDC.MOD_BUILD,
		dependencies = DispenserHervestDC.MOD_DEPENDENCIES,
		acceptedMinecraftVersions = DispenserHervestDC.MOD_ACCEPTED_MC_VERSIONS,
		useMetadata = true)
public class DispenserHervestDC {
	public static final String MOD_ID = "dcs_dispenser";
	public static final String MOD_NAME = "HarvestWithDispenser";
	public static final int MOD_MEJOR = 1;
	public static final int MOD_MINOR = 0;
	public static final int MOD_BUILD = 0;
	public static final String MOD_DEPENDENCIES = "after:DCsAppleMilk";
	public static final String MOD_ACCEPTED_MC_VERSIONS = "[1.7.10]";
	public static final String PACKAGE_BASE = "dcs";
	public static final String PACKAGE_ID = "dcs_dispenser";

	@Instance("dcs_dispenser")
	public static DispenserHervestDC instance;

	public static final Logger LOGGER = LogManager.getLogger(PACKAGE_ID);

	@EventHandler
	public void preInit(FMLPreInitializationEvent event) {
		if (Loader.isModLoaded("DCsAppleMilk")) {
			AMTPlugin.loadedAMT = true;
		}
		RegisterShearsJson.INSTANCE.setDir(event.getModConfigurationDirectory());
		RegisterShearsJson.INSTANCE.pre();
	}

	@EventHandler
	public void init(FMLInitializationEvent event) {
		if (Loader.isModLoaded("DCsAppleMilk")) {
			AMTPlugin.init();
		}
		RegisterShearsJson.INSTANCE.registerMaterial(Items.shears, 2);
		for (Item item : RegisterShearsJson.INSTANCE.rangeMap.keySet()) {
			BlockDispenser.dispenseBehaviorRegistry.putObject(item, DispenseShears.getInstance());
		}
	}

	@EventHandler
	public void postInit(FMLPostInitializationEvent event) {
		RegisterShearsJson.INSTANCE.post();
	}

}
