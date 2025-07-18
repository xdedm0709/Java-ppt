package com.tedu.element;

import com.tedu.element.*;
import com.tedu.manager.GameLoad;

public class Reverse extends Item {
    @Override
    public ElementObj createElement(String str) {
        return super.createElement(str, "item4_anim");
    }

    @Override
    public void grantAbilityTo(Play player) {
        player.setReverseWalking(true);
        this.setLive(false);
    }
}
