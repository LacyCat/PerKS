package cat.lacycat.perKS.Manager.Command;

import cat.lacycat.perKS.PerKS;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PickCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String @NotNull [] args) {

        // 1. 인자가 없을 때 (예: /pick)
        if (args.length == 0) {
            if (sender instanceof Player p) {
                // 명령어를 쓴 플레이어 본인에게 창을 띄움
                PerKS.getInstance().getPerkManager().showPick(p);
            } else {
                // 콘솔에서 /pick 만 치면 대상이 없으므로 경고
                sender.sendMessage("콘솔에서는 대상을 지정해야 합니다. 사용법: /" + label + " <플레이어/선택자>");
            }
            return true; // ⚠️ 중요: 여기서 반드시 종료하여 아래 args[0] 에러를 방지합니다.
        }

        // 2. 인자가 있을 때 (예: /pick @a, /pick PlayerName)
        String targetInput = args[0];

        try {
            List<Entity> targets = Bukkit.selectEntities(sender, targetInput);

            if (targets.isEmpty()) {
                sender.sendMessage("대상을 찾을 수 없습니다.");
                return true;
            }

            int count = 0;
            for (Entity entity : targets) {
                // 선택된 엔티티가 '플레이어'인지 확인
                if (entity instanceof Player targetPlayer) { // 패턴 매칭 적용으로 간결하게 변경
                    PerKS.getInstance().getPerkManager().showPick(targetPlayer);
                    count++;
                }
            }

            if (count == 0) {
                sender.sendMessage("선택된 대상 중 플레이어가 없습니다.");
            } else {
                sender.sendMessage("총 " + count + "명의 플레이어에게 창을 열었습니다.");
            }

        } catch (IllegalArgumentException e) {
            sender.sendMessage("올바르지 않은 선택자 문법입니다.");
        }

        return true;
    }
} // 괄호 개수 정상화
