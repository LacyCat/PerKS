package cat.lacycat.perKS.Ability;

import cat.lacycat.perKS.PerKS;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.Arrays;

public class BunnyLeg implements IAbility {
    private final static NamespacedKey jumpModifierKey = new NamespacedKey(PerKS.getInstance(), "bunneyleg");

    public static final double[] modi = {0.08, 0.09, 0.11};
    private Player p;
    private int level = 0;

    @Override
    public String getAbilityName() { return "토끼발"; }
    @Override
    public ItemStack getBook() {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.customName(Component.text("토끼발").decorate(TextDecoration.BOLD));
            bookMeta.setTitle("토끼발");
            bookMeta.setAuthor("일반");
            TextComponent page1 = Component.text("토끼발").color(TextColor.color(128,128,128)).decorate(TextDecoration.BOLD)
                    .appendNewline().append(Component.text("점프력을 총 " + modi[level] * 100 + "% 만큼 늘립니다").color(TextColor.color(0,0,0)));
            TextComponent page2 = Component.text("증가율 (n/100)").appendNewline().append(Component.text(Arrays.toString(modi)));
            bookMeta.addPages(page1, page2);
        }
        book.setItemMeta(bookMeta);
        return book;
    }

    @Override
    public AbilityTier getTier() {
        return AbilityTier.uncommon;
    }

    @Override
    public void onActivated(Player p) {
        level = 1;
        this.p = p;
    }

    @Override
    public void onDeactivated(Player player) {
        IAbility.super.onDeactivated(player);

        if (player != null) {
            AttributeInstance jump = player.getAttribute(Attribute.JUMP_STRENGTH);
            if (jump != null) {
                jump.removeModifier(jumpModifierKey);
            }
        }
    }

    @Override
    public void onUpdated() {
        AttributeInstance jump = p.getAttribute(Attribute.JUMP_STRENGTH);
        if (jump == null) return;

        jump.removeModifier(jumpModifierKey);

        AttributeModifier modifier = new AttributeModifier(
                jumpModifierKey, // 고유 키
                modi[level - 1],             // 더할 수치 (기본 0.42 + 0.4 = 0.82)
                AttributeModifier.Operation.MULTIPLY_SCALAR_1 // 연산 방식
        );
        jump.addModifier(modifier);
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getLevel() { return level; }
}
