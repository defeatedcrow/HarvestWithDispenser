package defeatedcrow.dispenser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.block.DispenserBlock;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.registries.ForgeRegistries;

public class RegisterShearsJson {

	public static final HashMap<Item, Integer> rangeMap = new HashMap<Item, Integer>();

	private RegisterShearsJson() {}

	public static final RegisterShearsJson INSTANCE = new RegisterShearsJson();

	public HashMap<Item, Integer> getHeatMap() {
		return rangeMap;
	}

	public static boolean isEmpty(ItemStack item) {
		if (item == null) {
			item = ItemStack.EMPTY;
			return true;
		}
		return item.getItem() == null || item.isEmpty();
	}

	public void registerMaterial(Item item, int r) {
		if (item == null)
			return;
		if (!rangeMap.containsKey(item)) {
			rangeMap.put(item, r);
			DispenserHarvestDC.LOGGER.info("register item as shears: " + item.getRegistryName()
					.toString() + " : range " + r);
			String mapName = item.getRegistryName().toString();
			Map<String, Integer> map = Maps.newHashMap();
			map.put("range", r);
			intMap.put(mapName, map);

			DispenserBlock.registerDispenseBehavior(item.asItem(), new DispenseShears());
		}
	}

	public int getRange(ItemStack item) {
		if (isEmpty(item))
			return 0;
		if (rangeMap.containsKey(item.getItem())) {
			int ret = rangeMap.get(item.getItem());
			return ret;
		}
		return 0;
	}

	public void registerItemName(String name, int r) {
		if (name != null) {
			String itemName = name;
			String modid = "minecraft";
			if (name.contains(":")) {
				String[] n2 = name.split(":");
				if (n2 != null && n2.length > 0) {
					if (n2.length == 1) {
						itemName = n2[0];
					} else {
						modid = n2[0];
						itemName = n2[1];
					}

				} else {
					DispenserHarvestDC.LOGGER.info("fail to register target item from json: " + name);
					return;
				}
			}

			Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(modid, itemName));
			if (item != null) {
				DispenserHarvestDC.LOGGER.info("register target item from json: " + modid + ":" + itemName);
				INSTANCE.registerMaterial(item, r);
			}
		}
	}

	/* json */
	private static Map<String, Object> jsonMap = new HashMap<String, Object>();
	protected static Map<String, Map<String, Integer>> intMap = new HashMap<String, Map<String, Integer>>();

	private static File dir = null;

	public static void startMap() {
		if (!jsonMap.isEmpty()) {
			for (Entry<String, Object> ent : jsonMap.entrySet()) {
				if (ent != null) {
					String name = ent.getKey();
					Object value = ent.getValue();
					Float f = 0F;
					int range = 0;
					if (value instanceof Map) {
						String h = ((Map) value).get("range").toString();
						f = Float.parseFloat(h);
						range = MathHelper.floor(f);
					}
					INSTANCE.registerItemName(name, range);
				}
			}
		} else {
			DispenserHarvestDC.LOGGER.info("no shears json data.");
		}
	}

	public static void pre() {
		if (dir != null) {
			try {
				if (!dir.exists() && !dir.createNewFile()) {
					return;
				}

				if (dir.canRead()) {
					FileInputStream fis = new FileInputStream(dir.getPath());
					InputStreamReader isr = new InputStreamReader(fis);
					JsonReader jsr = new JsonReader(isr);
					Gson gson = new Gson();
					Map get = gson.fromJson(jsr, Map.class);

					isr.close();
					fis.close();
					jsr.close();

					if (get != null && !get.isEmpty()) {
						jsonMap.putAll(get);

					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		startMap();
	}

	public static void post() {

		if (dir != null) {
			try {
				if (!dir.exists() && !dir.createNewFile()) {
					return;
				} else if (!jsonMap.isEmpty()) {
					DispenserHarvestDC.LOGGER.info("item resistant data json is already exists.");
					return;
				}

				RegisterShearsJson.INSTANCE.registerMaterial(Items.SHEARS, 2);

				if (dir.canWrite()) {
					FileOutputStream fos = new FileOutputStream(dir.getPath());
					OutputStreamWriter osw = new OutputStreamWriter(fos);
					JsonWriter jsw = new JsonWriter(osw);
					jsw.setIndent(" ");
					Gson gson = new Gson();
					gson.toJson(intMap, Map.class, jsw);

					osw.close();
					fos.close();
					jsw.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public static void setDir(Path path) {
		dir = new File(path.toFile(), "harvest_with_dispenser.json");
		if (dir.getParentFile() != null) {
			dir.getParentFile().mkdirs();
		}
	}
}
