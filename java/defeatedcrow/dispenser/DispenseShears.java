package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockCrops;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DispenseShears extends BehaviorDefaultDispenseItem {
	private static final DispenseShears INSTANCE = new DispenseShears();

	public static DispenseShears getInstance() {
		return INSTANCE;
	}

	private DispenseShears() {}

	private final BehaviorDefaultDispenseItem dispenseBehavior = new BehaviorDefaultDispenseItem();

	@Override
	@Nonnull
	public ItemStack dispenseStack(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
		if (HaCPlugin.isScythe(stack)) {
			int r = HaCPlugin.getScytheTier(stack);
			return harvestCrop(source, stack, r);
		} else if (RegisterShearsJson.INSTANCE.getRange(stack) > 0) {
			return harvestCrop(source, stack, RegisterShearsJson.INSTANCE.getRange(stack));
		} else {
			return super.dispenseStack(source, stack);
		}
	}

	private boolean isShears(ItemStack stack) {
		return stack.getItem() instanceof ItemShears;
	}

	@Nonnull
	private ItemStack harvestCrop(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		EnumFacing face = source.getBlockState().getValue(BlockDispenser.FACING);
		BlockPos pos = source.getBlockPos();
		int range = r;
		if (range > 0) {
			boolean success = false;
			IItemHandler inv = null;
			for (EnumFacing f : EnumFacing.VALUES) {
				if (world.getTileEntity(pos.offset(f)) != null
						&& !(world.getTileEntity(pos.offset(f)) instanceof TileEntityDispenser)
						&& world.getTileEntity(pos.offset(f))
								.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) {
					inv = world.getTileEntity(pos.offset(f))
							.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
					break;
				}
			}

			BlockPos pos2 = pos.offset(face, range + 1);
			for (int x = pos2.getX() - range; x <= pos2.getX() + range; x++) {
				for (int z = pos2.getZ() - range; z <= pos2.getZ() + range; z++) {
					BlockPos p1 = new BlockPos(x, pos2.getY() + 1, z);
					BlockPos p2 = new BlockPos(x, pos2.getY(), z);
					IBlockState s1 = world.getBlockState(p1);
					IBlockState s2 = world.getBlockState(p2);

					List<ItemStack> list = new ArrayList<ItemStack>();

					if (HaCPlugin.isHarvestableCrop(s1) && !HaCPlugin.getHarvest(s1).isEmpty()) {
						list.addAll(HaCPlugin.getHarvest(s1));
						world.setBlockState(p1, HaCPlugin.setGroundState(s1));
					} else if (s1.getBlock() instanceof BlockCrops) {
						BlockCrops crop = (BlockCrops) s1.getBlock();
						if (crop.isMaxAge(s1)) {
							List<ItemStack> l1 = new ArrayList<ItemStack>();
							l1 = crop.getDrops(world, p1, s1, 0);

							if (!l1.isEmpty()) {
								list.addAll(l1);
								world.setBlockState(p1, crop.getDefaultState());
							}
						}
					} else if (s1.getBlock() instanceof BlockCactus || s1.getBlock() instanceof BlockReed
							|| s1.getMaterial() == Material.GOURD) {
						list.add(new ItemStack(s1.getBlock().getItemDropped(s2, world.rand, 0), 1, 0));
						world.setBlockToAir(p1);
					} else if (!(s1.getBlock() instanceof BlockStem) && s1.getBlock() instanceof IGrowable) {
						IGrowable crop = (IGrowable) s1.getBlock();
						if (!crop.canUseBonemeal(world, world.rand, p1, s1)) {
							List<ItemStack> l1 = new ArrayList<ItemStack>();
							l1 = s1.getBlock().getDrops(world, p1, s1, 0);

							if (!l1.isEmpty()) {
								list.addAll(l1);
								world.setBlockState(p1, s1.getBlock().getDefaultState());
							}
						}
					}

					if (HaCPlugin.isHarvestableCrop(s2) && !HaCPlugin.getHarvest(s2).isEmpty()) {
						list.addAll(HaCPlugin.getHarvest(s2));
						world.setBlockState(p2, HaCPlugin.setGroundState(s2));
					} else if (s2.getBlock() instanceof BlockCrops) {
						BlockCrops crop = (BlockCrops) s2.getBlock();
						if (crop.isMaxAge(s2)) {
							List<ItemStack> l1 = new ArrayList<ItemStack>();
							l1 = crop.getDrops(world, p2, s2, 0);

							if (!l1.isEmpty()) {
								list.addAll(l1);
								world.setBlockState(p2, crop.getDefaultState());
							}
						}
					} else if (s2.getBlock() instanceof BlockCactus || s2.getBlock() instanceof BlockReed
							|| s2.getMaterial() == Material.GOURD) {
						list.add(new ItemStack(s2.getBlock().getItemDropped(s2, world.rand, 0), 1, 0));
						world.setBlockToAir(p2);
					} else if (!(s2.getBlock() instanceof BlockStem) && s2.getBlock() instanceof IGrowable) {
						IGrowable crop = (IGrowable) s2.getBlock();
						if (!crop.canUseBonemeal(world, world.rand, p2, s2)) {
							List<ItemStack> l1 = new ArrayList<ItemStack>();
							l1 = s2.getBlock().getDrops(world, p2, s2, 0);

							if (!l1.isEmpty()) {
								list.addAll(l1);
								world.setBlockState(p2, s2.getBlock().getDefaultState());
							}
						}
					}

					if (!list.isEmpty()) {
						success = true;
						for (int i = 0; i < list.size(); i++) {
							ItemStack c1 = list.get(i);
							ItemStack ret = null;
							if (inv != null) {
								int slot = 0;
								while (slot < inv.getSlots()) {
									ret = inv.insertItem(slot, c1, false);
									if (ret == null || ret.getItem() == null) {
										break;
									} else {
										slot++;
									}
								}
							} else {
								ret = c1;
							}
							if (ret != null && ret.getItem() != null) {
								EntityItem drop = new EntityItem(world, pos.offset(face).getX() + 0.5D,
										pos.offset(face).getY() + 0.5D, pos.offset(face).getZ() + 0.5D, ret);
								drop.motionX = 0.0D;
								drop.motionY = 0.0D;
								drop.motionZ = 0.0D;
								world.spawnEntityInWorld(drop);
							}
						}
					}

				}
			}

			if (success) {
				if (stack.attemptDamageItem(1, world.rand)) {
					stack = null;
				}
			}
		}

		return stack;

	}

}