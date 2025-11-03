package io.jbnu.ac.kr;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.audio.Sound;
import java.util.Iterator;

public class GameWorld {
    public final float WORLD_GRAVITY = -9.8f * 200;
    public final float DEATH_LINE = 0;
    public final float GROUND_Y = 50;        // 플랫폼 Y 위치
    public final float GROUND_HEIGHT = 50;   // 플랫폼 높이 *** 중요! ***

    private Sound hitSound;
    private Sound clearSound;

    private GameCharacter player;
    private Array<Block> blocks;
    private Array<Rectangle> platforms;
    private Array<CoinObject> objects;
    private Flag flag;

    private int score;
    public int Level;
    public boolean isGameClear = false;
    public boolean isGameOver = false;

    private Texture playerTexture;
    private Texture objectTexture;
    private Texture blockTexture;
    private Texture platformTexture;
    private float worldWidth;
    private int totalScore = 0;  // *** 누적 점수 ***

    public GameWorld(Texture playerTexture, Texture objectTexture, Texture blockTexture,
                     Texture platformTexture,Sound hitSound, Sound clearSound,  float worldWidth, int level) {
        this.playerTexture = playerTexture;
        this.objectTexture = objectTexture;
        this.blockTexture = blockTexture;
        this.platformTexture = platformTexture;
        this.worldWidth = worldWidth;
        this.hitSound = hitSound;
        this.clearSound = clearSound;

        player = new GameCharacter(playerTexture, 0, GROUND_Y + GROUND_HEIGHT);
        objects = new Array<>();
        blocks = new Array<>();
        platforms = new Array<>();

        score = 0;
        Level = level;
        loadGround(level);
    }



    public void update(float delta) {
        if(isGameOver || isGameClear) return;

        float moveAmount = 7;

        // 중력 적용
        player.velocity.y += WORLD_GRAVITY * delta;

        // Y축 이동
        float newY = player.position.y + player.velocity.y * delta;
        checkBlockCollisionY(newY);

        // 플랫폼(땅) 착지 (Y축 이동 후)
        checkPlatformCollision();

        // X축 이동
        float newX = player.position.x + moveAmount + player.velocity.x * delta;
        player.position.x = newX;
        player.sprite.setX(player.position.x);

        // X축 속도 감속
        if(Math.abs(player.velocity.x) > 0) {
            player.velocity.x *= 0.95f;
        }

        // X축 블록 충돌
        checkBlockCollisionX();

        // 코인 충돌
        checkCoinCollision();

        // 깃발 충돌
        checkFlagCollision();

        // Y <= 0이면 낭떠러지 추락
        if(player.position.y <= DEATH_LINE) {
            isGameOver = true;
            System.out.println("Fell into pit! GAME OVER");
        }

        // 체력 <= 0
        if(player.Hp <= 0) {
            isGameOver = true;
            System.out.println("HP 0! GAME OVER");
        }

        player.syncSpriteToPosition();
    }

    // Y축 블록 충돌
    private void checkBlockCollisionY(float newY) {
        Rectangle futurePlayerBounds = new Rectangle(
            player.position.x, newY,
            player.sprite.getWidth(), player.sprite.getHeight()
        );

        for(Block block : blocks) {
            if(futurePlayerBounds.overlaps(block.getBounds())) {
                player.Hp--;
                player.position.x = block.getBounds().x - player.sprite.getWidth() - 5;
                player.velocity.x = -1500f;
                player.velocity.y = 600f;

                // *** 충돌음 재생 ***
                hitSound.play(0.5f);

                System.out.println("Hit block! HP: " + player.Hp);
                return;
            }
        }
        player.position.y = newY;
    }

    // checkBlockCollisionX() 메서드 수정
    private void checkBlockCollisionX() {
        Rectangle playerBounds = player.sprite.getBoundingRectangle();

        for(Block block : blocks) {
            if(playerBounds.overlaps(block.getBounds())) {
                player.Hp--;
                player.position.x = block.getBounds().x - player.sprite.getWidth() - 5;
                player.velocity.x = -1500f;
                player.velocity.y = 600f;

                // *** 충돌음 재생 ***
                hitSound.play(0.5f);

                System.out.println("Hit Block! HP: " + player.Hp);
                break;
            }
        }
    }

