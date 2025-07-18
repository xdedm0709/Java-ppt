package com.tedu.element;

import com.tedu.element.*;
import com.tedu.manager.*;

public class PauseOther extends Item {
    @Override
    public ElementObj createElement(String str) {
        return super.createElement(str, "item6_anim");
    }

    @Override
    public void grantAbilityTo(Play triggerPlayer) {
        Play otherPlayer = getOtherPlayer(triggerPlayer);
        if (otherPlayer != null) {
            otherPlayer.pauseForFrames(100); // 暂停100帧 (约1秒)
        }
        this.setLive(false);
    }

    private Play getOtherPlayer(Play triggerPlayer) {
        for(ElementObj p : ElementManager.getManager().getElementsByKey(GameElement.PLAY)) {
            if (p != triggerPlayer) return (Play) p;
        }
        return null;
    }
}
