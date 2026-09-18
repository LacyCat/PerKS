package cat.lacycat.perKS;

import cat.lacycat.perKS.Manager.Command.PickCommand;
import cat.lacycat.perKS.Manager.InventoryBackupManager;
import cat.lacycat.perKS.Manager.PerkManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class PerKS extends JavaPlugin {

    private static PerKS instance;

    private InventoryBackupManager inventoryBackupManager;
    private PerkManager perkManager;

    @Override
    public void onEnable() {
        instance = this;

        // 의존성 순서 주의: PerkManager 생성자가 ibm을 필요로 함
        this.inventoryBackupManager = new InventoryBackupManager();
        this.perkManager = new PerkManager(inventoryBackupManager);

        // PerkManager 자신도 Listener이므로 반드시 등록해야 onPlayerDropItem 등이 동작함
        Bukkit.getPluginManager().registerEvents(perkManager, this);

        // 참고: 개별 능력(IAbility) 인스턴스는 onActivated() 시점에
        // 각자 registerEvents(this, PerKSPlugin.getInstance())를 호출하므로
        // 여기서 따로 등록할 필요는 없음
        getCommand("pick").setExecutor(new PickCommand());
    }

    @Override
    public void onDisable() {
        // 안전하게 모든 리스너 해제 (능력별로 등록된 것들 포함)
        org.bukkit.event.HandlerList.unregisterAll(this);
    }

    public static PerKS getInstance() {
        return instance;
    }

    public PerkManager getPerkManager() {
        return perkManager;
    }
}