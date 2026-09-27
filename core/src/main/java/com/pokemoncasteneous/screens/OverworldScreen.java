package com.pokemoncasteneous.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.ScreenUtils;
import com.pokemoncasteneous.PokemonGame;

public final class OverworldScreen extends ScreenAdapter {
    private final PokemonGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final Texture playerTexture;

    public OverworldScreen(PokemonGame game) {
        this.game = game;
        Pixmap pixmap = new Pixmap(32, 40, Pixmap.Format.RGBA8888);
        pixmap.setColor(0.12f, 0.24f, 0.34f, 1f); pixmap.fillRectangle(7, 0, 18, 24);
        pixmap.setColor(0.96f, 0.72f, 0.43f, 1f); pixmap.fillCircle(16, 31, 9);
        pixmap.setColor(0.12f, 0.24f, 0.34f, 1f); pixmap.fillRectangle(7, 15, 18, 4);
        playerTexture = new Texture(pixmap);
        pixmap.dispose();
        font.getData().setScale(1.15f);
    }

    @Override
    public void render(float delta) {
        float speed = 190f * Math.min(delta, 0.05f);
        if (Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A)) game.state().playerX -= speed;
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D)) game.state().playerX += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W)) game.state().playerY += speed;
        if (Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S)) game.state().playerY -= speed;
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(game.state().playerX, game.state().playerY, 0);
        camera.update();

        ScreenUtils.clear(0.36f, 0.58f, 0.42f, 1f);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        for (int x = -4; x <= 4; x++) for (int y = -4; y <= 4; y++) {
            shapes.setColor(((x + y) & 1) == 0 ? 0.38f : 0.35f, 0.57f, 0.39f, 1f);
            shapes.rect(x * 160f - 80, y * 160f - 80, 160, 160);
        }
        shapes.setColor(0.78f, 0.69f, 0.49f, 1f);
        shapes.rect(-52, -1200, 104, 2400);
        shapes.rect(-1200, -52, 2400, 104);
        for (int x = -4; x <= 4; x++) for (int y = -4; y <= 4; y++) {
            if ((x * 7 + y * 3) % 5 == 0 && (x != 0 || y != 0)) {
                shapes.setColor(0.18f, 0.39f, 0.31f, 1f);
                shapes.circle(x * 310f + 85, y * 280f + 100, 32);
                shapes.setColor(0.36f, 0.25f, 0.19f, 1f);
                shapes.rect(x * 310f + 79, y * 280f + 48, 12, 28);
            }
        }
        shapes.end();
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(playerTexture, game.state().playerX - 16, game.state().playerY - 20);
        batch.end();

        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.position.set(Gdx.graphics.getWidth() / 2f, Gdx.graphics.getHeight() / 2f, 0);
        camera.update();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        font.setColor(0.96f, 0.96f, 0.88f, 1f);
        font.draw(batch, "FERNWOOD  /  ROUTE 01", 28, Gdx.graphics.getHeight() - 30);
        font.draw(batch, "WASD / ARROWS  MOVE       ENTER  ENCOUNTER", 28, 28);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.state().beginEncounter();
            game.getScreen().dispose();
            game.setScreen(new BattleScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    @Override public void resize(int width, int height) { camera.viewportWidth = width; camera.viewportHeight = height; }
    @Override public void dispose() { shapes.dispose(); font.dispose(); playerTexture.dispose(); }
}
