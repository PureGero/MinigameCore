package net.justminecraft.minigames.minigamecore;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.UUID;

public class PlayerData {

    protected static HashMap<UUID, PlayerData> cache = new HashMap<UUID, PlayerData>();

    protected static File dir = null;
    public String name = new String();
    public int money = 0;
    public UUID uuid = UUID.randomUUID();
    public ArrayList<ItemStack> equipment = new ArrayList<ItemStack>();
    public String unlocks = new String();
    public JSONObject stats = new JSONObject();

    public static PlayerData get(UUID uuid) {
        if (cache.containsKey(uuid))
            return cache.get(uuid);
        PlayerData p = new PlayerData();
        p.uuid = uuid;
        File out = new File(dir, uuid.toString().substring(0, 2) + "/" + uuid.toString().substring(2) + ".json");
        if (out.isFile()) {
            try {
                FileReader r = new FileReader(out);
                JSONParser parse = new JSONParser();
                JSONObject o = (JSONObject) parse.parse(r);
                if (o.containsKey("name"))
                    p.name = o.get("name").toString();
                if (o.containsKey("unlocks"))
                    p.unlocks = o.get("unlocks").toString();
                if (o.containsKey("money"))
                    p.money = Integer.parseInt(o.get("money").toString());
                if (o.containsKey("stats"))
                    p.stats = (JSONObject) o.get("stats");
                if (o.containsKey("equipment"))
                    try {
                        for (Object a : (JSONArray) o.get("equipment")) {
                            if (a == null)
                                p.equipment.add(null);
                            else
                                p.equipment.add((ItemStack) fromJSON((JSONObject) a));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                r.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        cache.put(uuid, p);
        return p;
    }

    @SuppressWarnings("unchecked")
    public static void save(UUID uuid) {
        PlayerData p = cache.get(uuid);
        if (p == null) return;
        cache.remove(uuid);
        File out = new File(dir, uuid.toString().substring(0, 2) + "/" + uuid.toString().substring(2) + ".json");
        if (!out.getParentFile().isDirectory())
            out.getParentFile().mkdirs();
        JSONObject o = new JSONObject();
        o.put("name", p.name);
        o.put("money", p.money);
        o.put("unlocks", p.unlocks);
        o.put("uuid", uuid.toString());
        o.put("stats", p.stats);
        JSONArray a = new JSONArray();
        for (ItemStack k : p.equipment) {
            a.add(toJSON(k));
        }
        o.put("equipment", a);
        try {
            FileWriter w = new FileWriter(out);
            o.writeJSONString(w);
            w.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject toJSON(ConfigurationSerializable item) {
        if (item == null) return null;
        JSONObject o = new JSONObject();
        for (Entry<String, Object> e : item.serialize().entrySet()) {
            if (e.getValue() instanceof ConfigurationSerializable) {
                o.put(e.getKey(), toJSON((ConfigurationSerializable) e.getValue()));
            } else
                o.put(e.getKey(), e.getValue());
        }
        o.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY, ConfigurationSerialization.getAlias(item.getClass()));
        return o;
    }

    public static ConfigurationSerializable fromJSON(JSONObject o) {
        if (o == null) return null;
        HashMap<String, Object> map = new HashMap<String, Object>();
        for (Object k : o.keySet()) {
            if (k instanceof String) {
                Object j = o.get(k);
                if (j instanceof JSONObject)
                    j = fromJSON((JSONObject) j);
                map.put((String) k, j);
            }
        }
        return ConfigurationSerialization.deserializeObject(map);
    }

    public void save() {
        save(uuid);
    }

    public void equipsave(Player p) {
        equipment = new ArrayList<ItemStack>();
        for (ItemStack i : p.getEquipment().getArmorContents()) {
            equipment.add(i);
        }
    }

    public void equip(Player p) {
        ItemStack[] a = p.getEquipment().getArmorContents();
        for (int i = 0; i < equipment.size(); i++) {
            a[i] = equipment.get(i);
        }
        p.getEquipment().setArmorContents(a);
    }

    public int getStat(String... path) {
        try {
            Object o = stats;
            for (int i = 0; i < path.length; i++) {
                o = ((JSONObject) o).get(path[i]);
            }
            return Integer.parseInt(o.toString());
        } catch (Exception e) {
        }
        return 0;
    }

    public void setStat(int v, String... path) {
        try {
            JSONObject o = stats;
            for (int i = 0; i < path.length - 1; i++) {
                if (!o.containsKey(path[i]))
                    o.put(path[i], new JSONObject());
                o = (JSONObject) o.get(path[i]);
            }
            o.put(path[path.length - 1], v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void incrementStat(String... path) {
        setStat(getStat(path) + 1, path);
    }
}
