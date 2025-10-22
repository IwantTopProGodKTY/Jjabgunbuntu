package io.jbnu.ac.kr;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.security.Key;
import java.util.Random;



/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {

    private SpriteBatch batch;
    Sound effectSound;

    GameWorld world;
    public int Level = 1;
    private Texture objectTexture; // 떨어지는 오브젝트 텍스처
    private Texture playerTexture;
    private Texture pauseTexture;
    private Texture blockTexture; // 블록 텍스쳐
    private BitmapFont scoreFont;

    //카메라
    private OrthographicCamera camera;
    private Viewport viewport;
    public CameraManager camManager;


    private final float WORLD_WIDTH = 1280;
    private final float WORLD_HEIGHT = 720;

    private enum GameState{
        RUNNING,
        PAUSE,
        CLEARED
    }
    GameState currentState;


    @Override
    public void create() {
        batch = new SpriteBatch();
        effectSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        playerTexture = new Texture("t.png");
        objectTexture = new Texture("coin.jpg");
        pauseTexture = new Texture("pause.png");
        blockTexture = new Texture("block.png");

        //월드 생성
        world = new GameWorld(playerTexture,objectTexture,blockTexture,this.WORLD_WIDTH,Level);
        //카메라 흔들림 제어 클래스 생성
        camManager = new CameraManager();

        scoreFont = new BitmapFont(); // 기본 비트맵 폰트 생성
        scoreFont.getData().setScale(2);



        currentState = GameState.RUNNING;

        camera = new OrthographicCamera();
        viewport = new FitViewport(800,600,camera);
        camera.setToOrtho(false,800,600);



    }

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        input();
        logic();
        draw();
        endingCheck();
    }

    private void logic(){

        // PAUSE
        if(currentState == GameState.RUNNING)
        {
            world.update(Gdx.graphics.getDeltaTime());
        }
        if(currentState == GameState.CLEARED)
        {
            updateEffect(Gdx.graphics.getDeltaTime());
        }

        // LEVEL 에 따른 스테이지 변화
        if(world.getScore() == 10) {
            Level *= 2;
            NewWorld(Level);
        }




    }

    private void draw(){
        batch.begin();


        camera.position.set(world.getPlayer().position.x, camera.position.y, 0);
        camera.update();


        batch.setProjectionMatrix(viewport.getCamera().combined);



        world.getPlayer().draw(batch);
        for (CoinObject obj : world.getObjects()) {
            obj.draw(batch);
        }

        // 깃발 및 블록 그리기
        for (Block block : world.getBlock())
        {
            block.draw(batch);
        }
        world.getFlag().draw(batch);

        scoreFont.draw(batch, "Score: " + world.getScore(), 20, WORLD_HEIGHT - 20);


        //Pause 이미지 그리기
        if(currentState ==  GameState.PAUSE)
        {
            batch.draw(pauseTexture,640-(pauseTexture.getWidth()/2),360-(pauseTexture.getHeight()/2));
        }

        batch.end();

    }
    private void input() {

        if (Gdx.input.isKeyPressed(Keys.RIGHT) && currentState == GameState.RUNNING){
            world.onPlayerRight();
            world.moveAmount = 7;
        } else if (Gdx.input.isKeyPressed(Keys.LEFT) && currentState == GameState.RUNNING) {
            world.onPlayerLeft();
            world.moveAmount = -7;
        }
        if (Gdx.input.isKeyPressed(Keys.SPACE) && currentState == GameState.RUNNING){
            world.onPlayerJump();
        }
        if(Gdx.input.isKeyPressed(Keys.ENTER))
        {
            currentState = GameState.PAUSE;
        }
        if(Gdx.input.isKeyPressed(Keys.ESCAPE))
        {
            currentState = GameState.RUNNING;
        }

    }

    public void NewWorld(int level)
    {
        // 다시 새로운 객체를 생성 레벨에 따라 레벨을 코인의 속도에 곱합
        world = new GameWorld(playerTexture,objectTexture,blockTexture,this.WORLD_WIDTH,level);

    }
    @Override
    public void dispose() {
        playerTexture.dispose();
        objectTexture.dispose();
        scoreFont.dispose();
        batch.dispose();
    }


    //기존 카메라함수 오버라이드
    @Override
    public void resize(int width, int height)
    {
        viewport.update(width,height,true);

    }





    // 카메라 매니저
    public class CameraManager {
        private float shakeDuration = 0f;
        private float shakeTimer = 0f;
        private float shakeIntensity = 0f;
        private Vector2 originalPosition = new Vector2();
        public boolean isShakedStarted;

        public void startShake(float Duration, float Intensity){
            if(shakeDuration > 0)
                return; // 이미 흔들린다면 무시
            this.shakeDuration = Duration;
            this.shakeIntensity = Intensity;
            this.shakeTimer = 0f;

            originalPosition.set(camera.position.x,camera.position.y);
            isShakedStarted = true;

        }

        public void updateShake(float delta)
        {
            if(shakeTimer < shakeDuration)
            {
                shakeTimer+=delta;

                float decayFactor = shakeTimer/shakeDuration;
                float currentIntensity = shakeIntensity * (1f-decayFactor);

                float offsetX = MathUtils.random(-1f,1f)*currentIntensity;
                float offsetY = MathUtils.random(-1f,1f)*currentIntensity;

                camera.position.x = originalPosition.x + offsetX;
                camera.position.y = originalPosition.y + offsetY;
            }
            else if(shakeDuration > 0)
            {
                camera.position.set(originalPosition,0);
                shakeDuration = 0f;
                shakeTimer = 0f;
                isShakedStarted = false;
            }

        }


    }

    private void endingCheck()
    {
        if(world.isGameClear() && (currentState != GameState.CLEARED))
        {
            currentState = GameState.CLEARED;
            startEffect();
        }
    }

    public void startEffect(){
        camManager.startShake(3.0f,80f); //3초간 강하게 흔든다
        camManager.isShakedStarted = true;
    }

    public void updateEffect(float delta)
    {
        camManager.updateShake(delta);
    }
}
