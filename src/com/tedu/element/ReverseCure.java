package com.tedu.element;

import com.tedu.element.*;
import com.tedu.manager.GameLoad;

public class ReverseCure extends Item {
    @Override
    public ElementObj createElement(String str) {
        return super.createElement(str, "item3_anim");
    }

    @Override
    public void grantAbilityTo(Play player) {
        player.setReverseWalking(false); // 直接调用解除方法
        this.setLive(false);
    }
}
