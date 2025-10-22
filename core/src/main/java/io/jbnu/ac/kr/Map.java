package io.jbnu.ac.kr;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;


public class Map {
    private Array<CoinObject> coins;
    private Array<Block> blocks;
    private Flag flag;

    private Texture coinTexture;
    private Texture blockTexture;

    public Map(int level)
    {
        this.coinTexture = coinTexture;
        this.blockTexture = blockTexture;
        coins = new Array<>();
        blocks = new Array<>();

        createMap(level);
    }


    public void createMap(int level)
    {

    }


    public  Flag getFlag()
    {
        return flag;
    }

    public Array<CoinObject> getCoins() {
        return coins;
    }

    public  Array<Block> getBlock(){
        return blocks;
    }
}
