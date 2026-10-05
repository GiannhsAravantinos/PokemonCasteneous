package com.pokemoncasteneous.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.pokemoncasteneous.PokemonGame;
import com.pokemoncasteneous.assets.OverworldMap;
import com.pokemoncasteneous.assets.OverworldTextures;
import com.pokemoncasteneous.assets.Tile;
import com.pokemoncasteneous.constants.GameplayConstants;
import com.pokemoncasteneous.constants.UiConstants;
import com.pokemoncasteneous.overworld.GridPosition;
import com.pokemoncasteneous.overworld.MovementInput;
import com.pokemoncasteneous.overworld.OverworldMovementController;
import com.pokemoncasteneous.overworld.PlayerPosition;
import com.pokemoncasteneous.player.PlayerInfo;
import com.pokemoncasteneous.utils.IndexUtils;
import com.pokemoncasteneous.utils.ScreenUtils;

import static com.pokemoncasteneous.constants.ColorConstants.*;

public final class OverworldScreen extends ScreenAdapter {
    private final PokemonGame game;
    private final OrthographicCamera camera = new OrthographicCamera();
    private final OrthographicCamera hudCamera = new OrthographicCamera();
    private final FitViewport worldViewport = new FitViewport(
            UiConstants.VIEW_COLUMNS, UiConstants.VIEW_ROWS, camera);
    private final ScreenViewport hudViewport = new ScreenViewport(hudCamera);
    private final BitmapFont font = new BitmapFont();
    private final ShapeRenderer shapes = new ShapeRenderer();
    private final OverworldMap overworldMap;
    private final OverworldTextures textures;
    private final OverworldMovementController movementController;
    private float animationTime;

    public OverworldScreen(PokemonGame game) {
        this.game = game;
        overworldMap = new OverworldMap("maps/overworld.map");
        textures = new OverworldTextures(overworldMap.getTileCatalog());
        if (!game.state().overworldPositionInitialized) {
            game.state().playerPosition = PlayerPosition.at(
                    new GridPosition(overworldMap.getWidth() / 2, (overworldMap.getHeight() - 1) / 2));
            game.state().overworldPositionInitialized = true;
        }
        movementController = new OverworldMovementController(game.state(), overworldMap);
        font.getData().setScale(2f);
    }

