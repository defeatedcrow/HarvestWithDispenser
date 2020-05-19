package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CactusBlock;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.DispenserBlock;
import net.minecraft.block.IGrowable;
import net.minecraft.block.StemBlock;
import net.minecraft.block.SugarCaneBlock;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.dispenser.IDispenseItemBehavior;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DispenseShears implements IDispenseItemBehavior {

	@Override
	@Nonnull
	public ItemStack dispense(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
		if (RegisterShearsJson.INSTANCE.getRange(stack) > 0) {
			return harvestCrop(source, stack, RegisterShearsJson.INSTANCE.getRange(stack));
		} else {
			return stack;
		}
	}

	private boolean isShears(ItemStack stack) {
		return stack.getItem() instanceof ShearsItem;
	}

	@Nonnull
	private ItemStack harvestCrop(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		Direction face = source.getBlockState().get(DispenserBlock.FACING);
		BlockPos pos = source.getBlockPos();
		int range = r;
		if (!world.isRemote) {
			boolean success = false;
			IItemHandler inv = null;
			for (Direction f : Direction.values()) {
				if (world.getTileEntity(pos.offset(f)) != null && !(world.getTileEntity(pos
						.offset(f)) instanceof DispenserTileEntity)) {
					LazyOptional<IItemHandler> inv2 = world.getTileEntity(pos.offset(f))
							.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
					if (inv2.isPresent()) {
						inv = inv2.orElse(null);
						break;
					}
				}
			}

			BlockPos pos2 = pos.offset(face, range + 1);
			for (int y = -1; y <= 1; y++) {
				for (int x = -range; x <= range; x++) {
					for (int z = -range; z <= range; z++) {
						BlockPos p1 = new BlockPos(pos2.getX() + x, pos2.getY() - y, pos2.getZ() + z);
						BlockState s1 = world.getBlockState(p1);

						List<ItemStack> list = new ArrayList<ItemStack>();

						if (s1.getBlock() instanceof CropsBlock) {
							CropsBlock crop = (CropsBlock) s1.getBlock();
							if (crop.isMaxAge(s1)) {
								List<ItemStack> l1 = crop.getDrops(s1, (ServerWorld) world, p1, world
										.getTileEntity(p1));

								if (!l1.isEmpty()) {
									list.addAll(l1);
									world.setBlockState(p1, crop.getDefaultState());
								}
							}
						} else if (s1.getMaterial() == Material.GOURD) {
							List<ItemStack> l1 = s1.getBlock().getDrops(s1, (ServerWorld) world, p1, null);
							if (!l1.isEmpty()) {
								list.addAll(l1);
								world.setBlockState(p1, Blocks.AIR.getDefaultState());
							}
						} else if (s1.getBlock() instanceof CactusBlock || s1.getBlock() instanceof SugarCaneBlock || s1
								.getBlock() instanceof BambooBlock) {
							// Search soil
							BlockPos bottom = null;
							for (int i = 1; p1.getY() - i > 1; i++) {
								if (world.getBlockState(p1.down(i)).getBlock() != s1.getBlock()) {
									bottom = p1.down(i);
									break;
								}
							}
							if (bottom != null) {
								ArrayList<BlockPos> crops = Lists.newArrayList();
								for (int i = 2; bottom.getY() + i < 255; i++) {
									if (world.getBlockState(bottom.up(i)).getBlock() != s1.getBlock()) {
										break;
									}
									crops.add(bottom.up(i));
								}
								if (!crops.isEmpty()) {
									crops.sort(Collections.reverseOrder());
									crops.forEach(target -> {
										List<ItemStack> l1 = s1.getBlock()
												.getDrops(s1, (ServerWorld) world, target, null);
										if (!l1.isEmpty()) {
											list.addAll(l1);
											world.setBlockState(target, Blocks.AIR.getDefaultState());
										}
									});
								}
							}
						} else if (!(s1.getBlock() instanceof StemBlock) && s1.getBlockState() instanceof IGrowable) {
							IGrowable crop = (IGrowable) s1.getBlockState();
							if (!crop.canGrow(world, p1, s1, false)) {
								List<ItemStack> l1 = s1.getBlock().getDrops(s1, (ServerWorld) world, p1, null);

								if (!l1.isEmpty()) {
									list.addAll(l1);
									world.setBlockState(p1, s1.getBlock().getDefaultState());
								}
							}
						}

						if (!list.isEmpty()) {
							success = true;
							for (int i = 0; i < list.size(); i++) {
								ItemStack c1 = list.get(i);
								ItemStack ret = ItemStack.EMPTY;
								if (inv != null) {
									int slot = 0;
									while (slot < inv.getSlots()) {
										ret = inv.insertItem(slot, c1, false);
										if (ret.isEmpty()) {
											break;
										} else {
											slot++;
										}
									}
								} else {
									ret = c1;
								}
								if (!ret.isEmpty()) {
									ItemEntity drop = new ItemEntity(world, pos.offset(face.getOpposite())
											.getX() + 0.5D, pos.offset(face.getOpposite()).getY() + 0.5D, pos
													.offset(face.getOpposite()).getZ() + 0.5D, ret);
									world.addEntity(drop);
								}
							}
						}
					}
				}
			}

			if (success) {
				if (stack.attemptDamageItem(1, world.rand, null)) {
					stack.setCount(0);
				}
				world.playEvent(1000, pos, 0);
				world.playEvent(2000, pos, face.getIndex());
			}
		}

		return stack;

	}

}