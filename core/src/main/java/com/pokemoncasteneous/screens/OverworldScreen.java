package com.pokemoncasteneous.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.pokemoncasteneous.PokemonGame;
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
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final BitmapFont font = new BitmapFont();
    private final Texture fallbackPlayerTexture;
    private final Texture grassTile;
    private final Texture pathTile;
    private final Texture treeSprite;
    private Texture playerSheet;
    private final TextureRegion[][] playerFrames;
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
        Pixmap pixmap = new Pixmap(32, 40, Pixmap.Format.RGBA8888);
        pixmap.setColor(PLAYER_TUNIC); pixmap.fillRectangle(7, 0, 18, 24);
        pixmap.setColor(PLAYER_SKIN); pixmap.fillCircle(16, 31, 9);
        pixmap.setColor(PLAYER_TUNIC); pixmap.fillRectangle(7, 15, 18, 4);
        fallbackPlayerTexture = new Texture(pixmap);
        pixmap.dispose();
        font.getData().setScale(0.08f);

        grassTile = loadOptionalTexture("grass.png");
        pathTile = loadOptionalTexture("dirt.png");
        treeSprite = loadOptionalTexture("tree.png");
        playerSheet = loadOptionalTexture("player.png");
        if (playerSheet != null && playerSheet.getWidth() == 128 && playerSheet.getHeight() == 128) {
            playerFrames = TextureRegion.split(playerSheet, 32, 32);
        } else {
            if (playerSheet != null) playerSheet.dispose();
            playerSheet = null;
            playerFrames = null;
        }
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
        shapes.setProjectionMatrix(camera.combined);
        if (grassTile == null || pathTile == null) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawGroundGrid();
            shapes.end();
        } else {
            drawGroundSprites();
        }
        if (treeSprite == null) {
            shapes.begin(ShapeRenderer.ShapeType.Filled);
            drawTrees();
            shapes.end();
        } else {
            drawTreeSprites();
        }
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
                targetX = startX + dx;
                targetY = startY + dy;
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

    private void drawGroundGrid() {
        int firstX = MathUtils.floor(camera.position.x - UiConstants.VIEW_COLUMNS / 2f) - 1;
        int lastX = MathUtils.floor(camera.position.x + UiConstants.VIEW_COLUMNS / 2f) + 1;
        int firstY = MathUtils.floor(camera.position.y - UiConstants.VIEW_ROWS / 2f) - 1;
        int lastY = MathUtils.floor(camera.position.y + UiConstants.VIEW_ROWS / 2f) + 1;

        for (int x = firstX; x <= lastX; x++) {
            for (int y = firstY; y <= lastY; y++) {
                if (x == 0 || y == 0) shapes.setColor(PATH_SAND);
                else shapes.setColor(((x + y) & 1) == 0 ? GRASS_TILE_LIGHT : GRASS_TILE_SHADE);
                shapes.rect(x - 0.5f, y - 0.5f, 1f, 1f);
            }
        }
    }

    private void drawTrees() {
        int firstX = MathUtils.floor(camera.position.x - UiConstants.VIEW_COLUMNS / 2f) - 1;
        int lastX = MathUtils.floor(camera.position.x + UiConstants.VIEW_COLUMNS / 2f) + 1;
        int firstY = MathUtils.floor(camera.position.y - UiConstants.VIEW_ROWS / 2f) - 1;
        int lastY = MathUtils.floor(camera.position.y + UiConstants.VIEW_ROWS / 2f) + 1;

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

    private Texture loadOptionalTexture(String path) {
        if (!Gdx.files.internal(path).exists()) return null;
        Texture texture = new Texture(Gdx.files.internal(path));
        texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
        return texture;
    }

    private void drawGroundSprites() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int firstX = MathUtils.floor(camera.position.x - UiConstants.VIEW_COLUMNS / 2f) - 1;
        int lastX = MathUtils.floor(camera.position.x + UiConstants.VIEW_COLUMNS / 2f) + 1;
        int firstY = MathUtils.floor(camera.position.y - UiConstants.VIEW_ROWS / 2f) - 1;
        int lastY = MathUtils.floor(camera.position.y + UiConstants.VIEW_ROWS / 2f) + 1;
        for (int x = firstX; x <= lastX; x++) {
            for (int y = firstY; y <= lastY; y++) {
                Texture tile = x == 0 || y == 0 ? pathTile : grassTile;
                batch.draw(tile, x - 0.5f, y - 0.5f, 1f, 1f);
            }
        }
        batch.end();
    }

    private void drawTreeSprites() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int firstX = MathUtils.floor(camera.position.x - UiConstants.VIEW_COLUMNS / 2f) - 1;
        int lastX = MathUtils.floor(camera.position.x + UiConstants.VIEW_COLUMNS / 2f) + 1;
        int firstY = MathUtils.floor(camera.position.y - UiConstants.VIEW_ROWS / 2f) - 1;
        int lastY = MathUtils.floor(camera.position.y + UiConstants.VIEW_ROWS / 2f) + 1;
        for (int x = firstX; x <= lastX; x++) {
            for (int y = firstY; y <= lastY; y++) {
                if ((x == 0 || y == 0) || Math.floorMod(x * 7 + y * 3, 11) != 0) continue;
                batch.draw(treeSprite, x - 0.5f, y - 0.5f,
                        UiConstants.TREE_SPRITE_WIDTH, UiConstants.TREE_SPRITE_HEIGHT);
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
        if (playerFrames == null) {
            batch.draw(fallbackPlayerTexture, x, y,
                    UiConstants.PLAYER_SPRITE_WIDTH, UiConstants.PLAYER_SPRITE_HEIGHT);
        } else {
            int frame = moving ? 1 + (int) (animationTime / UiConstants.PLAYER_WALK_FRAME_SECONDS) % 3 : 0;
            batch.draw(playerFrames[facingRow][frame], x, y,
                    UiConstants.PLAYER_SPRITE_WIDTH, UiConstants.PLAYER_SPRITE_HEIGHT);
        }
        batch.end();
    }

    @Override public void resize(int width, int height) {
        worldViewport.update(width, height, true);
        hudViewport.update(width, height, true);
    }
    @Override public void dispose() {
        shapes.dispose();
        font.dispose();
        fallbackPlayerTexture.dispose();
        if (grassTile != null) grassTile.dispose();
        if (pathTile != null) pathTile.dispose();
        if (treeSprite != null) treeSprite.dispose();
        if (playerSheet != null) playerSheet.dispose();
    }
}
