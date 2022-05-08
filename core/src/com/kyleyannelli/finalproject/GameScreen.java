package com.kyleyannelli.finalproject;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

public class GameScreen implements Screen {
    final FinalProject game;
    final int RENDER_WIDTH = 1280;
    final int RENDER_HEIGHT = 400;
    private int cameraOffset = 0;
    private TiledMap tiledMap;
    private MapLayer collisionLayerOne;
    private MapObjects collisionObjects;
    private Animation<TextureRegion> bg, bgFlipped;
    private OrthogonalTiledMapRenderer tiledMapRenderer;
    private BitmapFont font;
    private float timing;
    OrthographicCamera camera;
    private GuyRevamp revamp;
    private EvilLaserGuy evilOne;
    private Music trackOne;


    public GameScreen(final FinalProject game) {
        this.game = game;
        //initialize camera
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, RENDER_WIDTH, RENDER_HEIGHT);
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap2.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        // get rects from tiledMap
        this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
        this.collisionObjects = collisionLayerOne.getObjects();
        this.font = new BitmapFont();
        this.revamp = new GuyRevamp(RENDER_WIDTH/2, 250);
        this.evilOne = new EvilLaserGuy(32, 1720);
        this.trackOne = Gdx.audio.newMusic(Gdx.files.internal("music/trackOneSlowed.ogg"));
        trackOne.setVolume(1.0f);
        trackOne.setLooping(true);
        trackOne.play();
        loadBgFrames();
    }

    private void loadBgFrames() {
        TextureRegion[] frames = new TextureRegion[32];
        TextureRegion[] framesFlipped = new TextureRegion[32];
        for(int i = 0; i < 32; i++) {
            frames[i] = new TextureRegion(new Texture("bganim/frame_" + i + ".png"));
        }
        for(int i = 0; i < 32; i++) {
            framesFlipped[i] = new TextureRegion(new Texture("bganim/flipped/frame_" + i + ".png"));
        }
        this.bg = new Animation<TextureRegion>(.1f, frames);
        this.bgFlipped = new Animation<>(.1f, framesFlipped);
    }
    @Override
    public void show() {

    }

    @Override
    public void render(float delta) {
        timing += delta;
        if(Gdx.input.isKeyJustPressed(Input.Keys.R)) reloadMap();
        if(Gdx.input.isKeyJustPressed(Input.Keys.SHIFT_LEFT)) {
            if(Gdx.input.isKeyPressed(Input.Keys.R)) {
                reset();
            }
        }
        ScreenUtils.clear(0, 0, 0, 1);
        adjustObjects();
        game.batch.begin();
        if(revamp.pos().y > 1200) {
            game.batch.draw(bgFlipped.getKeyFrame(timing, true), 0, 750);
            game.batch.draw(bg.getKeyFrame(timing, true), 0, 750*2);
            game.batch.draw(bgFlipped.getKeyFrame(timing, true), 0, 750*3);
        }
        else {
            game.batch.draw(bg.getKeyFrame(timing, true), 0, 0);
            game.batch.draw(bgFlipped.getKeyFrame(timing, true), 0, 750);
        }
        game.batch.end();
        camera.update();
        game.batch.setProjectionMatrix(camera.combined);
        tiledMapRenderer.setView(camera);
        tiledMapRenderer.render();
        game.batch.begin();
        game.batch.draw(revamp.currentSprite(), revamp.pos().x, revamp.pos().y);
        game.batch.draw(evilOne.currentSprite(), evilOne.pos()[0], evilOne.pos()[1]);
        game.batch.end();
        revamp.update(delta, collisionObjects);
//        evilOne.update(delta, revamp);
//        game.font.draw(game.batch, ""+(int)revamp.getPeak(), revamp.pos().x, revamp.pos().y + revamp.getRect().height);
        cameraFollowGuy(delta);
        debug();
    }

    private void debug() {
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) revamp.setPos(new Vector2(RENDER_WIDTH/2, 200));
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) revamp.setPos(new Vector2(RENDER_WIDTH/2 - 100, 1300));
        if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) revamp.setPos(new Vector2(RENDER_WIDTH/2 - 100, 2000));
    }

    private void reset() {
        //initialize camera
        this.camera = new OrthographicCamera();
        this.camera.setToOrtho(false, RENDER_WIDTH, RENDER_HEIGHT);
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap2.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
        // get rects from tiledMap
        this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
        this.collisionObjects = collisionLayerOne.getObjects();
        this.font = new BitmapFont();
        this.revamp = new GuyRevamp(RENDER_WIDTH/2, 250);
        this.evilOne = new EvilLaserGuy(32, 1720);
        this.trackOne = Gdx.audio.newMusic(Gdx.files.internal("music/trackOneSlowed.ogg"));
        trackOne.setVolume(1.0f);
        trackOne.setLooping(true);
        trackOne.play();
        loadBgFrames();
    }

    private void reloadMap() {
        TmxMapLoader loader = new TmxMapLoader();
        this.tiledMap = loader.load("betamap2.tmx");
        // for rendering the tiledMap
        this.tiledMapRenderer = new OrthogonalTiledMapRenderer(tiledMap);
    }

    // handle collision object change at different levels. minimizes for loop on bigger maps
    public void adjustObjects() {
        if(revamp.getRect().y >= 1300) {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor2");
            this.collisionObjects = collisionLayerOne.getObjects();
        }
        else {
            this.collisionLayerOne = (MapLayer) tiledMap.getLayers().get("floor");
            this.collisionObjects = collisionLayerOne.getObjects();
        }
    }

    public void cameraFollowGuy(float delta) {
        if(revamp.getRect().y > 1800) {
            camera.position.y += (revamp.getRect().y - camera.position.y) * 0.2f * delta;
        }
        else if(cameraFollow(1270, 1800,75)){ }
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
        trackOne.dispose();
    }
}
