package cat.lacycat.perKS.Manager;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.UUID;

public class InventoryBackupManager { // 귀찮아서 AI로 대체

    // 플레이어의 UUID를 키로 하여 인벤토리 배열을 저장하는 저장소
    private final HashMap<UUID, ItemStack[]> inventoryBackup = new HashMap<>();
    private final HashMap<UUID, ItemStack[]> armorBackup = new HashMap<>();

    // 1. 인벤토리 백업하기
    public void backupInventory(Player player) {
        UUID uuid = player.getUniqueId();

        // getContents()는 0~40번 슬롯(인벤토리+갑옷+오프핸드) 전체를 복사해 옵니다.
        // 클론(clone) 처리를 해두어야 원본 아이템이 변형되어도 백업본이 안전합니다.
        ItemStack[] contents = player.getInventory().getContents();
        ItemStack[] clonedContents = new ItemStack[contents.length];

        for (int i = 0; i < contents.length; i++) {
            if (contents[i] != null) {
                clonedContents[i] = contents[i].clone();
            }
        }

        inventoryBackup.put(uuid, clonedContents);

        // 백업 후 플레이어 인벤토리를 비우고 싶다면 추가
        player.getInventory().clear();
    }

    // 2. 인벤토리 복구하기 (덮어씌우기)
    public void restoreInventory(Player player) {
        UUID uuid = player.getUniqueId();

        if (inventoryBackup.containsKey(uuid)) {
            ItemStack[] savedContents = inventoryBackup.get(uuid);

            // 기존 인벤토리를 완전히 덮어씌웁니다.
            player.getInventory().setContents(savedContents);

            // 데이터 관리용 메모리 해제
            inventoryBackup.remove(uuid);
            player.updateInventory(); // 플레이어 화면 동기화 새로고침
        }
    }
}
