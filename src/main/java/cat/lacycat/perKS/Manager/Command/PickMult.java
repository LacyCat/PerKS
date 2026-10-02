package cat.lacycat.perKS.Manager.Command;

import cat.lacycat.perKS.Manager.PerkManager;
import cat.lacycat.perKS.PerKS;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PickMult implements CommandExecutor {
    // 아직 안 띄운 남은 횟수
    private final Map<UUID, Integer> remaining = new HashMap<>();
    private BukkitTask task;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String @NotNull [] args) {
        if (args.length == 0) {
            sender.sendMessage("사용법: /" + label + " <대상> [횟수]");
            return true;
        }

        int times = 1;
        if (args.length >= 2) {
            try {
                times = Integer.parseInt(args[1]);
            } catch (NumberFormatException e) {
                sender.sendMessage("횟수는 정수여야 합니다.");
                return true;
            }
            if (times < 1 || times > 100) {
                sender.sendMessage("횟수는 1 ~ 100 사이여야 합니다.");
                return true;
            }
        }

        try {
            int count = 0;
            for (Entity entity : Bukkit.selectEntities(sender, args[0])) {
                if (entity instanceof Player target) {
                    remaining.merge(target.getUniqueId(), times, Integer::sum);
                    count++;
                }
            }

            if (count == 0) {
                sender.sendMessage("선택된 대상 중 플레이어가 없습니다.");
            } else {
                sender.sendMessage("총 " + count + "명의 플레이어에게 " + times + "회 선택 창을 열었습니다.");
                startTask();
            }
        } catch (IllegalArgumentException e) {
            sender.sendMessage("올바르지 않은 선택자 문법입니다.");
        }
        return true;
    }

    private void startTask() {
        if (task != null) return; // 이미 돌고 있음

        task = Bukkit.getScheduler().runTaskTimer(PerKS.getInstance(), () -> {
            PerkManager pm = PerKS.getInstance().getPerkManager();

            Iterator<Map.Entry<UUID, Integer>> it = remaining.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, Integer> e = it.next();
                Player p = Bukkit.getPlayer(e.getKey());

                // 나간 플레이어는 대기열에서 제거
                if (p == null || !p.isOnline()) {
                    it.remove();
                    continue;
                }
                // 선택 중이면 다음 틱에 다시 확인
                if (pm.choosing.contains(p.getUniqueId())) continue;

                pm.showPick(p);
                int left = e.getValue() - 1;
                if (left <= 0) {
                    it.remove();
                    p.sendMessage(Component.text("마지막 선택입니다.", NamedTextColor.YELLOW));
                } else {
                    e.setValue(left);
                    p.sendMessage(Component.text("남은 선택 횟수: " + left + "회", NamedTextColor.YELLOW));
                }
            }

            // 할 일이 없으면 태스크 종료
            if (remaining.isEmpty()) {
                task.cancel();
                task = null;
            }
        }, 0L, 2L);
    }
}