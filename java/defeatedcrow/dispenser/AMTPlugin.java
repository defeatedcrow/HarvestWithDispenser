package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.List;

import cpw.mods.fml.common.registry.GameRegistry;
import mods.defeatedcrow.api.plants.IRightClickHarvestable;
import net.minecraft.block.Block;
import net.minecraft.block.BlockDispenser;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class AMTPlugin {

	public static boolean loadedAMT = false;

	public static Item knife;

	public static void init() {
		if (loadedAMT) {
			knife = GameRegistry.findItem("DCsAppleMilk", "defeatedcrow.chalcedonyShears");
			if (knife != null) {
				BlockDispenser.dispenseBehaviorRegistry.putObject(knife, DispenseShears.getInstance());
				DispenserHervestDC.LOGGER
						.info("register item as shears: " + knife.getUnlocalizedName() + " : range " + 3);
			}
		}
	}

	/* scythe */

	public static boolean isScythe(ItemStack item) {
		if (!loadedAMT || item == null || item.getItem() == null) {
			return false;
		} else {
			return item.getItem() == knife;
		}
	}

	public static int getScytheTier(ItemStack item) {
		if (!isScythe(item)) {
			return 0;
		} else {
			return 3;
		}
	}

	/* crop */

	public static boolean isHarvestableCrop(Block block, int meta) {
		if (!loadedAMT || block == null || !(block instanceof IRightClickHarvestable)) {
			return false;
		} else {
			IRightClickHarvestable crop = (IRightClickHarvestable) block;
			if (crop.getCropItem(meta) != null) {
				return true;
			}
		}
		return false;
	}

	public static List<ItemStack> getHarvest(Block block, int meta) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		if (!isHarvestableCrop(block, meta)) {
			return ret;
		} else {
			IRightClickHarvestable crop = (IRightClickHarvestable) block;
			ret.add(crop.getCropItem(meta));
			return ret;
		}
	}

	public static int getInicial(Block block, int meta) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		if (!isHarvestableCrop(block, meta)) {
			return 0;
		} else {
			IRightClickHarvestable crop = (IRightClickHarvestable) block;
			return crop.getInitialMetadata(meta);
		}
	}
}
