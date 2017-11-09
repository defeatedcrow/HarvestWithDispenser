package defeatedcrow.dispenser;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import cpw.mods.fml.common.Loader;
import net.minecraft.block.Block;
import net.minecraft.block.BlockCactus;
import net.minecraft.block.BlockDispenser;
import net.minecraft.block.BlockReed;
import net.minecraft.block.BlockStem;
import net.minecraft.block.IGrowable;
import net.minecraft.block.material.Material;
import net.minecraft.dispenser.BehaviorDefaultDispenseItem;
import net.minecraft.dispenser.IBlockSource;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityDispenser;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

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
		if (Loader.isModLoaded("DCsAppleMilk") && AMTPlugin.isScythe(stack)) {
			int r = AMTPlugin.getScytheTier(stack);
			return harvestCrop(source, stack, r);
		} else if (RegisterShearsJson.INSTANCE.getRange(stack) > 0) {
			return harvestCrop(source, stack, RegisterShearsJson.INSTANCE.getRange(stack));
		} else {
			return super.dispenseStack(source, stack);
		}
	}

	@Nonnull
	private ItemStack harvestCrop(@Nonnull IBlockSource source, @Nonnull ItemStack stack, int r) {
		World world = source.getWorld();
		int meta = source.getBlockMetadata();
		EnumFacing face = BlockDispenser.func_149937_b(meta);
		int x1 = source.getXInt() + face.getFrontOffsetX();
		int y1 = source.getYInt() + face.getFrontOffsetY();
		int z1 = source.getZInt() + face.getFrontOffsetZ();
		int range = r;
		if (range > 0) {
			boolean success = false;
			IInventory inv = null;
			for (EnumFacing f : EnumFacing.values()) {
				TileEntity t = world.getTileEntity(source.getXInt() + f.getFrontOffsetX(),
						source.getYInt() + f.getFrontOffsetY(), source.getZInt() + f.getFrontOffsetZ());
				if (t != null && !(t instanceof TileEntityDispenser) && t instanceof IInventory) {
					inv = (IInventory) t;
					break;
				}
			}

			int x2 = x1 + face.getFrontOffsetX() * r;
			int y2 = y1 + face.getFrontOffsetY() * r;
			int z2 = z1 + face.getFrontOffsetZ() * r;
			for (int x = x2 - range; x <= x2 + range; x++) {
				for (int z = z2 - range; z <= z2 + range; z++) {
					Block s1 = world.getBlock(x, y2 + 1, z);
					Block s2 = world.getBlock(x, y2, z);
					int m1 = world.getBlockMetadata(x, y2 + 1, z);
					int m2 = world.getBlockMetadata(x, y2, z);

					List<ItemStack> list = new ArrayList<ItemStack>();

					// y2 + 1
					if (AMTPlugin.isHarvestableCrop(s1, m1) && !AMTPlugin.getHarvest(s1, m1).isEmpty()) {
						list.addAll(AMTPlugin.getHarvest(s1, m1));
						world.setBlockMetadataWithNotify(x, y2 + 1, z, AMTPlugin.getInicial(s1, m1), 3);
					} else if (s1 instanceof BlockCactus || s1 instanceof BlockReed
							|| s1.getMaterial() == Material.gourd) {
						list.add(new ItemStack(s1.getItemDropped(m1, world.rand, 0), 1, 0));
						world.setBlockToAir(x, y2 + 1, z);
					} else if (!(s1 instanceof BlockStem) && s1 instanceof IGrowable) {
						IGrowable crop = (IGrowable) s1;
						if (!crop.func_149851_a(world, x, y2 + 1, z, false)) {
							List<ItemStack> l1 = new ArrayList<ItemStack>();
							l1 = s1.getDrops(world, x, y2 + 1, z, m1, 0);

							if (!l1.isEmpty()) {
								list.addAll(l1);
								world.setBlockMetadataWithNotify(x, y2 + 1, z, 0, 3);
							}
						}
					}

					// y2
					if (AMTPlugin.isHarvestableCrop(s2, m2) && !AMTPlugin.getHarvest(s2, m2).isEmpty()) {
						list.addAll(AMTPlugin.getHarvest(s2, m2));
						world.setBlockMetadataWithNotify(x, y2, z, AMTPlugin.getInicial(s2, m2), 3);
					} else if (s2 instanceof BlockCactus || s2 instanceof BlockReed
							|| s2.getMaterial() == Material.gourd) {
						list.add(new ItemStack(s2.getItemDropped(m2, world.rand, 0), 1, 0));
						world.setBlockToAir(x, y2, z);
					} else if (!(s2 instanceof BlockStem) && s2 instanceof IGrowable) {
						IGrowable crop = (IGrowable) s2;
						if (!crop.func_149851_a(world, x, y2, z, false)) {
							List<ItemStack> l1 = new ArrayList<ItemStack>();
							l1 = s2.getDrops(world, x, y2, z, m2, 0);

							if (!l1.isEmpty()) {
								list.addAll(l1);
								world.setBlockMetadataWithNotify(x, y2, z, 0, 3);
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
								while (slot < inv.getSizeInventory()) {
									ItemStack target = inv.getStackInSlot(slot);
									if (target == null) {
										inv.setInventorySlotContents(slot, c1);
										ret = null;
									} else if (OreDictionary.itemMatches(c1, target, false)
											&& c1.getTagCompound() == target.getTagCompound()) {
										int add = c1.stackSize + target.stackSize;
										if (add <= target.getMaxStackSize()) {
											target.stackSize = add;
											ret = null;
											inv.markDirty();
										} else {
											target.stackSize = target.getMaxStackSize();
											c1.stackSize = add - target.getMaxStackSize();
											ret = c1.copy();
											inv.markDirty();
										}
									} else {
										ret = c1.copy();
									}
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
								EntityItem drop = new EntityItem(world, x1 + 0.5D, y1 + 0.5D, z1 + 0.5D, ret);
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