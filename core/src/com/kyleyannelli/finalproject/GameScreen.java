package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.ScreenUtils;

import java.security.Key;
import java.util.HashMap;

public class GameScreen implements Screen {
    final FinalProject game;
    final int RENDER_WIDTH = 1280;
    final int RENDER_HEIGHT = 400;
    private int cameraOffset = 0;
    private TiledMap tiledMap;
    private MapLayer collisionLayerOne;
    private MapObjects collisionObjects;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private BitmapFont font;
    private float deltaTimer, deltaTimerEvil = -2;
    private int lastEvilInput;
    private HashMap<Integer, Integer> followGuy;
    OrthographicCamera camera;
    private GuyRevamp revamp;


    public GameScreen(final FinalProject game) {
        this.game = game;
        //initialize camera
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, RENDER_WIDTH, RENDER_HEIGHT);
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        // get rects from tiledMap
        this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
        this.collisionObjects = collisionLayerOne.getObjects();
        this.font = new BitmapFont();
        this.followGuy = new HashMap<>();
        this.revamp = new GuyRevamp(RENDER_WIDTH/2, 200);
    }

    @Override
    public void show() {
    }

    @Override
    public void render(float delta) {
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) reloadMap();
        ScreenUtils.clear(0, 0, 0, 1);
        adjustObjects();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        game.batch.begin();
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        revamp.update(delta, collisionObjects);
        game.batch.draw(revamp.currentSprite(), revamp.pos().x, revamp.pos().y);
        game.batch.end();
        cameraFollowGuy();
        debug();
    }

    private void debug() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) revamp.setPos(new Vector2(RENDER_WIDTH/2, 200));
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) revamp.setPos(new Vector2(RENDER_WIDTH/2 - 100, 1300));
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) revamp.setPos(new Vector2(RENDER_WIDTH/2 - 100, 2000));
    }

    private void reloadMap() {
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    // handle collision object change at different levels. minimizes for loop on bigger maps
    public void adjustObjects() {
        if(revamp.getRect().y >= 1300) {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor2");
            this.collisionObjects = collisionLayerOne.getObjects();
        } else {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
            this.collisionObjects = collisionLayerOne.getObjects();
        }
    }

    public void cameraFollowGuy() {
        if(cameraFollow(2200, 99999, 95)) { }
        else if(cameraFollow(1270, 2200,75)){ }
        else if(cameraFollow(450, 1270,50)) { }
        else if(revamp.getRect().y < 450 && camera.position.y >= 200){
            cameraOffset = 0;
            camera.position.y -= 50;
        }
    }

    private boolean cameraFollow(int posInitial, int posMax, int offset) {
        boolean change = false;
        if(revamp.getRect().y < posMax && revamp.pos().y >= posInitial && cameraOffset >= offset + 2) {
            change = true;
            cameraOffset -= 2;
            camera.position.y -= cameraOffset;
        }
        else if(revamp.getRect().y >= posInitial && cameraOffset < offset) {
            cameraOffset += 2;
            camera.position.y += cameraOffset;
            change = true;
        }
        return change;
    }

    @Override
    public void resize(int width, int height) {
        camera.viewportWidth = width;
        camera.viewportHeight = height;
        game.batch.setProjectionMatrix(camera.combined);
        camera.update();
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
//        guy.dispose();
    }
}
