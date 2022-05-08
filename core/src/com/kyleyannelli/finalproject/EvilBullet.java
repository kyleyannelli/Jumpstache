package com.kyleyannelli.finalproject;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

public class EvilBullet {
    private float x, y;
    private float speed, acceleration, maxSpeed;
    private TextureRegion texture;

    public EvilBullet(float x, float y) {
        this.x = x;
        this.y = y;
        this.texture = new TextureRegion(new Texture("character/evilLaserBullet.png"));
    }

    public TextureRegion getSprite() { return texture; }
}
