/**
 * Taken from https://dev.bukkit.org/projects/villager-trade-api
 */

package com.gmail.val59000mc.villagerapi;

import org.bukkit.inventory.ItemStack;

public final class VillagerTrade
{


    private ItemStack primaryItem;
    private ItemStack secondaryItem;
    private ItemStack rewardItem;


    public VillagerTrade(final ItemStack primaryItem, final ItemStack secondaryItem, final ItemStack rewardItem)
    {
        this.primaryItem = primaryItem;
        this.secondaryItem = secondaryItem;
        this.rewardItem = rewardItem;
    }


    public VillagerTrade(final ItemStack primarySlot, final ItemStack rewardItem)
    {
        this.primaryItem = primarySlot;
        this.rewardItem = rewardItem;
    }


    @Deprecated
    public static boolean hasItem2(final VillagerTrade villagerTrade)
    {
        return villagerTrade.secondaryItem != null;
    }


    public static boolean hasSecondaryItem(final VillagerTrade villagerTrade)
    {
        return villagerTrade.secondaryItem != null;
    }


    @Deprecated
    public static ItemStack getItem1(final VillagerTrade villagerTrade)
    {
        return villagerTrade.secondaryItem;
    }


    public static ItemStack getPrimaryItem(final VillagerTrade villagerTrade)
    {
        return villagerTrade.primaryItem;
    }


    @Deprecated
    public static ItemStack getItem2(final VillagerTrade villagerTrade)
    {
        return villagerTrade.secondaryItem;
    }


    public static ItemStack getSecondaryItem(final VillagerTrade villagerTrade)
    {
        return villagerTrade.secondaryItem;
    }


    public static ItemStack getRewardItem(final VillagerTrade villagerTrade)
    {
        return villagerTrade.rewardItem;
    }


}