    // *** 플랫폼 착지 ***
    private void checkPlatformCollision() {
        Rectangle playerBounds = new Rectangle(
            player.position.x, player.position.y,
            player.sprite.getWidth(), player.sprite.getHeight()
        );

        boolean onPlatform = false;

        // 플랫폼과의 충돌 검사
        for(Rectangle platform : platforms) {
            // X축 겹침 확인
            if(playerBounds.x + playerBounds.width > platform.x &&
                playerBounds.x < platform.x + platform.width) {

                // Y축에서 플랫폼 위에 있는지 확인
                if(playerBounds.y >= platform.y + platform.height - 5 &&
                    playerBounds.y <= platform.y + platform.height + 10 &&
                    player.velocity.y <= 0) {  // 위에서 내려오는 중

                    player.position.y = platform.y + platform.height;
                    player.velocity.y = 0;
                    player.isGrounded = true;
                    onPlatform = true;
                    return;
                }
            }
        }

        // 플랫폼이 없으면 추락 중
        if(!onPlatform) {
            player.isGrounded = false;
        }
    }

    // 코인 충돌
    private void checkCoinCollision() {
        Rectangle playerBounds = player.sprite.getBoundingRectangle();

        Iterator<CoinObject> iter = objects.iterator();
        while(iter.hasNext()) {
            CoinObject coin = iter.next();
            if(playerBounds.overlaps(coin.bounds)) {
                score += 10;
                System.out.println("Coin! Score: " + score);
                iter.remove();
            }
        }
    }

    // 깃발 충돌
    private void checkFlagCollision() {
        if(player.sprite.getBoundingRectangle().overlaps(flag.bounds)) {
            isGameClear = true;
            score += 100;

            if(Level == 3)
                clearSound.play(1.0f);

            System.out.println("Stage " + Level + " Clear! Score: " + score);
        }
    }

