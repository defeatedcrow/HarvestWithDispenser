package defeatedcrow.dispenser;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockDispenser;
import net.minecraft.block.IGrowable;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.init.Items;
import net.minecraft.item.EnumDyeColor;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DispenseBonemeal extends BehaviorDefaultDispenseItem {
	private static final DispenseBonemeal INSTANCE = new DispenseBonemeal();

	public static DispenseBonemeal getInstance() {
		return INSTANCE;
	}

	private DispenseBonemeal() {}

	private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();

	@Override
	@Nonnull
	public ItemStack dispenseStack(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
		if (isBonemeal(stack) && apply(source, stack, 2)) {
			return stack;
		} else {
			return super.dispenseStack(source, stack);
		}
	}

	private boolean isBonemeal(ItemStack stack) {
		return stack.getItem() == Items.DYE && EnumDyeColor.WHITE == EnumDyeColor.byDyeDamage(stack.getMetadata());
	}

	private boolean apply(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		EnumFacing face = source.getBlockState().getValue(BlockDispenser.FACING);
		BlockPos pos = source.getBlockPos();
		int range = r;
		boolean success = false;

		if (range > 0) {

			BlockPos pos2 = pos.offset(face, range + 1);
			for (int x = pos2.getX() - range; x <= pos2.getX() + range; x++) {
				for (int z = pos2.getZ() - range; z <= pos2.getZ() + range; z++) {
					BlockPos p = new BlockPos(x, pos2.getY(), z);
					IBlockState s = world.getBlockState(p);

					if (s.getBlock() instanceof IGrowable) {
						if (ItemDye.applyBonemeal(stack.copy(), world, p)) {
							world.playEvent(2005, p, 0);
							success = true;
						}
					}
				}
			}

			if (success) {
				stack.shrink(1);
				return true;
			}
		}

		return false;

	}

}