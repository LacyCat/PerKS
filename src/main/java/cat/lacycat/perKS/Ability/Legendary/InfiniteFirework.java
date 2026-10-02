package cat.lacycat.perKS.Ability.Legendary;

import cat.lacycat.perKS.Ability.AbilityTier;
import cat.lacycat.perKS.Ability.IAbility;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InfiniteFirework implements IAbility {
    public static List<Player> list = new ArrayList<>();
    @Override
    public String getAbilityName() {
        return "무한 폭죽";
    }

    @Override
    public ItemStack getBook() {
        return null;
    }

    @Override
    public ItemStack getShow() {
        return null;
    }

    @Override
    public AbilityTier getTier() {
        return AbilityTier.legendary;
    }

    @Override
    public void onActivated(Player p) {
        list.add(p);
    }

    @Override
    public void onUpdated() {

    }

    @Override
    public int getMaxLevel() {
        return 0;
    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public int getLevel() {
        return 0;
    }
}
