package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.List;

import defeatedcrow.hac.api.cultivate.GrowingStage;
import defeatedcrow.hac.api.cultivate.IClimateCrop;
import defeatedcrow.hac.main.item.tool.ItemScytheDC;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

public class HaCPlugin {

	public static Item scytheBrass;
	public static Item scytheSteel;
	public static Item scytheChal;
	public static Item scytheAlmandine;

	public static void init() {
		if (DispenserHervestDC.loadedHaC) {
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
			scytheAlmandine = Item.REGISTRY.getObject(new ResourceLocation("dcs_climate:dcs_scythe_garnet"));
			if (scytheAlmandine != null) {
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(scytheAlmandine, DispenseShears.getInstance());
				DispenserHervestDC.LOGGER.info(
						"register item as shears: " + scytheAlmandine.getRegistryName().toString() + " : range " + 4);
			}
		}
	}

	/* scythe */

	public static boolean isScythe(ItemStack item) {
		if (!DispenserHervestDC.loadedHaC || item == null || item.isEmpty()) {
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
		if (!DispenserHervestDC.loadedHaC || state == null || !(state.getBlock() instanceof IClimateCrop)) {
			return false;
		} else {
			IClimateCrop crop = (IClimateCrop) state.getBlock();
			if (crop.getCurrentStage(state) == GrowingStage.GROWN) {
				return true;
			}
		}
		return false;
	}

	public static List<ItemStack> getHarvest(IBlockState state, int fortune) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		if (!isHarvestableCrop(state)) {
			return ret;
		} else {
			IClimateCrop crop = (IClimateCrop) state.getBlock();
			ret.addAll(crop.getCropItems(state, fortune));
			return ret;
		}
	}

	public static IBlockState setGroundState(IBlockState state) {
		if (state.getBlock() instanceof IClimateCrop) {
			IBlockState set = ((IClimateCrop) state.getBlock()).setGroundState(state);
			return set;
		}
		return state;
	}
}
