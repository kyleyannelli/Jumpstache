package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Polygon;
import com.kyleyannelli.asteroids.PolygonObject;

public class EvilBullet extends PolygonObject {
    public final int MAX_SPEED = 2000, DECELERATION = 100;
    private float speed;
    private TextureRegion texture;

    public EvilBullet(Polygon hitbox, float startingRotation) {
        super(hitbox, startingRotation);
        this.texture = new TextureRegion(new Texture("character/evilLaserBullet.png"));
        this.speed = MAX_SPEED;
    }

    public TextureRegion getSprite() { return texture; }

    public float getSpeed() { return speed; }

    //delta and factor must be applied in variable which is passed through
    public void applySpeed(float speed) {
        speed += speed;
    }

    public void update(float delta) {
        hitbox.setPosition(hitbox.getX() + (float)Math.cos(Math.toRadians(startingRotation)) * (speed * delta),
                hitbox.getY() + (float)Math.sin(Math.toRadians(startingRotation)) * (speed * delta));
    }
}
