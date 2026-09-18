package cat.lacycat.perKS.Manager;

import cat.lacycat.perKS.Ability.AbilityTier;
import cat.lacycat.perKS.Ability.BunnyLeg;
import cat.lacycat.perKS.Ability.IAbility;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.reflections.Reflections;

import java.lang.reflect.*;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class PerkManager implements Listener {
    private static final int[] percents = {45, 30, 22, 3, 1};

    private final Map<String, Class<? extends IAbility>> registeredAbilities = new HashMap<>();
    // [수정] 매번 리플렉션으로 인스턴스화해서 티어를 확인하지 않도록, 시작 시점에 티어별로 미리 그룹핑해둔다.
    private final Map<AbilityTier, List<Class<? extends IAbility>>> abilitiesByTier = new EnumMap<>(AbilityTier.class);

    private InventoryBackupManager ibm;

    public PerkManager(InventoryBackupManager ibm) {
        this.ibm = ibm;
        scanAndRegisterAbilities();
        buildTierIndex();
    }

    private Map<UUID, List<IAbility>> perk = new HashMap<>();
    public final Set<UUID> choosing = new HashSet<>();

    public void showPick(Player p) {
        if (choosing.contains(p.getUniqueId())) return;
        ibm.backupInventory(p);
        p.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, Integer.MAX_VALUE, 1, true));
        p.sendActionBar(Component.text("현명하게 선택하세요...").color(TextColor.color(128, 128, 128)).decorate(TextDecoration.BOLD));
        choosing.add(p.getUniqueId());

        // [수정] perk.get()이 null일 수 있으므로 computeIfAbsent로 항상 빈 리스트를 보장한다.
        List<IAbility> ownedAbilities = perk.computeIfAbsent(p.getUniqueId(), k -> new ArrayList<>());
        List<IAbility> selectedAbilities = pickThreeRandomAbilities(ownedAbilities);

        // [수정] 하드코딩된 if-else 대신 루프로 슬롯 배치 (가독성 + 실수 방지)
        int[] bookSlots = {3, 4, 5};
        int bookIndex = 0;
        for (int slot = 0; slot < 9; slot++) {
            boolean isBookSlot = false;
            for (int bs : bookSlots) {
                if (bs == slot) { isBookSlot = true; break; }
            }
            if (isBookSlot) {
                p.getInventory().setItem(slot, selectedAbilities.get(bookIndex).getBook());
                bookIndex++;
            } else {
                p.getInventory().setItem(slot, createBarrierItem());
            }
        }

        p.updateInventory(); // 패킷 동기화 새로고침
    }

    private void scanAndRegisterAbilities() {
        try {
            Reflections reflections = new Reflections("cat.lacycat.perKS.Ability");
            Set<Class<? extends IAbility>> classes = reflections.getSubTypesOf(IAbility.class);

            for (Class<? extends IAbility> clazz : classes) {
                // 인터페이스나 추상 클래스는 제외하고 순수 구현체만 처리
                if (clazz.isInterface() || java.lang.reflect.Modifier.isAbstract(clazz.getModifiers())) {
                    continue;
                }

                // 임시로 인스턴스를 하나 만들어서 능력치 이름(getAbilityName)을 알아내 저장합니다.
                IAbility tempInstance = clazz.getDeclaredConstructor().newInstance();
                registeredAbilities.put(tempInstance.getAbilityName(), clazz);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * [신규] 등록된 능력치들을 티어별로 한 번만 분류해서 캐싱한다.
     * pickThreeRandomAbilities 안에서 매 시도마다 전체 능력치를 리플렉션으로
     * 재인스턴스화하며 티어를 확인하던 낭비를 제거하기 위함.
     */
    private void buildTierIndex() {
        for (AbilityTier tier : AbilityTier.values()) {
            abilitiesByTier.put(tier, new ArrayList<>());
        }
        for (Class<? extends IAbility> clazz : registeredAbilities.values()) {
            try {
                IAbility temp = clazz.getDeclaredConstructor().newInstance();
                abilitiesByTier.get(temp.getTier()).add(clazz);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        Item item = event.getItemDrop();

        if (!choosing.contains(player.getUniqueId()) || item.getItemStack().getType() != Material.WRITTEN_BOOK) {
            return;
        }

        ItemMeta meta = item.getItemStack().getItemMeta();
        // [수정] customName()이 null인 경우(이름 없는 책) NPE 방지
        if (meta == null || meta.customName() == null) {
            return;
        }

        String s = PlainTextComponentSerializer.plainText().serialize(meta.customName());

        // [수정] perk.get()이 null일 수 있으므로 computeIfAbsent로 항상 빈 리스트를 보장
        List<IAbility> ownedAbilities = perk.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());

        if (_existing(player, s)) {
            IAbility ability = _get(player, s);
            // [수정] 만렙 체크 없이 레벨업 되던 부분 방어
            if (ability != null && ability.getLevel() < ability.getMaxLevel()) {
                ability.setLevel(ability.getLevel() + 1);
                ability.onUpdated();
            }
        } else {
            Class<? extends IAbility> abilityClass = registeredAbilities.get(s);
            if (abilityClass != null) {
                try {
                    IAbility newAbility = abilityClass.getDeclaredConstructor().newInstance();

                    newAbility.onActivated(player);

                    ownedAbilities.add(newAbility);
                    player.sendMessage(Component.text("[" + s + "] 능력을 획득하셨습니다"));

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        choosing.remove(player.getUniqueId());
        player.removePotionEffect(PotionEffectType.BLINDNESS);
        ibm.restoreInventory(player);
        item.remove();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (choosing.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            if (choosing.contains(player.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (choosing.remove(player.getUniqueId())) {
            player.removePotionEffect(PotionEffectType.BLINDNESS);
            ibm.restoreInventory(player);
        }
        List<IAbility> owned = perk.remove(player.getUniqueId());
        if (owned != null) {
            for (IAbility a : owned) {
                a.onDeactivated(player); // HandlerList.unregisterAll(this) 호출됨
            }
        }
    }

    private boolean _existing(Player p, String s) {
        List<IAbility> owned = perk.get(p.getUniqueId());
        if (owned == null) return false;
        for (IAbility a : owned) {
            if (a.getAbilityName().equals(s)) return true;
        }
        return false;
    }

    private IAbility _get(Player p, String s) {
        List<IAbility> owned = perk.get(p.getUniqueId());
        if (owned == null) return null;
        for (IAbility a : owned) {
            if (a.getAbilityName().equals(s)) return a;
        }
        return null;
    }

    /**
     * percents 확률 분배에 따라 중복 없이, 그리고 이미 만레벨인 능력치를 제외하고 3개를 뽑는 메서드
     *
     * @param playerAbilities 플레이어가 현재 보유 중인 능력치 리스트 (레벨 검사용, null이 아님을 보장)
     */
    private List<IAbility> pickThreeRandomAbilities(List<IAbility> playerAbilities) {
        List<IAbility> chosen = new ArrayList<>();
        List<String> chosenNames = new ArrayList<>(); // 중복 방지용 이름 체크

        // 안전장치: 등록된 총 능력치가 3개 미만이면 예외 방지를 위해 기본 반환 처리
        if (registeredAbilities.size() <= 3) {
            for (Class<? extends IAbility> clazz : registeredAbilities.values()) {
                try { chosen.add(clazz.getDeclaredConstructor().newInstance()); } catch (Exception ignored) {}
            }
            while (chosen.size() < 3) { chosen.add(new BunnyLeg()); }
            return chosen;
        }

        int maxAttempts = 500;
        int attempts = 0;

        while (chosen.size() < 3 && attempts < maxAttempts) {
            attempts++;
            AbilityTier pickedTier = rollTier();

            // [수정] 매번 리플렉션으로 전체를 순회하지 않고, 미리 캐싱된 티어별 목록을 사용
            List<Class<? extends IAbility>> tierPool = abilitiesByTier.get(pickedTier);
            if (tierPool == null || tierPool.isEmpty()) continue;

            Class<? extends IAbility> finalChoice = tierPool.get(ThreadLocalRandom.current().nextInt(tierPool.size()));
            try {
                IAbility abilityInstance = finalChoice.getDeclaredConstructor().newInstance();
                String currentName = abilityInstance.getAbilityName();

                if (chosenNames.contains(currentName)) continue;

                boolean isMaxLevel = false;
                for (IAbility existing : playerAbilities) {
                    if (existing.getAbilityName().equals(currentName)) {
                        if (existing.getLevel() >= existing.getMaxLevel()) {
                            isMaxLevel = true;
                        }
                        break;
                    }
                }

                if (!isMaxLevel) {
                    chosen.add(abilityInstance);
                    chosenNames.add(currentName);
                }

            } catch (Exception ignored) {}
        }

        // 만레벨 제외 조건 때문에 3개를 채우지 못했다면, 예외 방지를 위해 만레벨 검사를 풀고 빈자리 채우기
        if (chosen.size() < 3) {
            for (Class<? extends IAbility> clazz : registeredAbilities.values()) {
                if (chosen.size() >= 3) break;
                try {
                    IAbility temp = clazz.getDeclaredConstructor().newInstance();
                    if (!chosenNames.contains(temp.getAbilityName())) {
                        chosen.add(temp);
                        chosenNames.add(temp.getAbilityName());
                    }
                } catch (Exception ignored) {}
            }
        }

        return chosen;
    }

    private AbilityTier rollTier() {
        int chance = ThreadLocalRandom.current().nextInt(100); // 0 ~ 99
        int cumulative = 0;

        AbilityTier[] tiers = AbilityTier.values(); // uncommon, rare, epic, legendary, mystic 순서
        for (int i = 0; i < percents.length; i++) {
            cumulative += percents[i];
            if (chance < cumulative) {
                return tiers[i];
            }
        }
        return AbilityTier.uncommon; // 예외 예방 기본값
    }

    /**
     * 상호작용 통제용 방벽 아이템 생성 (이름 초기화)
     */
    private ItemStack createBarrierItem() {
        ItemStack barrier = new ItemStack(Material.BARRIER);
        ItemMeta meta = barrier.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.text(" "));
            barrier.setItemMeta(meta);
        }
        return barrier;
    }
}