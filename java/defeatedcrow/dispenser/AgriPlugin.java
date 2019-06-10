package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.List;

import com.infinityraider.agricraft.tiles.TileEntityCrop;

import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class AgriPlugin {

	public static boolean isAgriCrop(World world, BlockPos pos) {
		if (!DispenserHervestDC.loadedAgri) {
			return false;
		} else {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityCrop) {
				return true;
			}
		}
		return false;
	}

	public static boolean isHarvestableCrop(World world, BlockPos pos) {
		if (!DispenserHervestDC.loadedAgri) {
			return false;
		} else {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityCrop) {
				return ((TileEntityCrop) tile).isMature();
			}
		}
		return false;
	}

	public static List<ItemStack> getHarvest(World world, BlockPos pos, int fortune) {
		List<ItemStack> ret = new ArrayList<ItemStack>();
		if (!isHarvestableCrop(world, pos)) {
			return ret;
		} else {
			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileEntityCrop) {
				TileEntityCrop crop = (TileEntityCrop) tile;
				crop.getDrops((stack) -> add(ret, stack), false, false, true);
			}
		}
		return ret;
	}

	static void add(List<ItemStack> list, ItemStack item) {
		if (item != null && item.getItem() != null && !item.isEmpty()) {
			list.add(item);
		}
	}

	public static void setGroundState(World world, BlockPos pos) {
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileEntityCrop) {
			TileEntityCrop crop = (TileEntityCrop) tile;
			crop.setGrowthStage(0);
		}
	}

}