    // *** 레벨별 맵 생성 ***
    private void loadGround(int level) {
        blocks.clear();
        platforms.clear();
        objects.clear();

        // 플랫폼 크기: 100
        final float PLATFORM_WIDTH = 100;

        switch(level) {
            case 1:
                // 1스테이지: 전체 플랫폼 + 500마다 장애물
                createFullPlatforms(3000, PLATFORM_WIDTH);

                // 장애물: 500, 1000, 1500, 2000, 2500
                blocks.add(new Block(500, GROUND_Y + GROUND_HEIGHT, blockTexture));
                blocks.add(new Block(1000, GROUND_Y + GROUND_HEIGHT, blockTexture));
                blocks.add(new Block(1500, GROUND_Y + GROUND_HEIGHT, blockTexture));
                blocks.add(new Block(2000, GROUND_Y + GROUND_HEIGHT, blockTexture));
                blocks.add(new Block(2500, GROUND_Y + GROUND_HEIGHT, blockTexture));

                System.out.println("Stage 1 loaded - Obstacles at 500, 1000, 1500, 2000, 2500");
                break;

            case 2:
                // 2스테이지: 특정 배치
                // 0~500: 플랫폼 + 장애물(500)
                for(int i = 0; i < 5; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }
                blocks.add(new Block(500, GROUND_Y + GROUND_HEIGHT, blockTexture));

                // 500~900: 플랫폼
                for(int i = 5; i < 9; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }

                // 900~1000: 낭떠러지 (플랫폼 없음) + 코인 (가운데)
                objects.add(new CoinObject(objectTexture, 910, GROUND_Y + GROUND_HEIGHT + 50, 0));


                // 1000~1500: 플랫폼 + 장애물(1500)
                for(int i = 10; i < 15; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }
                blocks.add(new Block(1500, GROUND_Y + GROUND_HEIGHT, blockTexture));

                // 1500~1900: 플랫폼
                for(int i = 15; i < 19; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }

                // 1900~2000: 낭떠러지 (플랫폼 없음)
                objects.add(new CoinObject(objectTexture, 1910, GROUND_Y + GROUND_HEIGHT + 50, 0));

                // 2000~2500: 플랫폼 + 장애물(2500)
                for(int i = 20; i < 25; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }
                blocks.add(new Block(2500, GROUND_Y + GROUND_HEIGHT, blockTexture));

                // 2500~3000: 플랫폼
                for(int i = 25; i < 30; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }

                System.out.println("Stage 2 loaded - Obstacles at 500, 1500, 2500 / Pits at 900-1000, 1900-2000");
                break;

            case 3:
                // 3스테이지: 2와 같은 배치에 코인 추가

                // 0~500: 플랫폼 + 장애물(500) + 코인
                for(int i = 0; i < 5; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }
                blocks.add(new Block(500, GROUND_Y + GROUND_HEIGHT, blockTexture));
                objects.add(new CoinObject(objectTexture, 500, GROUND_Y + GROUND_HEIGHT + 50, 0));

                // 500~900: 플랫폼
                for(int i = 5; i < 9; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }



                // 900~1000: 낭떠러지
                objects.add(new CoinObject(objectTexture, 910, GROUND_Y + GROUND_HEIGHT + 50, 0));

                // 1000~1500: 플랫폼 + 장애물(1500) + 코인
                for(int i = 10; i < 15; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }
                blocks.add(new Block(1500, GROUND_Y + GROUND_HEIGHT, blockTexture));
                objects.add(new CoinObject(objectTexture, 1500, GROUND_Y + GROUND_HEIGHT + 50, 0));

                // 1500~1900: 플랫폼
                for(int i = 15; i < 19; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }

                // 1900~2000: 낭떠러지
                objects.add(new CoinObject(objectTexture, 1910, GROUND_Y + GROUND_HEIGHT + 50, 0));

                // 2000~2500: 플랫폼 + 장애물(2500) + 코인
                for(int i = 20; i < 25; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }
                blocks.add(new Block(2500, GROUND_Y + GROUND_HEIGHT, blockTexture));
                objects.add(new CoinObject(objectTexture, 2500, GROUND_Y + GROUND_HEIGHT + 50, 0));

                // 2500~3000: 플랫폼
                for(int i = 25; i < 30; i++) {
                    Rectangle platform = new Rectangle(i * PLATFORM_WIDTH, GROUND_Y, PLATFORM_WIDTH, GROUND_HEIGHT);
                    platforms.add(platform);
                }

                System.out.println("Stage 3 loaded - Obstacles with coins at 500, 1500, 2500 / Pits at 900-1000, 1900-2000");
                break;
        }

        flag = new Flag(2999, GROUND_Y + GROUND_HEIGHT, new Texture("flag.png"));
    }

    // 1스테이지용: 전체 플랫폼 생성
    private void createFullPlatforms(float mapLength, float platformWidth) {
        for(int i = 0; i < mapLength / platformWidth; i++) {
            float x = i * platformWidth;
            if(x >= mapLength) break;

            Rectangle platform = new Rectangle(x, GROUND_Y, platformWidth, GROUND_HEIGHT);
            platforms.add(platform);
        }
    }

    public void setTotalScore(int score) {
        totalScore = score;
        this.score = 0;  // 현재 스테이지 점수는 0부터
    }

    public int getScore() {
        return totalScore + score;  // 누적 점수 + 현재 점수
    }
    public Array<CoinObject> getObjects() { return objects; }
    public Array<Block> getBlock() { return blocks; }
    public Array<Rectangle> getPlatforms() { return platforms; }
    public GameCharacter getPlayer() { return player; }
    public Flag getFlag() { return flag; }
    public void onPlayerJump() { player.jump(); }
    public boolean isGameClear() { return isGameClear; }
}
