package com.tedu.element;

public class Diamondcard extends BoxObj {
    public Diamondcard() {
        this.name = "box7"; // 对应原box7
    }

    @Override
    protected String[] getAnimationPaths() {
        return new String[]{
                "bin/image/Characters/diamondcard1.jpg",
                "bin/image/Characters/diamondcard2.jpg",
                "bin/image/Characters/diamondcard3.jpg",
                "bin/image/Characters/diamondcard4.jpg"
        };
    }

    @Override
    protected void grantAbility() {
        Play triggerPlayer = getPlayerInRange();
        if (triggerPlayer != null) {
            triggerPlayer.setResistWaterTrap(true); // 赋予抵抗能力
            isAbilityGranted = true;
            System.out.println("玩家" + triggerPlayer.getPlayerNumber() + "获得泡泡抵抗能力（Waterbomb）");
        }
    }
}