package com.tedu.element;

import com.tedu.element.*;
import com.tedu.manager.*;
import com.tedu.show.GameJFrame;
import java.awt.Rectangle;
import java.util.List;

public class Surround extends Item {
    @Override
    public ElementObj createElement(String str) {
        return super.createElement(str, "item1_anim");
    }

    @Override
    public void grantAbilityTo(Play triggerPlayer) {
        ElementManager em = ElementManager.getManager();
        Play otherPlayer = getOtherPlayer(triggerPlayer);

        if (otherPlayer != null) {
            System.out.println("玩家 " + triggerPlayer.getPlayerID() + " 触发陷阱，包围了 " + otherPlayer.getPlayerID());
            surroundPlayerWithBubbles(otherPlayer);
        }
        this.setLive(false);
    }

    private void surroundPlayerWithBubbles(Play targetPlayer) {
        final int TILE_SIZE = GameJFrame.TILE_SIZE;
        int gridX = (targetPlayer.getX() + targetPlayer.getW() / 2) / TILE_SIZE;
        int gridY = (targetPlayer.getY() + targetPlayer.getH() / 2) / TILE_SIZE;

        int[][] positions = {
                {gridX, gridY - 1}, {gridX, gridY + 1},
                {gridX - 1, gridY}, {gridX + 1, gridY}
        };

        for (int[] pos : positions) {
            int bubbleX = pos[0] * TILE_SIZE;
            int bubbleY = pos[1] * TILE_SIZE;
            // 创建一个临时的、不能动的泡泡（陷阱泡泡）
            // 注意：这需要 Bubble 类能接收更复杂的创建字符串
            String bubbleStr = bubbleX + "," + bubbleY + ",1," + "trap_owner";
            ElementObj bubble = GameLoad.getObj("bubble").createElement(bubbleStr);
            if (bubble != null) {
                ElementManager.getManager().addElement(bubble, GameElement.BUBBLE);
            }
        }
    }

    private Play getOtherPlayer(Play triggerPlayer) {
        for(ElementObj p : ElementManager.getManager().getElementsByKey(GameElement.PLAY)) {
            if (p != triggerPlayer) return (Play) p;
        }
        return null;
    }
}
