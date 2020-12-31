package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;

import net.minecraft.block.BambooBlock;
import net.minecraft.block.BeehiveBlock;
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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.entity.IShearable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ShearsItem;
import net.minecraft.tags.BlockTags;
import net.minecraft.tileentity.BeehiveTileEntity;
import net.minecraft.tileentity.DispenserTileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.IForgeShearable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class DispenseShears implements IDispenseItemBehavior {

	@Override
	@Nonnull
	public ItemStack dispense(@Nonnull IBlockSource source, @Nonnull ItemStack stack) {
		if (RegisterShearsJson.INSTANCE.getRange(stack) > 0 && harvestCrop(source, stack, RegisterShearsJson.INSTANCE
				.getRange(stack))) {
			return stack;
		} else if (isShears(stack)) {
			if (shearComb(source, stack, RegisterShearsJson.INSTANCE
					.getRange(stack)) || shear(source, stack, RegisterShearsJson.INSTANCE.getRange(stack))) {
				source.getWorld().playEvent(1000, source.getBlockPos(), 0);
				source.getWorld().playEvent(2000, source.getBlockPos(), source.getBlockState()
						.get(DispenserBlock.FACING).getIndex());
			}
			return stack;
		} else {
			return stack;
		}
	}

	private boolean isShears(ItemStack stack) {
		return stack.getItem() instanceof ShearsItem;
	}

	private boolean harvestCrop(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
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
				return true;
			}
		}

		return false;

	}

	/* method from vanilla */

	private static boolean shearComb(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		Direction face = source.getBlockState().get(DispenserBlock.FACING);
		BlockPos pos = source.getBlockPos().offset(face);
		BlockState blockstate = world.getBlockState(pos);
		if (blockstate.isIn(BlockTags.BEEHIVES)) {
			int i = blockstate.get(BeehiveBlock.HONEY_LEVEL);
			if (i >= 5) {
				world.playSound((PlayerEntity) null, pos, SoundEvents.BLOCK_BEEHIVE_SHEAR, SoundCategory.BLOCKS, 1.0F, 1.0F);
				BeehiveBlock.dropHoneyComb(world, pos);
				((BeehiveBlock) blockstate.getBlock())
						.takeHoney(world, blockstate, pos, (PlayerEntity) null, BeehiveTileEntity.State.BEE_RELEASED);
				if (stack.attemptDamageItem(1, world.rand, null)) {
					stack.setCount(0);
				}
				return true;
			}
		}

		return false;
	}

	private static boolean shear(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
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

			List<ItemStack> list = new ArrayList<ItemStack>();

			for (LivingEntity livingentity : world.getEntitiesWithinAABB(LivingEntity.class, new AxisAlignedBB(pos
					.offset(face)), EntityPredicates.NOT_SPECTATING)) {
				if (livingentity instanceof IForgeShearable) {
					IForgeShearable ishearable = (IForgeShearable) livingentity;
					int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);
					if (ishearable.isShearable(stack, world, pos.offset(face))) {
						list.addAll(ishearable.onSheared(null, stack, world, pos.offset(face), fortune));
					}
				} else if (livingentity instanceof IShearable) {
					IShearable ishearable = (IShearable) livingentity;
					if (ishearable.isShearable()) {
						ishearable.shear(SoundCategory.BLOCKS);
						success = true;
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
						ItemEntity drop = new ItemEntity(world, pos.offset(face.getOpposite()).getX() + 0.5D, pos
								.offset(face.getOpposite()).getY() + 0.5D, pos.offset(face.getOpposite()).getZ() + 0.5D,
								ret);
						world.addEntity(drop);
					}
				}
			}

			if (success) {
				if (stack.attemptDamageItem(1, world.rand, null)) {
					stack.setCount(0);
				}
				return true;
			}
		}

		return false;
	}

}