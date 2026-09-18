package cat.lacycat.perKS.Ability;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IAbility extends org.bukkit.event.Listener {
    String getAbilityName();
    ItemStack getBook();
    AbilityTier getTier();
    void onActivated(Player p);
    default void onDeactivated(Player player) {
        org.bukkit.event.HandlerList.unregisterAll(this);
    }
    void onUpdated();
    int getMaxLevel();
    void setLevel(int level);
    int getLevel();

}
