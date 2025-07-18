package com.tedu.element;

import com.tedu.manager.ElementManager;
import com.tedu.manager.GameElement;
import com.tedu.manager.GameLoad;

public class Waterbomb extends BoxObj {
    public Waterbomb() {
        this.name = "box6"; // 对应原box6
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/waterbomb1.jpg",
                "bin/image/Characters/waterbomb2.jpg",
                "bin/image/Characters/waterbomb3.jpg",
                "bin/image/Characters/waterbomb4.jpg"
        };
    }

    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer == null) return;

        Play targetPlayer = getOtherPlayer(triggerPlayer);
        if (targetPlayer != null) {
            surroundPlayerWithBubbles(targetPlayer);
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "触发Remotecontrol，玩家" + targetPlayer.getPlayerNumber() + "被包围");
        }
    }

    // 生成包围泡泡
    private void surroundPlayerWithBubbles(Play target) {
        ElementManager em = ElementManager.getManager();
        int gridSize = 45;
        int x = target.getX();
        int y = target.getY();

        // 上下左右四个位置
        int[][] positions = {
                {x, y - gridSize - 60},
                {x, y + gridSize - 60},
                {x - gridSize, y - 60},
                {x + gridSize, y - 60}
        };

        for (int[] pos : positions) {
            if (isValidBubblePosition(pos[0], pos[1])) {
                Bubble bubble = (Bubble) GameLoad.getObj("bubble").createElement(
                        pos[0] + "," + pos[1] + ",1,2000,water_trap"
                );
                em.addElement(bubble, GameElement.BUBBLE);
            }
        }
    }

    // 检查泡泡位置合法性
    private boolean isValidBubblePosition(int x, int y) {
        if (x < 0 || x > 800 || y < 0 || y > 600) return false;

        ElementManager em = ElementManager.getManager();
        for (ElementObj obj : em.getElementsByKey(GameElement.MAPS)) {
            if (obj.getX() == x && obj.getY() == y && ("BRICK".equals(obj.getName()) || "IRON".equals(obj.getName()))) {
                return false;
            }
        }
        return true;
    }
}