package cat.lacycat.perKS.Ability.Legendary;

import cat.lacycat.perKS.Ability.AbilityTier;
import cat.lacycat.perKS.Ability.IAbility;
import cat.lacycat.perKS.Manager.Util;
import cat.lacycat.perKS.PerKS;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class InfFirework implements IAbility, Listener {
    private UUID p;
    private ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
    public static int[] modi = {10,5,3,2,1};
    public static int[] modi_2 = {1,2,3,100,255};
    public static NamespacedKey inf_id = new NamespacedKey(PerKS.getInstance(), "inf_firework");
    private int level = 0;

    @Override
    public String getAbilityName() {
        return "무한 폭죽";
    }

    @Override
    public String getID() {
        return "inf_firework";
    }

    @Override
    public ItemStack getBook() {
        // level + 1이 배열 범위를 벗어나지 않도록 방어 코드 적용 권장 (여기서는 유지)
        int displayLevel = Math.min(level + 1, modi.length - 1);
        return Util.buildBook(this, Util.tiertoname(getTier()),
                List.of(Component.text(getAbilityName(), Util.tiertocolor(getTier()), TextDecoration.BOLD),
                        Component.text("\"무한동력과 무한 폭죽\"").color(NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC, TextDecoration.BOLD),
                        Component.empty()
                ),
                List.of(new Util.AbilityBufInfo(Util.AbilityBufType.Event,true,
                        Component.text("체공시간 " + modi_2[displayLevel] + "의 폭죽을 " + modi[displayLevel] + "초 마다 지급받습니다."),
                        Component.text("단, 폭죽을 한번 사용해야 다시 들어옵니다."))
                )
        );
    }

    @Override
    public ItemStack getShow() {
        ItemStack item = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) item.getItemMeta();
        if (meta != null) {
            meta.customName(Component.text("무한 폭죽").decoration(TextDecoration.ITALIC,false).color(Util.tiertocolor(getTier())));
            meta.setPower(255);
            item.setItemMeta(meta);
        }
        return item;
    }

    @Override
    public AbilityTier getTier() {
        return AbilityTier.legendary;
    }

    @Override
    public void onActivated(Player p) {
        this.p = p.getUniqueId();
        Bukkit.getPluginManager().registerEvents(this, PerKS.getInstance());
        onUpdated();
        p.getInventory().addItem(item.clone()); // give -> 인벤토리 추가로 수정
    }

    @Override
    public void onUpdated() {
        FireworkMeta meta = (FireworkMeta) item.getItemMeta();
        if (meta != null) {
            meta.setPower(modi_2[level]);
            meta.getPersistentDataContainer().set(inf_id, PersistentDataType.STRING, p.toString());
            item.setItemMeta(meta);
        }
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public void setLevel(int level) {
        this.level = level;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @EventHandler
    public void onUse(PlayerInteractEvent ev) {
        Player player = ev.getPlayer();
        Action action = ev.getAction();

        // 1. 우클릭 검증
        if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack clickedItem = ev.getItem();

            // 2. null 및 아이템 타입 선행 검증 (NullPointerException 방지)
            if (clickedItem == null || clickedItem.getType() != Material.FIREWORK_ROCKET) return;
            if (clickedItem.getItemMeta() == null) return;

            // 3. PersistentData 검증
            if (!clickedItem.getItemMeta().getPersistentDataContainer().has(inf_id, PersistentDataType.STRING)) return;

            String ownerUUIDString = clickedItem.getItemMeta().getPersistentDataContainer().get(inf_id, PersistentDataType.STRING);

            // 4. 타인이 무한 폭죽을 쓰려고 하면 취소
            if (!Objects.equals(ownerUUIDString, player.getUniqueId().toString())) {
                ev.setCancelled(true);
                return;
            }

            // 5. 능력 소유자 본인이 아니라면 아래 로직 차단 (정상 소유자만 통과)
            if (!player.getUniqueId().equals(p)) return;

            // 6. 지연 후 폭죽 다시 지급 루틴 실행
            long delayTicks = 20L * modi[level];
            Bukkit.getScheduler().runTaskLater(PerKS.getInstance(), () -> {
                // 온라인 상태인지 검증 후 안전하게 지급
                if (player.isOnline()) {
                    player.getInventory().addItem(item.clone());
                }
            }, delayTicks);
        }
    }
}
