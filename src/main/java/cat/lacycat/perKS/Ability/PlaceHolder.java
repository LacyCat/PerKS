package cat.lacycat.perKS.Ability;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.ItemMeta;

public class PlaceHolder implements IAbility{
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
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.customName(Component.text("NULL").decorate(TextDecoration.BOLD).decoration(TextDecoration.ITALIC, false));
            bookMeta.setTitle("플레이스홀더");
            bookMeta.setAuthor("없음");
        }
        book.setItemMeta(bookMeta);
        return book;
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
