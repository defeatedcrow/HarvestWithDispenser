package defeatedcrow.dispenser;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.block.DispenserBlock;
import net.minecraft.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;

@Mod("dcs_dispenser")
public final class DispenserHarvestDC {

	public static final Logger LOGGER = LogManager.getLogger();

	public static boolean loadedHaC = false;
	public static boolean loadedAgri = false;

	public static DispenserHarvestDC INSTANCE;

	public DispenserHarvestDC() {
		INSTANCE = this;

		FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setupCommon);
		MinecraftForge.EVENT_BUS.register(this);

		RegisterShearsJson.setDir(FMLPaths.CONFIGDIR.get());
	}

	public void setupCommon(final FMLCommonSetupEvent event) {
		RegisterShearsJson.pre();
		RegisterShearsJson.post();
		DispenserBlock.registerDispenseBehavior(Items.BONE_MEAL, new DispenseBonemeal());
	}

}
