package cat.lacycat.perKS.Ability.Dummy;

import cat.lacycat.perKS.Ability.AbilityTier;
import cat.lacycat.perKS.Ability.IAbility;
import cat.lacycat.perKS.Manager.Util;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class PlaceHolder implements IAbility {
    @Override
    public String getAbilityName() {
        return "null";
    }

    @Override
    public String getID() {
        return "placeholder";
    }

    @Override
    public ItemStack getBook() {
        return Util.buildBook(this, Util.tiertoname(getTier()),
                List.of(Component.text(getAbilityName(), Util.tiertocolor(getTier()), TextDecoration.BOLD),
                        Component.text("\"이 시스템의 한계를 뚫었다...\"").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC, TextDecoration.BOLD),
                        Component.empty()),
                List.of(new Util.AbilityBufInfo(Util.AbilityBufType.Else, true, Component.text("아무 일도 일어나지 않습니다.")),
                        new Util.AbilityBufInfo(Util.AbilityBufType.Else, false, Component.text("아무 일도 일어나지 않습니다.")))
                );
    }

    @Override
    public ItemStack getShow() {
        ItemStack nullpaper = new ItemStack(Material.PAPER);
        ItemMeta meta = nullpaper.getItemMeta();
        meta.customName(Component.text("여백의 미").decoration(TextDecoration.ITALIC,false));
        nullpaper.setItemMeta(meta);
        return nullpaper;
    }

    @Override
    public AbilityTier getTier() {
        return AbilityTier.dummy;
    }

    @Override
    public void onActivated(Player p) {

    }

    @Override
    public void onUpdated() {

    }

    @Override
    public int getMaxLevel() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void setLevel(int level) {

    }

    @Override
    public int getLevel() {
        return 0;
    }
}
