package net.justminecraft.minigames.minigamecore;

import org.bukkit.Material;

public class Food {
    public static int getHungerRegenValue(Material mat) {

        if(mat == Material.POTATO_ITEM)
            return 1;
        if(mat == Material.CAKE_BLOCK || mat == Material.COOKIE || mat == Material.MELON || mat == Material.POISONOUS_POTATO || mat == Material.RAW_CHICKEN
                || mat == Material.RAW_FISH || mat == Material.SPIDER_EYE)
            return 2;
        if(mat == Material.PORK || mat == Material.RAW_BEEF)
            return 3;
        if(mat == Material.APPLE || mat == Material.CARROT_ITEM || mat == Material.GOLDEN_APPLE || mat == Material.ROTTEN_FLESH)
            return 4;
        if(mat == Material.BREAD || mat == Material.COOKED_FISH)
            return 5;
        if(mat == Material.BAKED_POTATO || mat == Material.COOKED_CHICKEN || mat == Material.GOLDEN_CARROT || mat == Material.MUSHROOM_SOUP)
            return 6;
        if(mat == Material.COOKED_BEEF || mat == Material.GRILLED_PORK || mat == Material.PUMPKIN_PIE)
            return 8;
        return 0;

    }

    public static double getSaturationValue(Material mat) {

        if(mat == Material.CAKE_BLOCK || mat == Material.COOKIE)
            return 0.4D;
        if(mat == Material.POTATO_ITEM)
            return 0.6D;
        if(mat == Material.ROTTEN_FLESH)
            return 0.8D;
        if(mat == Material.MELON || mat == Material.POISONOUS_POTATO || mat == Material.RAW_CHICKEN || mat == Material.RAW_FISH)
            return 1.2D;
        if(mat == Material.PORK || mat == Material.RAW_BEEF)
            return 1.8D;
        if(mat == Material.APPLE)
            return 2.4D;
        if(mat == Material.SPIDER_EYE)
            return 3.2D;
        if(mat == Material.CARROT_ITEM || mat == Material.PUMPKIN_PIE)
            return 4.8D;
        if(mat == Material.BREAD || mat == Material.COOKED_FISH)
            return 6D;
        if(mat == Material.BAKED_POTATO || mat == Material.COOKED_CHICKEN || mat == Material.MUSHROOM_SOUP)
            return 7.2D;
        if(mat == Material.GOLDEN_APPLE)
            return 9.6D;
        if(mat == Material.COOKED_BEEF || mat == Material.GRILLED_PORK)
            return 12.8D;
        if(mat == Material.GOLDEN_CARROT)
            return 14.4D;
        return 0;

    }
}
