package com.tedu.element;

public class RangeUp extends Item {
    @Override
    public ElementObj createElement(String str) {
        return super.createElement(str, "item2_anim");
    }

    @Override
    public void grantAbilityTo(Play player) {
        player.increaseBubbleRange(1);
        this.setLive(false);
    }
}