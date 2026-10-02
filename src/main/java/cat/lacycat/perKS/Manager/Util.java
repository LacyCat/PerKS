package cat.lacycat.perKS.Manager;

import cat.lacycat.perKS.PerKS;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;

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


}
