package com.kyleyannelli.finalproject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.utils.Array;

public class EvilLaserGuy {
    private TextureRegion currentTexture, leftNeutral;
    private float pos[], timer;
    private Array<EvilBullet> bullets;

    public EvilLaserGuy(float x, float y) {
        pos = new float[]{x, y};
        initializeSprites();
    }

    public void update(float delta, GuyRevamp revamp) {
        //update delta timer
        timer += delta;
        if(revamp.pos().y > 1200) {
            if(timer > 2f) {

                timer = 0;
            }
        }
    }

    private void initializeSprites() {
        leftNeutral = new TextureRegion(new Texture("character/characterEvilLeft.png"));
        currentTexture = leftNeutral;
    }

    public float[] pos() { return pos; }

    public TextureRegion currentSprite() { return currentTexture; }

    public Array<EvilBullet> getBullets() { return bullets; }
}
