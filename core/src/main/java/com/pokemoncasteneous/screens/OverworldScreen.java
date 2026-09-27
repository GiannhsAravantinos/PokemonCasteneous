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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pokemoncasteneous.PokemonGame;
import com.pokemoncasteneous.constants.GameConstants;

import static com.pokemoncasteneous.constants.ColorConstants.*;

public final class OverworldScreen extends ScreenAdapter {
    private final PokemonGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final FitViewport worldViewport = new FitViewport(
            GameConstants.VIEW_COLUMNS, GameConstants.VIEW_ROWS, camera);
    private final FitViewport hudViewport = new FitViewport(
            GameConstants.VIEW_COLUMNS, GameConstants.VIEW_ROWS, hudCamera);
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final Texture playerTexture;
    private float startX;
    private float startY;
    private float targetX;
    private float targetY;
    private float stepProgress;
    private boolean moving;

    public OverworldScreen(PokemonGame game) {
        this.game = game;
        Pixmap pixmap = new Pixmap(32, 40, Pixmap.Format.RGBA8888);
        pixmap.setColor(PLAYER_TUNIC); pixmap.fillRectangle(7, 0, 18, 24);
        pixmap.setColor(PLAYER_SKIN); pixmap.fillCircle(16, 31, 9);
        pixmap.setColor(PLAYER_TUNIC); pixmap.fillRectangle(7, 15, 18, 4);
        playerTexture = new Texture(pixmap);
        pixmap.dispose();
        font.getData().setScale(0.08f);
    }

    @Override
    public void render(float delta) {
        updateMovement(delta);

        // The viewport maps the fixed 64-by-36 world grid onto the window and keeps its 16:9 shape.
        worldViewport.apply();
        // The half-square camera offset puts the viewport edges on tile boundaries when the player rests.
        camera.position.set(game.state().playerX + 0.5f, game.state().playerY + 0.5f, 0);
        camera.update();

        ScreenUtils.clear(OVERWORLD_GRASS.r, OVERWORLD_GRASS.g, OVERWORLD_GRASS.b, OVERWORLD_GRASS.a);
        shapes.setProjectionMatrix(camera.combined);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        drawGroundGrid();
        drawTrees();
        shapes.end();

        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(playerTexture,
                game.state().playerX - GameConstants.PLAYER_SPRITE_WIDTH / 2f,
                game.state().playerY - GameConstants.PLAYER_SPRITE_HEIGHT / 2f,
                GameConstants.PLAYER_SPRITE_WIDTH,
                GameConstants.PLAYER_SPRITE_HEIGHT);
        batch.end();

        // HUD coordinates use the same virtual grid, but stay fixed while the world camera follows the player.
        hudViewport.apply();
        hudCamera.position.set(GameConstants.VIEW_COLUMNS / 2f, GameConstants.VIEW_ROWS / 2f, 0);
        hudCamera.update();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(HUD_TEXT);
        font.draw(batch, "FERNWOOD  /  ROUTE 01", 1, GameConstants.VIEW_ROWS - 1);
        font.draw(batch, "WASD / ARROWS  MOVE       ENTER  ENCOUNTER", 1, 1);
        batch.end();

        // Enter starts the encounter and hands control to the battle screen.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            game.state().beginEncounter();
            game.getScreen().dispose();
            game.setScreen(new BattleScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    private void updateMovement(float delta) {
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        int dx = left == right ? 0 : (right ? 1 : -1);
        int dy = down == up ? 0 : (up ? 1 : -1);

        // Movement is one cell at a time; releasing a key finishes the current step on its destination cell.
        if (dx != 0 && dy != 0) dx = 0;
        float timeLeft = delta;
        while (timeLeft > 0f && (moving || dx != 0 || dy != 0)) {
            if (!moving) {
                startX = Math.round(game.state().playerX);
                startY = Math.round(game.state().playerY);
                targetX = startX + dx;
                targetY = startY + dy;
                stepProgress = 0f;
                moving = true;
            }

            float timeForStep = (1f - stepProgress) / GameConstants.PLAYER_MOVE_SPEED;
            float stepTime = Math.min(timeLeft, timeForStep);
            stepProgress = Math.min(1f, stepProgress + stepTime * GameConstants.PLAYER_MOVE_SPEED);
            timeLeft -= stepTime;
            game.state().playerX = startX + (targetX - startX) * stepProgress;
            game.state().playerY = startY + (targetY - startY) * stepProgress;

            if (stepProgress >= 1f) {
                game.state().playerX = targetX;
                game.state().playerY = targetY;
                moving = false;
                stepProgress = 0f;
                if (dx == 0 && dy == 0) break;
            }
        }
    }

    private void drawGroundGrid() {
        int firstX = MathUtils.floor(camera.position.x - GameConstants.VIEW_COLUMNS / 2f) - 1;
        int lastX = MathUtils.floor(camera.position.x + GameConstants.VIEW_COLUMNS / 2f) + 1;
        int firstY = MathUtils.floor(camera.position.y - GameConstants.VIEW_ROWS / 2f) - 1;
        int lastY = MathUtils.floor(camera.position.y + GameConstants.VIEW_ROWS / 2f) + 1;

        for (int x = firstX; x <= lastX; x++) {
            for (int y = firstY; y <= lastY; y++) {
                if (x == 0 || y == 0) shapes.setColor(PATH_SAND);
                else shapes.setColor(((x + y) & 1) == 0 ? GRASS_TILE_LIGHT : GRASS_TILE_SHADE);
                shapes.rect(x - 0.5f, y - 0.5f, 1f, 1f);
            }
        }
    }

    private void drawTrees() {
        int firstX = MathUtils.floor(camera.position.x - GameConstants.VIEW_COLUMNS / 2f) - 1;
        int lastX = MathUtils.floor(camera.position.x + GameConstants.VIEW_COLUMNS / 2f) + 1;
        int firstY = MathUtils.floor(camera.position.y - GameConstants.VIEW_ROWS / 2f) - 1;
        int lastY = MathUtils.floor(camera.position.y + GameConstants.VIEW_ROWS / 2f) + 1;

        for (int x = firstX; x <= lastX; x++) {
            for (int y = firstY; y <= lastY; y++) {
                if ((x == 0 || y == 0) || Math.floorMod(x * 7 + y * 3, 11) != 0) continue;
                shapes.setColor(TREE_TRUNK);
                shapes.rect(x - 0.08f, y - 0.38f, 0.16f, 0.38f);
                shapes.setColor(TREE_FOLIAGE);
                shapes.circle(x, y + 0.08f, 0.36f);
            }
        }
    }

    @Override public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }
    @Override public void dispose() { shapes.dispose(); font.dispose(); playerTexture.dispose(); }
}
