package com.tedu.element;

public class ResistCard extends Item {
    @Override
    public ElementObj createElement(String str) {
        return super.createElement(str, "item8_anim");
    }

    @Override
    public void grantAbilityTo(Play player) {
        player.setHasResistCard(true);
        this.setLive(false);
    }
}