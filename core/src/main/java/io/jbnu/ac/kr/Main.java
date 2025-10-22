package io.jbnu.ac.kr;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.ScreenUtils;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Main extends ApplicationAdapter {
    private SpriteBatch batch;


    @Override
    public void create() {
        batch = new SpriteBatch();

    }

    @Override
    public void render() {
        ScreenUtils.clear(1f, 1f, 1f, 1f);
        input();
        logic();
        draw();
    }

    public void input()
    {

    }

    public void logic()
    {

    }

    public void draw()
    {
        batch.begin();

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();

    }
}
