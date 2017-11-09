package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.List;

import defeatedcrow.hac.api.blockstate.DCState;
import defeatedcrow.hac.api.cultivate.GrowingStage;
import defeatedcrow.hac.api.cultivate.IClimateCrop;
import defeatedcrow.hac.core.base.ClimateDoubleCropBase;
import defeatedcrow.hac.main.item.tool.ItemScytheDC;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class HaCPlugin {

	public static boolean loadedHaC = false;

	public static Item scytheBrass;
	public static Item scytheSteel;
	public static Item scytheChal;

	public static void init() {
		if (loadedHaC) {
			scytheBrass = Item.REGISTRY.getObject(new ResourceLocation("dcs_climate:dcs_scythe_brass"));
			if (scytheBrass != null) {
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(scytheBrass, DispenseShears.getInstance());
				DispenserHervestDC.LOGGER
						.info("register item as shears: " + scytheBrass.getRegistryName().toString() + " : range " + 3);
			}
			scytheSteel = Item.REGISTRY.getObject(new ResourceLocation("dcs_climate:dcs_scythe_steel"));
			if (scytheSteel != null) {
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(scytheSteel, DispenseShears.getInstance());
				DispenserHervestDC.LOGGER
						.info("register item as shears: " + scytheSteel.getRegistryName().toString() + " : range " + 4);
			}
			scytheChal = Item.REGISTRY.getObject(new ResourceLocation("dcs_climate:dcs_scythe_chalcedony"));
			if (scytheChal != null) {
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(scytheChal, DispenseShears.getInstance());
				DispenserHervestDC.LOGGER
						.info("register item as shears: " + scytheChal.getRegistryName().toString() + " : range " + 5);
			}
		}
	}

	/* scythe */

	public static boolean isScythe(ItemStack item) {
		if (!loadedHaC || item == null || item.getItem() == null) {
			return false;
		} else {
			return item.getItem() instanceof ItemScytheDC;
		}
	}

	public static int getScytheTier(ItemStack item) {
		if (!isScythe(item)) {
			return 0;
		} else {
			ItemScytheDC scythe = (ItemScytheDC) item.getItem();
			return scythe.range + 1;
		}
	}

	/* crop */

	public static boolean isHarvestableCrop(IBlockState state) {
		if (!loadedHaC || state == null || !(state.getBlock() instanceof IClimateCrop)) {
			return false;
		} else {
			IClimateCrop crop = (IClimateCrop) state.getBlock();
			if (crop.getCurrentStage(state) == GrowingStage.GROWN) {
				return true;
			}
		}
		return false;
	}

	public static List<ItemStack> getHarvest(IBlockState state) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		if (!isHarvestableCrop(state)) {
			return ret;
		} else {
			IClimateCrop crop = (IClimateCrop) state.getBlock();
			ret.addAll(crop.getCropItems(state, 0));
			return ret;
		}
	}

	public static IBlockState setGroundState(IBlockState state) {
		if (state.getBlock() instanceof ClimateDoubleCropBase) {
			return state.withProperty(DCState.STAGE8, 4);
		} else {
			return state.withProperty(DCState.STAGE4, 0);
		}
	}
}
