package io.jbnu.ac.kr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import java.util.Iterator;

public class GameWorld {
    public final float WORLD_GRAVITY = -9.8f * 200;
    public final float FLOOR_LEVEL = 100;
    public final float DEATH_LINE = 0;

    private GameCharacter player;
    private Array<Block> blocks;
    private Array<Rectangle> pits;
    private Array<CoinObject> objects;
    private Flag flag;

    private int score;
    public int Level;
    public boolean isGameClear = false;
    public boolean isGameOver = false;

    private Texture playerTexture;
    private Texture objectTexture;
    private Texture blockTexture;

    private float worldWidth;

    // 무적 시간 관련
    private float invincibleTimer = 0f;
    private final float INVINCIBLE_TIME = 2f; // 2초 무적

    public GameWorld(Texture playerTexture, Texture objectTexture, Texture blockTexture, float worldWidth, int level) {
        this.playerTexture = playerTexture;
        this.objectTexture = objectTexture;
        this.blockTexture = blockTexture;
        this.worldWidth = worldWidth;

        player = new GameCharacter(playerTexture, 100, FLOOR_LEVEL);
        objects = new Array<>();
        blocks = new Array<>();
        pits = new Array<>();

        score = 0;
        Level = level;
        invincibleTimer = 0f;

        loadGround(level);
    }

    public void update(float delta) {
        if(isGameOver || isGameClear) return;

        float moveAmount = 7; // 자동 오른쪽 이동

        // 중력 적용
        player.velocity.y += WORLD_GRAVITY * delta;

        // 무적 시간 감소
        if(invincibleTimer > 0) {
            invincibleTimer -= delta;
        }

        // Y축 이동 (중력)
        float newY = player.position.y + player.velocity.y * delta;

        // 바닥 및 낭떠러지 충돌 검사 (Y 위치 결정)
        checkFloorAndPitCollision(newY);

        // X축 이동
        player.position.x += moveAmount;
        player.sprite.setX(player.position.x);

        // 장애물 충돌 검사 (무적 시간 중이 아닐 때만)
        if(invincibleTimer <= 0) {
            checkBlockCollision();
        }

        // 코인 충돌 검사
        checkCoinCollision();

        // 깃발 충돌 검사
        checkFlagCollision();

        // Y < 0이면 게임오버 (낭떠러지)
        if(player.position.y < DEATH_LINE) {
            isGameOver = true;
            System.out.println("Fell into pit! GAME OVER");
        }

        // 체력 <= 0이면 게임오버
        if(player.Hp <= 0) {
            isGameOver = true;
            System.out.println("HP 0! GAME OVER");
        }

        player.syncSpriteToPosition();
    }

    // 바닥 및 낭떠러지 충돌 처리
    private void checkFloorAndPitCollision(float newY) {
        Rectangle playerBounds = new Rectangle(player.position.x, newY,
            player.sprite.getWidth(), player.sprite.getHeight());

        boolean inPit = false;

        // 낭떠러지 구간에 플레이어가 있는지 확인
        for(Rectangle pit : pits) {
            if(playerBounds.overlaps(pit)) {
                inPit = true;
                break;
            }
        }

        // 낭떠러지가 아니면 바닥에 고정
        if(!inPit) {
            if(newY <= FLOOR_LEVEL) {
                player.position.y = FLOOR_LEVEL;
                player.velocity.y = 0;
                player.isGrounded = true;
            } else {
                player.position.y = newY;
                player.isGrounded = false;
            }
        } else {
            // 낭떠러지 구간이면 그냥 떨어짐
            player.position.y = newY;
            player.isGrounded = false;
        }
    }

    // 장애물 충돌 검사
    private void checkBlockCollision() {
        Rectangle playerBounds = player.sprite.getBoundingRectangle();

        for(Block block : blocks) {
            if(playerBounds.overlaps(block.getBounds())) {
                // 체력 1 감소
                player.Hp--;

                // 무적 시간 부여 (2초 동안 충돌 안함)
                invincibleTimer = INVINCIBLE_TIME;

                // 뒤로 밀려남
                player.position.x -= 80; // 더 멀리 밀려남
                player.velocity.y = 300f; // 약간 위로 튕김

                System.out.println("Hit Block! HP: " + player.Hp + " | Invincible for " + INVINCIBLE_TIME + "s");
                break;
            }
        }
    }

    // 코인 충돌 검사
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

    // 깃발 충돌 검사
    private void checkFlagCollision() {
        if(player.sprite.getBoundingRectangle().overlaps(flag.bounds)) {
            isGameClear = true;
            score += 100; // 완주 보너스
            System.out.println("Stage " + Level + " Clear! Total Score: " + score);
        }
    }

    // 레벨별 맵 생성
    private void loadGround(int level) {
        blocks.clear();
        pits.clear();
        objects.clear();

        switch(level) {
            case 1:
                // 1스테이지: 장애물만
                blocks.add(new Block(500, FLOOR_LEVEL, blockTexture));
                blocks.add(new Block(1000, FLOOR_LEVEL, blockTexture));
                blocks.add(new Block(1500, FLOOR_LEVEL, blockTexture));
                System.out.println("=== Stage 1 Loaded ===");
                break;

            case 2:
                // 2스테이지: 낭떠러지와 장애물 번갈아
                for(int i = 0; i < 7; i++) {
                    float x = 250 + (250 * i);
                    if(i % 2 == 0) {
                        pits.add(new Rectangle(x, 0, 100, FLOOR_LEVEL));
                        System.out.println("Pit at x=" + x);
                    } else {
                        blocks.add(new Block(x, FLOOR_LEVEL, blockTexture));
                        System.out.println("Block at x=" + x);
                    }
                }
                System.out.println("=== Stage 2 Loaded ===");
                break;

            case 3:
                // 3스테이지: 낭떠러지, 장애물, 코인
                for(int i = 0; i < 7; i++) {
                    float x = 250 + (250 * i);
                    if(i % 2 == 0) {
                        pits.add(new Rectangle(x, 0, 100, FLOOR_LEVEL));
                    } else {
                        blocks.add(new Block(x, FLOOR_LEVEL, blockTexture));
                        objects.add(new CoinObject(objectTexture, x + 10, FLOOR_LEVEL + 70, 0));
                    }
                }
                System.out.println("=== Stage 3 Loaded ===");
                break;
        }

        // 깃발 생성
        flag = new Flag(1999, FLOOR_LEVEL, new Texture("flag.png"));
    }

    // Getter들
    public int getScore() { return score; }
    public Array<CoinObject> getObjects() { return objects; }
    public Array<Block> getBlock() { return blocks; }
    public Array<Rectangle> getPits() { return pits; }
    public GameCharacter getPlayer() { return player; }
    public Flag getFlag() { return flag; }
    public void onPlayerJump() { player.jump(); }
    public boolean isGameClear() { return isGameClear; }
}
