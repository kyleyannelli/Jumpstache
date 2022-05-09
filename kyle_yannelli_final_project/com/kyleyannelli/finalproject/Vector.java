package com.kyleyannelli.finalproject;
import com.badlogic.gdx.math.Vector2;

public class Vector {
    public static float distance(Vector2 a, Vector2 b) {
        return (float) Math.sqrt( ((b.x - a.x) * (b.x - a.x)) + ((b.y - a.y) * (b.y - a.y)) );
    }

    public static float angle(Vector2 a, Vector2 b) {
        double dotProduct = a.x * b.x + a.y * b.y;
        double crossProduct = a.x * b.y - a.y * b.x;
        return (float) (Math.atan2(Math.abs(crossProduct), dotProduct) * 180.0 / Math.PI);
    }

    public static Vector2 add2d(Vector2 a, Vector2 b) {
        return new Vector2(a.x + b.x, a.y + b.y);
    }

    public static Vector2 sub2d(Vector2 a, Vector2 b) {
        return new Vector2(a.x - b.x, a.y - b.y);
    }

    public static Vector2 multiply2d(Vector2 a, float scalar) {
        return new Vector2(a.x * scalar,a.y * scalar);
    }

    public static Vector2 normalize2d(Vector2 a) {
        float length = (float) Math.sqrt((a.x * a.x) + (a.y * a.y));
        return new Vector2(a.x/length, a.y/length);
    }
}