    @Override
    public void render(float delta) {
        movementController.updateMovementFunctionality(delta, movementInput());
        animationTime = movementController.isMoving() ? animationTime + delta : 0f;

        // The viewport maps the fixed logical grid onto the window and keeps its 16:9 shape.
        worldViewport.apply();
        // The half-square camera offset puts the viewport edges on tile boundaries when the player rests.
        PlayerPosition playerPosition = game.state().playerPosition;
        camera.position.set(playerPosition.x() + 0.5f, playerPosition.y() + 0.5f, 0);
        camera.update();

        ScreenUtils.clear(OVERWORLD_GRASS);
        drawMap();
        drawPlayer();

        // HUD coordinates are pixel-based so bitmap text stays crisp while the world camera follows the player.
        hudViewport.apply();
        hudCamera.update();
        if (game.state().playerInfoVisible) {
            drawPlayerInfo();
        }
        drawHudText();

        // Enter starts the encounter and hands control to the battle screen.
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.state().beginEncounter();
            game.getScreen().dispose();
            game.setScreen(new BattleScreen(game));
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.I)) {
            game.state().playerInfoVisible = !game.state().playerInfoVisible;
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) Gdx.app.exit();
    }

    private MovementInput movementInput() {
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        int dx = left == right ? 0 : (right ? 1 : -1);
        int dy = down == up ? 0 : (up ? 1 : -1);
        int speedMultiplier = Gdx.input.isKeyPressed(Input.Keys.SPACE)
                ? GameplayConstants.PLAYER_RUN_SPEED_MULTIPLIER
                : GameplayConstants.PLAYER_WALK_SPEED_MULTIPLIER;
        return new MovementInput(dx, dy, speedMultiplier);
    }

    private void drawMap() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int firstX = Math.max(0, MathUtils.floor(camera.position.x - UiConstants.VIEW_COLUMNS / 2f) - 1);
        int lastX = Math.min(overworldMap.getWidth() - 1, MathUtils.floor(camera.position.x + UiConstants.VIEW_COLUMNS / 2f) + 1);
        int firstY = Math.max(0, MathUtils.floor(camera.position.y - UiConstants.VIEW_ROWS / 2f) - 1);
        int lastY = Math.min(overworldMap.getHeight() - 1, MathUtils.floor(camera.position.y + UiConstants.VIEW_ROWS / 2f) + 1);

        // Draw base ground tiles first so foreground sprites can sit on top.
        IndexUtils.cartesianIndices(firstX, lastX, firstY, lastY).forEach(index -> {
            Tile tile = overworldMap.tileAt(index.x(), index.y());
            Tile ground = tile.isForeground() ? overworldMap.getTileCatalog().tileById(tile.getUnderlayId()) : tile;
            Texture groundTexture = textures.textureFor(ground);
            if (groundTexture != null) {
                batch.draw(groundTexture, index.x() - 0.5f, index.y() - 0.5f, 1f, 1f);
            }
        });
        // Draw foreground tiles after ground tiles so tall sprites overlap correctly.
        IndexUtils.cartesianIndices(firstX, lastX, firstY, lastY)
                .map(index -> new TileAt(index, overworldMap.tileAt(index.x(), index.y())))
                .filter(tileAt -> tileAt.tile().isForeground())
                .forEach(tileAt -> {
                    Texture sprite = textures.textureFor(tileAt.tile());
                    if (sprite != null) {
                        IndexUtils.CartesianIndex index = tileAt.index();
                        batch.draw(sprite, index.x() - 0.5f, index.y() - 0.5f,
                                UiConstants.TREE_SPRITE_WIDTH, UiConstants.TREE_SPRITE_HEIGHT);
                    }
                });
        batch.end();
    }

    private void drawPlayer() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        PlayerPosition playerPosition = game.state().playerPosition;
        float x = playerPosition.x() - UiConstants.PLAYER_SPRITE_WIDTH / 2f;
        float y = playerPosition.y() - UiConstants.PLAYER_SPRITE_HEIGHT / 2f;

        int frame = movementController.isMoving() ? 1 + (int) (animationTime / UiConstants.PLAYER_WALK_FRAME_SECONDS) % 3 : 0;
        batch.draw(textures.playerFrames[movementController.facing().spriteRow()][frame],
                x, y, UiConstants.PLAYER_SPRITE_WIDTH, UiConstants.PLAYER_SPRITE_HEIGHT);

        batch.end();
    }

    private void drawPlayerInfo() {
        // Step 1: Project the player's world position into HUD pixel coordinates.
        PlayerPosition playerPosition = game.state().playerPosition;
        Vector3 screenPosition = camera.project(
                new Vector3(playerPosition.x(), playerPosition.y() + UiConstants.PLAYER_SPRITE_HEIGHT / 2f, 0),
                worldViewport.getScreenX(),
                worldViewport.getScreenY(),
                worldViewport.getScreenWidth(),
                worldViewport.getScreenHeight());

        // Step 2: Size and clamp the panel so it hovers near the player without leaving the window.
        float panelWidth = 420f;
        float panelHeight = 208f;
        float panelX = MathUtils.clamp(screenPosition.x - panelWidth / 2f, 12f, hudViewport.getWorldWidth() - panelWidth - 12f);
        float panelY = MathUtils.clamp(screenPosition.y + 22f, 58f, hudViewport.getWorldHeight() - panelHeight - 42f);

        // Step 3: Draw the translucent panel background and border before drawing text.
        shapes.setProjectionMatrix(hudCamera.combined);
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapes.begin(ShapeRenderer.ShapeType.Filled);
        shapes.setColor(PLAYER_INFO_PANEL);
        shapes.rect(panelX, panelY, panelWidth, panelHeight);
        shapes.end();

        shapes.begin(ShapeRenderer.ShapeType.Line);
        shapes.setColor(PLAYER_INFO_BORDER);
        shapes.rect(panelX, panelY, panelWidth, panelHeight);
        shapes.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        PlayerInfo playerInfo = game.state().playerInfo;
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        // Step 4: Draw the player record fields in the same HUD coordinate space.
        font.setColor(HUD_TEXT);
        font.draw(batch, "ID: " + playerInfo.id(), panelX + 28f, panelY + 168f);
        font.draw(batch, "Name: " + playerInfo.name(), panelX + 28f, panelY + 124f);
        font.draw(batch, "Gender: " + playerInfo.gender().displayName(), panelX + 28f, panelY + 80f);
        font.draw(batch, "Age: " + playerInfo.age(), panelX + 28f, panelY + 36f);
        batch.end();
    }

    private void drawHudText() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        font.setColor(HUD_TEXT);
        font.draw(batch, "FERNWOOD / ROUTE 01", 24f, hudViewport.getWorldHeight() - 24f);
        font.draw(batch, "WASD / ARROWS  MOVE   ENTER  ENCOUNTER   I  INFO", 24f, 28f);
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
        shapes.dispose();
        textures.dispose();
    }

    private record TileAt(IndexUtils.CartesianIndex index, Tile tile) { }
}
