package cat.lacycat.perKS.Ability.Common;

import cat.lacycat.perKS.Ability.AbilityTier;
import cat.lacycat.perKS.Ability.IAbility;
import cat.lacycat.perKS.Manager.Util;
import cat.lacycat.perKS.PerKS;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
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
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.List;

public class BunnyLeg implements IAbility {
    private final static NamespacedKey jumpModifierKey = new NamespacedKey(PerKS.getInstance(), "bunneyleg");

    public static final double[] modi = {0.08, 0.1772, 0.306692};
    private Player p;
    private int level = 0;

    @Override
    public String getAbilityName() { return "토끼발"; }
    @Override
    public String getID() { return "bunnyleg"; }
    @Override
    public ItemStack getBook() {
        return Util.buildBook(this, Util.tiertoname(getTier()),
                List.of(Component.text(getAbilityName(), Util.tiertocolor(getTier()), TextDecoration.BOLD),
                        Component.text("\"높게, 더 높게!\"").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC, TextDecoration.BOLD),
                        Component.empty()
                ),
                List.of(new Util.AbilityBufInfo(Util.AbilityBufType.Attribute, true,
                        Component.text("점프력 +" + (int) (modi[Math.min(level, modi.length - 1)] * 100) + "% (대략)"))
                )
        );


    }

    @Override
    public ItemStack getShow() {
        ItemStack rabbitleg = new ItemStack(Material.RABBIT_FOOT);
        ItemMeta meta = rabbitleg.getItemMeta();
        meta.customName(Component.text("토끼발").decoration(TextDecoration.ITALIC,false).color(Util.tiertocolor(getTier())));
        return rabbitleg;
    }

    @Override
    public AbilityTier getTier() {
        return AbilityTier.common;
    }

    @Override
    public void onActivated(Player p) {
        this.p = p;
        onUpdated();
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
                modi[level - 1],
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
