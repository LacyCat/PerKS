package cat.lacycat.perKS.Ability;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public interface IAbility extends org.bukkit.event.Listener {
    /**
     * 능력의 고유 식별자이자 이름을 구합니다
     * @return 이름
     */
    String getAbilityName();
    String getID();
    ItemStack getBook();

    /**
     * 미사용 - 나중에 사용 될 예정입니다.
     * @return 표시 아이템을 구합니다.
     */
    ItemStack getShow();
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
