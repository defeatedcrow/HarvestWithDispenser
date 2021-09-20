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
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Enchantments;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.IShearable;
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
		if (DispenserHervestDC.loadedHaC && HaCPlugin.isScythe(stack)) {
			int r = HaCPlugin.getScytheTier(stack);
			if (!harvestCrop(source, stack, r))
				harvestWool(source, stack, r);
			return stack;
		} else if (RegisterShearsJson.INSTANCE
				.getRange(stack) > 0 && harvestCrop(source, stack, RegisterShearsJson.INSTANCE.getRange(stack))) {
			return stack;
		} else if (isShears(stack) && harvestWool(source, stack, 2)) {
			return stack;
		} else {
			return super.dispenseStack(source, stack);
		}
	}

	private boolean isShears(ItemStack stack) {
		return stack.getItem() instanceof ItemShears;
	}

	private boolean harvestCrop(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		EnumFacing face = source.getBlockState().getValue(BlockDispenser.FACING);
		BlockPos pos = source.getBlockPos();
		int range = r;
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);

		if (range > 0) {
			boolean success = false;
			IItemHandler inv = null;
			for (EnumFacing f : EnumFacing.VALUES) {
				if (world.getTileEntity(pos.offset(f)) != null && !(world.getTileEntity(pos
						.offset(f)) instanceof TileEntityDispenser) && world.getTileEntity(pos.offset(f))
								.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) {
					inv = world.getTileEntity(pos.offset(f))
							.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
					break;
				}
			}

			BlockPos pos2 = pos.offset(face, range + 1);
			for (int x = pos2.getX() - range; x <= pos2.getX() + range; x++) {
				for (int z = pos2.getZ() - range; z <= pos2.getZ() + range; z++) {
					for (int y = pos2.getY() + 2; y >= pos2.getY() - 2; y--) {
						BlockPos p2 = new BlockPos(x, y, z);
						IBlockState s2 = world.getBlockState(p2);

						List<ItemStack> list = new ArrayList<ItemStack>();

						if (DispenserHervestDC.loadedHaC && HaCPlugin.isHaCCrop(s2)) {
							if (HaCPlugin.isSeaweed(s2)) {
								list.addAll(HaCPlugin.harvestSeaweed(world, p2, s2, fortune));
							} else if (HaCPlugin.isHarvestableCrop(s2) && !HaCPlugin.getHarvest(s2, fortune)
									.isEmpty()) {
								list.addAll(HaCPlugin.getHarvest(s2, fortune));
								world.setBlockState(p2, HaCPlugin.setGroundState(s2));
							}
						} else if (DispenserHervestDC.loadedAgri && AgriPlugin.isAgriCrop(world, p2)) {
							if (AgriPlugin.isHarvestableCrop(world, p2) && !AgriPlugin.getHarvest(world, p2, fortune)
									.isEmpty()) {
								list.addAll(AgriPlugin.getHarvest(world, p2, fortune));
								AgriPlugin.setGroundState(world, p2);
							}
						} else if (s2.getBlock() instanceof BlockCrops) {
							BlockCrops crop = (BlockCrops) s2.getBlock();
							if (crop.isMaxAge(s2)) {
								NonNullList<ItemStack> l1 = NonNullList.create();
								crop.getDrops(l1, world, p2, s2, fortune);

								if (!l1.isEmpty()) {
									list.addAll(l1);
									world.setBlockState(p2, crop.getDefaultState());
								}
							}
						} else if (s2.getBlock() instanceof BlockCactus || s2.getBlock() instanceof BlockReed) {
							if (world.getBlockState(p2.down()).getBlock() == s2.getBlock()) {
								list.add(new ItemStack(s2.getBlock().getItemDropped(s2, world.rand, fortune), 1, 0));
								world.setBlockToAir(p2);
							}
						} else if (s2.getMaterial() == Material.GOURD) {
							list.add(new ItemStack(s2.getBlock().getItemDropped(s2, world.rand, fortune), 1, 0));
							world.setBlockToAir(p2);
						} else if (!(s2.getBlock() instanceof BlockStem) && s2.getBlock() instanceof IGrowable) {
							IGrowable crop = (IGrowable) s2.getBlock();
							if (!crop.canGrow(world, p2, s2, false)) {
								NonNullList<ItemStack> l1 = NonNullList.create();
								s2.getBlock().getDrops(l1, world, p2, s2, fortune);

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
									EntityItem drop = new EntityItem(world, pos.offset(face).getX() + 0.5D,
											pos.offset(face).getY() + 0.5D, pos.offset(face).getZ() + 0.5D, ret);
									drop.motionX = 0.0D;
									drop.motionY = 0.0D;
									drop.motionZ = 0.0D;
									world.spawnEntity(drop);
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
				return true;
			}
		}

		return false;

	}

	private boolean harvestWool(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		EnumFacing face = source.getBlockState().getValue(BlockDispenser.FACING);
		BlockPos pos = source.getBlockPos();
		int range = r;
		int fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, stack);

		if (range > 0) {
			boolean success = false;
			IItemHandler inv = null;
			for (EnumFacing f : EnumFacing.VALUES) {
				if (world.getTileEntity(pos.offset(f)) != null && !(world.getTileEntity(pos
						.offset(f)) instanceof TileEntityDispenser) && world.getTileEntity(pos.offset(f))
								.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite())) {
					inv = world.getTileEntity(pos.offset(f))
							.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, f.getOpposite());
					break;
				}
			}

			List<ItemStack> list = new ArrayList<ItemStack>();

			AxisAlignedBB aabb = new AxisAlignedBB(pos.offset(face, r));
			aabb = aabb.grow(r);
			List<Entity> entities = world.getEntitiesWithinAABB(Entity.class, aabb);

			for (Entity e : entities) {
				if (e != null && e instanceof IShearable) {
					IShearable target = (IShearable) e;
					if (target.isShearable(stack, world, e.getPosition())) {
						list.addAll(target.onSheared(stack, world, e.getPosition(), fortune));
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
						EntityItem drop = new EntityItem(world, pos.offset(face).getX() + 0.5D,
								pos.offset(face).getY() + 0.5D, pos.offset(face).getZ() + 0.5D, ret);
						drop.motionX = 0.0D;
						drop.motionY = 0.0D;
						drop.motionZ = 0.0D;
						world.spawnEntity(drop);
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
