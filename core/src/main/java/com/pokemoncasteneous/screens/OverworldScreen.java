package com.pokemoncasteneous.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pokemoncasteneous.PokemonGame;
import com.pokemoncasteneous.assets.OverworldMap;
import com.pokemoncasteneous.assets.OverworldTextures;
import com.pokemoncasteneous.assets.Tile;
import com.pokemoncasteneous.constants.GameplayConstants;
import com.pokemoncasteneous.constants.UiConstants;
import com.pokemoncasteneous.utils.ScreenUtils;

import static com.pokemoncasteneous.constants.ColorConstants.*;

public final class OverworldScreen extends ScreenAdapter {
    private final PokemonGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final FitViewport worldViewport = new FitViewport(
            UiConstants.VIEW_COLUMNS, UiConstants.VIEW_ROWS, camera);
    private final FitViewport hudViewport = new FitViewport(
            UiConstants.VIEW_COLUMNS, UiConstants.VIEW_ROWS, hudCamera);
    private final BitmapFont font = new BitmapFont();
    private final OverworldMap overworldMap;
    private final OverworldTextures textures;
    private float startX;
    private float startY;
    private float targetX;
    private float targetY;
    private float stepProgress;
    private float animationTime;
    private int facingRow;
    private boolean moving;

    public OverworldScreen(PokemonGame game) {
        this.game = game;
        overworldMap = new OverworldMap("maps/overworld.map");
        textures = new OverworldTextures(overworldMap.tileCatalog());
        if (!game.state().overworldPositionInitialized) {
            game.state().playerX = overworldMap.width() / 2;
            game.state().playerY = (overworldMap.height() - 1) / 2;
            game.state().overworldPositionInitialized = true;
        }
        font.getData().setScale(0.08f);
    }

    @Override
    public void render(float delta) {
        updateMovement(delta);
        animationTime = moving ? animationTime + delta : 0f;

        // The viewport maps the fixed logical grid onto the window and keeps its 16:9 shape.
        worldViewport.apply();
        // The half-square camera offset puts the viewport edges on tile boundaries when the player rests.
        camera.position.set(game.state().playerX + 0.5f, game.state().playerY + 0.5f, 0);
        camera.update();

        ScreenUtils.clear(OVERWORLD_GRASS);
        drawMap();
        drawPlayer();

        // HUD coordinates use the same virtual grid, but stay fixed while the world camera follows the player.
        hudViewport.apply();
        hudCamera.position.set(UiConstants.VIEW_COLUMNS / 2f, UiConstants.VIEW_ROWS / 2f, 0);
        hudCamera.update();
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(HUD_TEXT);
        font.draw(batch, "FERNWOOD  /  ROUTE 01", 1, UiConstants.VIEW_ROWS - 1);
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
        if (dx < 0) facingRow = 1;
        else if (dx > 0) facingRow = 2;
        else if (dy > 0) facingRow = 3;
        else if (dy < 0) facingRow = 0;
        float timeLeft = delta;
        while (timeLeft > 0f && (moving || dx != 0 || dy != 0)) {
            if (!moving) {
                startX = Math.round(game.state().playerX);
                startY = Math.round(game.state().playerY);
                targetX = MathUtils.clamp(startX + dx, 0, overworldMap.width() - 1);
                targetY = MathUtils.clamp(startY + dy, 0, overworldMap.height() - 1);
                if (targetX == startX && targetY == startY) break;
                stepProgress = 0f;
                moving = true;
            }

            float timeForStep = (1f - stepProgress) / GameplayConstants.PLAYER_MOVE_SPEED;
            float stepTime = Math.min(timeLeft, timeForStep);
            stepProgress = Math.min(1f, stepProgress + stepTime * GameplayConstants.PLAYER_MOVE_SPEED);
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

    private void drawMap() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int firstX = Math.max(0, MathUtils.floor(camera.position.x - UiConstants.VIEW_COLUMNS / 2f) - 1);
        int lastX = Math.min(overworldMap.width() - 1, MathUtils.floor(camera.position.x + UiConstants.VIEW_COLUMNS / 2f) + 1);
        int firstY = Math.max(0, MathUtils.floor(camera.position.y - UiConstants.VIEW_ROWS / 2f) - 1);
        int lastY = Math.min(overworldMap.height() - 1, MathUtils.floor(camera.position.y + UiConstants.VIEW_ROWS / 2f) + 1);

        for (int y = firstY; y <= lastY; y++) {
            for (int x = firstX; x <= lastX; x++) {
                Tile tile = overworldMap.tileAt(x, y);
                Tile ground = tile.isForeground() ? overworldMap.tileDefinition(tile.underlayId()) : tile;
                Texture groundTexture = textures.textureFor(ground);
                if (groundTexture != null) batch.draw(groundTexture, x - 0.5f, y - 0.5f, 1f, 1f);
            }
        }
        for (int y = firstY; y <= lastY; y++) {
            for (int x = firstX; x <= lastX; x++) {
                Tile tile = overworldMap.tileAt(x, y);
                if (!tile.isForeground()) continue;
                Texture sprite = textures.textureFor(tile);
                if (sprite != null) {
                    batch.draw(sprite, x - 0.5f, y - 0.5f,
                            UiConstants.TREE_SPRITE_WIDTH, UiConstants.TREE_SPRITE_HEIGHT);
                }
            }
        }
        batch.end();
    }

    private void drawPlayer() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        float x = game.state().playerX - UiConstants.PLAYER_SPRITE_WIDTH / 2f;
        float y = game.state().playerY - UiConstants.PLAYER_SPRITE_HEIGHT / 2f;

        int frame = moving ? 1 + (int) (animationTime / UiConstants.PLAYER_WALK_FRAME_SECONDS) % 3 : 0;
        batch.draw(textures.playerFrames[facingRow][frame], x, y, UiConstants.PLAYER_SPRITE_WIDTH, UiConstants.PLAYER_SPRITE_HEIGHT);

        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }

    @Override
    public void dispose() {
        font.dispose();
        textures.dispose();
    }
}
