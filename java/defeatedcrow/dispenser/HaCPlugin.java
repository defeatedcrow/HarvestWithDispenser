package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.List;

import defeatedcrow.hac.api.cultivate.GrowingStage;
import defeatedcrow.hac.api.cultivate.IClimateCrop;
import defeatedcrow.hac.food.FoodInit;
import defeatedcrow.hac.main.item.tool.ItemScytheDC;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HaCPlugin {

	public static Item scytheBrass;
	public static Item scytheSteel;
	public static Item scytheChal;
	public static Item scytheAlmandine;
	public static Item scytheStone;
	public static Item scytheToolSteel;

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
				DispenserHervestDC.LOGGER.info("register item as shears: " + scytheAlmandine.getRegistryName()
						.toString() + " : range " + 4);
			}
			scytheStone = Item.REGISTRY.getObject(new ResourceLocation("dcs_climate:dcs_scythe_stone"));
			if (scytheStone != null) {
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(scytheStone, DispenseShears.getInstance());
				DispenserHervestDC.LOGGER
						.info("register item as shears: " + scytheStone.getRegistryName().toString() + " : range " + 2);
			}
			scytheToolSteel = Item.REGISTRY.getObject(new ResourceLocation("dcs_climate:dcs_scythe_toolsteel"));
			if (scytheToolSteel != null) {
				BlockDispenser.DISPENSE_BEHAVIOR_REGISTRY.putObject(scytheToolSteel, DispenseShears.getInstance());
				DispenserHervestDC.LOGGER.info("register item as shears: " + scytheToolSteel.getRegistryName()
						.toString() + " : range " + 6);
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

	public static boolean isHaCCrop(IBlockState state) {
		if (DispenserHervestDC.loadedHaC && state != null && (state.getBlock() instanceof IClimateCrop)) {
			return true;
		}
		return false;
	}

	public static boolean isSeaweed(IBlockState state) {
		return state.getBlock() == FoodInit.cropSeaweed;
	}

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

	public static List<ItemStack> harvestSeaweed(World world, BlockPos pos, IBlockState state, int fortune) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		// 頂点の検索
		BlockPos p2 = pos;
		while (isSeaweed(world.getBlockState(p2)) && isSeaweed(world.getBlockState(p2.up())) && p2.getY() < 254) {
			p2 = p2.up();
		}

		// 収穫
		if (!isSeaweed(world.getBlockState(p2)) || !isSeaweed(world.getBlockState(p2.down()))) {
			return ret;
		}

		int count = 0;
		while (isSeaweed(world.getBlockState(p2)) && isSeaweed(world.getBlockState(p2.down())) && p2.getY() > 1) {
			if (world.setBlockState(p2, Blocks.WATER.getDefaultState(), 2)) {
				count++;
			}
			p2 = p2.down();
		}
		if (count > 0) {
			ret.add(new ItemStack(FoodInit.seeds, count, 8));
		}
		return ret;
	}
}
