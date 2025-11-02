package io.jbnu.ac.kr;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Rectangle;

public class GameCharacter {
    public Vector2 position;
    public Vector2 velocity;
    public Sprite sprite;
    public int Hp = 5;
    public boolean isGrounded = false;
    public boolean isMovingRight = true;
    public boolean isMovingLeft = false;

    public GameCharacter(Texture texture, float startX, float startY) {
        this.position = new Vector2(startX, startY);
        this.velocity = new Vector2(0, 0);
        this.sprite = new Sprite(texture);
        this.sprite.setPosition(position.x, position.y);
        this.sprite.setSize(50,50);
    }

    public void jump() {
        if(isGrounded) {
            velocity.y = 800f;
            isGrounded = false;
        }
    }

    public void moveRight() {
        isMovingRight = true;
        isMovingLeft = false;
    }

    public void moveLeft() {
        isMovingRight = false;
        isMovingLeft = true;
    }

    public void syncSpriteToPosition() {
        sprite.setPosition(position.x, position.y);
    }

    public Rectangle getBounds() {
        return sprite.getBoundingRectangle();
    }

    public void draw(SpriteBatch batch) {
        sprite.draw(batch);
    }
}

