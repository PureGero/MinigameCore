package net.justminecraft.minigames.minigamecore;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.*;

public class TopCoins {

    private static final String TOPCOINS_CSV = "topcoins.csv";
    private static final List<TopCoins> toplist = new ArrayList<>();
    private static final Map<UUID, TopCoins> toplistIndex = new HashMap<>();

    private final UUID uuid;
    private int coins;

    private TopCoins(UUID uuid, int coins) {
        this.uuid = uuid;
        this.coins = coins;
    }

    public static void load(File folder) {
        File file = new File(folder, TOPCOINS_CSV);
        if (file.isFile()) {
            try {
                toplist.clear();
                toplistIndex.clear();
                for (String line : Files.readAllLines(file.toPath())) {
                    try {
                        String[] parts = line.split(",");
                        toplist.add(toplistIndex.computeIfAbsent(UUID.fromString(parts[0]), uuid -> new TopCoins(uuid, Integer.parseInt(parts[1]))));
                    } catch (Exception ignored) {}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void save(File folder) {
        sort();

        List<String> lines = new ArrayList<>();

        for (TopCoins coin : toplist) {
            lines.add(coin.uuid + "," + coin.coins);
        }

        folder.mkdirs();

        try {
            Files.write(new File(folder, TOPCOINS_CSV).toPath(), lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sort() {
        // Sort is basically O(n) since the list is already mostly sorted
        for (int i = 1; i < toplist.size(); i++) {
            sort(i);
        }
    }

    private static void sort(int i) {
        if (i <= 0) {
            return;
        }

        if (toplist.get(i - 1).coins < toplist.get(i).coins) {
            swap(i - 1, i);
            sort(i - 1);
        }
    }

    private static void swap(int i, int j) {
        TopCoins temp = toplist.get(i);
        toplist.set(i, toplist.get(j));
        toplist.set(j, temp);
    }

    public static void updateCoins(PlayerData pd) {
        if (pd.money > 0) {
            TopCoins topCoins = toplistIndex.computeIfAbsent(pd.uuid, uuid -> {
                TopCoins coins = new TopCoins(uuid, 0);
                toplist.add(coins);
                return coins;
            });
            topCoins.coins = pd.money;
        }
    }

    public static boolean command(CommandSender sender, String label, String[] args) {
        int page = 1;
        try {
            if (args.length > 0)
                page = Integer.parseInt(args[0]);
            if (page < 1 || page - 1 > toplist.size() / 10)
                throw new NumberFormatException();
        } catch (NumberFormatException e) {
            sender.sendMessage(ChatColor.RED + "'" + args[0] + "' is not a valid page.");
            return false;
        }
        sender.sendMessage(ChatColor.YELLOW + " --- Top Coins (Page " + page + ") --- ");
        sender.sendMessage(ChatColor.GOLD + "Next page: /" + label + " " + (page + 1));
        for (int i = page * 10 - 10; i < page * 10 && i < toplist.size(); i++) {
            OfflinePlayer p = Bukkit.getOfflinePlayer(toplist.get(i).uuid);
            sender.sendMessage(ChatColor.YELLOW + (sender.getName().equals(p.getName()) ? ChatColor.BOLD.toString() : "") + " " + (i + 1) + ". " + p.getName() + " (" + prettyCoins(toplist.get(i).coins) + " coins)");
        }
        return true;
    }

    private static String prettyCoins(int coins) {
        if (coins > 10000000) {
            return coins / 1000000 + "M";
        } else if (coins > 1000000) {
            return (coins / 100000) / 10.0 + "M";
        } else if (coins > 10000) {
            return coins / 1000 + "k";
        } else if (coins > 1000) {
            return (coins / 100) / 10.0 + "k";
        } else {
            return coins + "";
        }
    }
}
