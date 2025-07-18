package com.tedu.element;

import com.tedu.element.*;
import com.tedu.manager.GameLoad;

public class AddBubble extends Item {
    @Override
    public ElementObj createElement(String str) {
        return super.createElement(str, "item7_anim");
    }

    @Override
    public void grantAbilityTo(Play player) {
        player.increaseMaxBubbles(1);
        this.setLive(false);
    }
}