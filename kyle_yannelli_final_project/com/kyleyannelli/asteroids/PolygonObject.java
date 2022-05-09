package com.kyleyannelli.asteroids;

import com.badlogic.gdx.math.Polygon;

public abstract class PolygonObject {
    public float startingRotation;
    public long lifespan;
    public Polygon hitbox;

    public PolygonObject(Polygon hitbox, float startingRotation) {
        this.hitbox = hitbox;
        this.startingRotation = hitbox.getRotation();
        this.lifespan = System.currentTimeMillis();
    }
}
