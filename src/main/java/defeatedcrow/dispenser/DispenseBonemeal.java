package defeatedcrow.dispenser;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockState;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.item.BoneMealItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class DispenseBonemeal implements IDispenseItemBehavior {

	@Override
	@Nonnull
	public ItemStack dispense(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
		if (stack.getItem() instanceof BoneMealItem) {
			if (apply(source, stack, 2)) {
				source.getWorld().playEvent(1000, source.getBlockPos(), 0);
				source.getWorld().playEvent(2000, source.getBlockPos(), source.getBlockState()
						.get(DispenserBlock.FACING).getIndex());
			}
			return stack;
		} else {
			return stack;
		}
	}

	private static boolean apply(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		Direction face = source.getBlockState().get(DispenserBlock.FACING);
		BlockPos pos = source.getBlockPos();
		int range = r;
		if (!world.isRemote) {
			boolean success = false;

			BlockPos pos2 = pos.offset(face, range + 1);
			for (int y = -1; y <= 1; y++) {
				for (int x = -range; x <= range; x++) {
					for (int z = -range; z <= range; z++) {
						BlockPos p1 = new BlockPos(pos2.getX() + x, pos2.getY() - y, pos2.getZ() + z);
						BlockState s1 = world.getBlockState(p1);

						if (s1.getBlock() instanceof IGrowable) {
							if (BoneMealItem.applyBonemeal(stack.copy(), world, p1) || BoneMealItem.growSeagrass(stack
									.copy(), world, p1, (Direction) null)) {
								world.playEvent(2005, p1, 0);
								success = true;
							}
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