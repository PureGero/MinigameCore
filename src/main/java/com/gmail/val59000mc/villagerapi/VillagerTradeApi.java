/**
 * Taken from https://dev.bukkit.org/projects/villager-trade-api
 */

package com.gmail.val59000mc.villagerapi;

import java.lang.reflect.Field;

import net.minecraft.server.v1_8_R3.EntityVillager;
import net.minecraft.server.v1_8_R3.ItemStack;
import net.minecraft.server.v1_8_R3.MerchantRecipe;
import net.minecraft.server.v1_8_R3.MerchantRecipeList;

import org.bukkit.craftbukkit.v1_8_R3.entity.CraftVillager;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Villager;
import org.bukkit.plugin.java.JavaPlugin;

public final class VillagerTradeApi extends JavaPlugin
{

    public static void clearTrades(final Villager villager)
    {

        final EntityVillager entityVillager = ((CraftVillager)villager).getHandle();

        try
        {
            final Field recipes = entityVillager.getClass().getDeclaredField("br");
            recipes.setAccessible(true);
            recipes.set(entityVillager, new MerchantRecipeList());
        }

        catch (Exception exc) {
            exc.printStackTrace();
        }

    }


    public static void addTrade(final Villager villager, final VillagerTrade villagerTrade)
    {

        final EntityVillager entityVillager = ((CraftVillager)villager).getHandle();

        try
        {
            final Field recipes = entityVillager.getClass().getDeclaredField("br");
            recipes.setAccessible(true);
            final MerchantRecipeList list = (MerchantRecipeList)recipes.get(entityVillager);
            final ItemStack primaryItem = CraftItemStack.asNMSCopy(VillagerTrade.getPrimaryItem(villagerTrade));

            MerchantRecipe recipe = null;

            if (VillagerTrade.hasSecondaryItem(villagerTrade))
            {
                final ItemStack secondaryItem = CraftItemStack.asNMSCopy(VillagerTrade.getSecondaryItem(villagerTrade));
                final ItemStack rewardItem = CraftItemStack.asNMSCopy(VillagerTrade.getRewardItem(villagerTrade));
                recipe = new MerchantRecipe(primaryItem, secondaryItem, rewardItem);
            }

            else
            {
                final ItemStack rewardItem = CraftItemStack.asNMSCopy(VillagerTrade.getRewardItem(villagerTrade));
                recipe = new MerchantRecipe(primaryItem, rewardItem);
            }

            final Field rewardXp =recipe.getClass().getDeclaredField("rewardExp");
            rewardXp.setAccessible(true);
            rewardXp.set(recipe, false);

            final Field maxUses = recipe.getClass().getDeclaredField("maxUses");
            maxUses.setAccessible(true);
            maxUses.set(recipe, 99999);

            list.add(recipe);
            recipes.set(entityVillager, list);
        }

        catch (Exception exc)
        {
            exc.printStackTrace();
        }

    }


}