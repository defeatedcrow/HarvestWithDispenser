package defeatedcrow.dispenser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.google.common.collect.Maps;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;

public class RegisterShearsJson {

	public static final HashMap<Item, Integer> rangeMap = new HashMap<Item, Integer>();

	private RegisterShearsJson() {}

	public static final RegisterShearsJson INSTANCE = new RegisterShearsJson();

	public HashMap<Item, Integer> getHeatMap() {
		return rangeMap;
	}

	public static boolean isEmpty(ItemStack item) {
		if (item == null) {
			return true;
		}
		return item.getItem() == null || item.getItem() == null;
	}

	public void registerMaterial(Item item, int r) {
		if (item == null)
			return;
		if (!rangeMap.containsKey(item)) {
			rangeMap.put(item, r);
			DispenserHervestDC.LOGGER
					.info("register item as shears: " + item.getRegistryName().toString() + " : range " + r);
			String mapName = item.getRegistryName().toString();
			Map<String, Integer> map = Maps.newHashMap();
			map.put("range", r);
			intMap.put(mapName, map);
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
					DispenserHervestDC.LOGGER.info("fail to register target item from json: " + name);
					return;
				}
			}

			Item item = Item.REGISTRY.getObject(new ResourceLocation(modid, itemName));
			if (item != null) {
				DispenserHervestDC.LOGGER.info("register target item from json: " + modid + ":" + itemName);
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
						range = MathHelper.floor_float(f);
					}
					INSTANCE.registerItemName(name, range);
				}
			}
		} else {
			DispenserHervestDC.LOGGER.info("no shears json data.");
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

	// 生成は初回のみ
	public static void post() {

		if (dir != null) {
			try {
				if (!dir.exists() && !dir.createNewFile()) {
					return;
				} else if (!jsonMap.isEmpty()) {
					DispenserHervestDC.LOGGER.info("item resistant data json is already exists.");
					return;
				}

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

	public static void setDir(File file) {
		dir = new File(file, "defeatedcrow/dispenser/shears_item.json");
		if (dir.getParentFile() != null) {
			dir.getParentFile().mkdirs();
		}
	}
}
