package cat.lacycat.perKS.Manager;

import cat.lacycat.perKS.Ability.AbilityTier;
import cat.lacycat.perKS.Ability.IAbility;
import cat.lacycat.perKS.PerKS;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.ComponentBuilder;
import net.kyori.adventure.text.JoinConfiguration;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class Util {
    public static NamespacedKey ab_key = new NamespacedKey(PerKS.getInstance().namespace(), "ability_name");

    /**
     * 입력된 정수를 로마 숫자로 변환합니다
     * @param num 정수
     * @return 로마 숫자
     */
    public static String intToRoman(int num) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};

        StringBuilder roman = new StringBuilder();

        for (int i = 0; i < values.length; i++) {
            while (num >= values[i]) {
                num -= values[i];
                roman.append(symbols[i]);
            }
        }

        return roman.toString();
    }

    public enum AbilityBufType {
        Attribute,
        Event,
        Else
    }

    public record AbilityBufInfo(AbilityBufType type, boolean pos, Component... about) {

        /** 한 줄(논리 줄)씩 담은 리스트. 첫 줄은 +/- 접두, 나머지는 └- 접두 */
        public List<Component> lines() {
            List<Component> out = new ArrayList<>();
            for (int i = 0; i < about.length; i++) {
                Component prefix = (i == 0)
                        ? Component.text(pos ? "+ " : "- ", pos ? NamedTextColor.GREEN : NamedTextColor.RED)
                        : Component.text("└- ", pos ? NamedTextColor.GREEN : NamedTextColor.RED);
                // 부모 스타일이 없는 textOfChildren이라 접두 색이 내용으로 번지지 않는다
                out.add(Component.textOfChildren(prefix, about[i]));
            }
            return out;
        }

        public Component build_inline() {
            return Component.join(JoinConfiguration.newlines(), lines());
        }
    }

    public static ItemStack buildBook(IAbility ability, String author,
                                      List<Component> header, List<AbilityBufInfo> bufs) {
        final int maxLines = 14; // 한 페이지 최대 줄 수 (잘리면 13으로)
        int headerLines = bookLines(header);

        // 1) 버프 하나 = 한 덩어리. 한 페이지에 안 들어가는 거대한 버프만 줄 단위로 쪼갠다
        List<List<Component>> units = new ArrayList<>();
        for (AbilityBufInfo info : bufs) {
            List<Component> block = info.lines();
            if (headerLines + bookLines(block) <= maxLines) {
                units.add(block);
            } else {
                for (Component line : block) units.add(List.of(line));
            }
        }

        // 2) 덩어리가 남은 줄에 안 들어가면 통째로 다음 페이지로
        List<Component> pages = new ArrayList<>();
        List<Component> cur = new ArrayList<>(header);
        int used = headerLines;
        boolean hasBody = false;

        for (List<Component> unit : units) {
            int n = bookLines(unit);
            if (hasBody && used + n > maxLines) {
                pages.add(Component.join(JoinConfiguration.newlines(), cur));
                cur = new ArrayList<>(header);
                used = headerLines;
                hasBody = false;
            }
            cur.addAll(unit);
            used += n;
            hasBody = true;
        }
        if (hasBody || pages.isEmpty()) {
            pages.add(Component.join(JoinConfiguration.newlines(), cur));
        }

        // 3) 책 생성
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        meta.customName(Component.text(ability.getAbilityName() + " " + intToRoman(ability.getLevel() + 1))
                .decorate(TextDecoration.BOLD)
                .decoration(TextDecoration.ITALIC, false));
        meta.setTitle(ability.getAbilityName() + " " + intToRoman(ability.getLevel() + 1));
        meta.setAuthor(author);
        meta.addPages(pages.toArray(new Component[0]));
        meta.getPersistentDataContainer().set(ab_key, PersistentDataType.STRING, ability.getID());
        book.setItemMeta(meta);
        return book;
    }

    private static int bookLines(List<Component> lines) {
        int sum = 0;
        for (Component c : lines) sum += bookLineCount(c);
        return sum;
    }

    /** 화면상 줄 수 추정 (자동 줄바꿈 포함). 폭 한도 112px (실제 114) */
    private static int bookLineCount(Component component) {
        final int maxWidth = 112;
        List<Integer> g = new ArrayList<>();
        bookFlatten(component, false, g);

        int lines = 1, width = 0, i = 0;
        while (i < g.size()) {
            char ch = (char) (g.get(i) & 0xFFFF);
            if (ch == '\n') { lines++; width = 0; i++; continue; }
            if (ch == ' ') {
                int w = bookGlyphWidth(g.get(i));
                if (width + w > maxWidth) { lines++; width = 0; } else width += w;
                i++;
                continue;
            }
            int j = i, ww = 0; // 공백/개행 전까지 한 단어
            while (j < g.size()) {
                char cj = (char) (g.get(j) & 0xFFFF);
                if (cj == ' ' || cj == '\n') break;
                ww += bookGlyphWidth(g.get(j));
                j++;
            }
            if (width + ww <= maxWidth) {
                width += ww; i = j;
            } else if (ww <= maxWidth) {
                lines++; width = ww; i = j;           // 단어 통째로 다음 줄
            } else {
                if (width > 0) { lines++; width = 0; }
                for (; i < j; i++) {                  // 한 줄보다 긴 단어는 글자 단위로 꺾음
                    int w = bookGlyphWidth(g.get(i));
                    if (width + w > maxWidth) { lines++; width = 0; }
                    width += w;
                }
            }
        }
        return lines;
    }

    /** 트리를 (글자 | 굵기<<16) 목록으로 펼친다. 굵기는 부모에서 상속 */
    private static void bookFlatten(Component c, boolean parentBold, List<Integer> out) {
        TextDecoration.State st = c.decoration(TextDecoration.BOLD);
        boolean bold = st == TextDecoration.State.NOT_SET ? parentBold : st == TextDecoration.State.TRUE;
        if (c instanceof TextComponent t) {
            for (char ch : t.content().toCharArray()) out.add(ch | (bold ? 0x10000 : 0));
        }
        for (Component child : c.children()) bookFlatten(child, bold, out);
    }

    /** 기본 폰트 글자 폭 근사값 (간격 1px 포함). 한글 등 비ASCII는 넉넉하게 9 */
    private static int bookGlyphWidth(int code) {
        char ch = (char) (code & 0xFFFF);
        int w = switch (ch) {
            case 'i', '!', '.', ',', ':', ';', '|' -> 2;
            case 'l', '\'' -> 3;
            case ' ', 'I', 't', '[', ']' -> 4;
            case 'f', 'k', '<', '>', '(', ')', '*', '"', '{', '}' -> 5;
            case '@', '~' -> 7;
            default -> ch < 128 ? 6 : 9;
        };
        return (code >>> 16) != 0 ? w + 1 : w;
    }

    public static NamedTextColor tiertocolor(AbilityTier t) {
        NamedTextColor c = switch (t) {
            case AbilityTier.dummy -> NamedTextColor.BLACK;
            case common -> NamedTextColor.GRAY;
            case rare -> NamedTextColor.GREEN;
            case epic -> NamedTextColor.LIGHT_PURPLE;
            case legendary -> NamedTextColor.GOLD;
            case mystic -> NamedTextColor.DARK_AQUA;
        };
        return c;
    }

    public static String tiertoname(AbilityTier t) {
        String c = switch (t) {
            case AbilityTier.dummy -> "더미";
            case common -> "일반";
            case rare -> "희귀";
            case epic -> "에픽";
            case legendary -> "전설";
            case mystic -> "신화";
        };
        return c;
    }
}
