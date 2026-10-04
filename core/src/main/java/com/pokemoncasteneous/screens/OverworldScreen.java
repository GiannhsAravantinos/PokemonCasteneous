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
import com.pokemoncasteneous.constants.UiConstants;
import com.pokemoncasteneous.overworld.GridPosition;
import com.pokemoncasteneous.overworld.MovementInput;
import com.pokemoncasteneous.overworld.OverworldMovementController;
import com.pokemoncasteneous.overworld.PlayerPosition;
import com.pokemoncasteneous.utils.IndexUtils;
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
    private final OverworldMovementController movementController;
    private float animationTime;

    public OverworldScreen(PokemonGame game) {
        this.game = game;
        overworldMap = new OverworldMap("maps/overworld.map");
        textures = new OverworldTextures(overworldMap.tileCatalog());
        if (!game.state().overworldPositionInitialized) {
            game.state().playerPosition = PlayerPosition.at(
                    new GridPosition(overworldMap.width() / 2, (overworldMap.height() - 1) / 2));
            game.state().overworldPositionInitialized = true;
        }
        movementController = new OverworldMovementController(game.state(), overworldMap);
        font.getData().setScale(0.08f);
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

    private MovementInput movementInput() {
        boolean left = Gdx.input.isKeyPressed(Input.Keys.LEFT) || Gdx.input.isKeyPressed(Input.Keys.A);
        boolean right = Gdx.input.isKeyPressed(Input.Keys.RIGHT) || Gdx.input.isKeyPressed(Input.Keys.D);
        boolean up = Gdx.input.isKeyPressed(Input.Keys.UP) || Gdx.input.isKeyPressed(Input.Keys.W);
        boolean down = Gdx.input.isKeyPressed(Input.Keys.DOWN) || Gdx.input.isKeyPressed(Input.Keys.S);
        int dx = left == right ? 0 : (right ? 1 : -1);
        int dy = down == up ? 0 : (up ? 1 : -1);
        return new MovementInput(dx, dy);
    }

    private void drawMap() {
        SpriteBatch batch = game.batch();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        int firstX = Math.max(0, MathUtils.floor(camera.position.x - UiConstants.VIEW_COLUMNS / 2f) - 1);
        int lastX = Math.min(overworldMap.width() - 1, MathUtils.floor(camera.position.x + UiConstants.VIEW_COLUMNS / 2f) + 1);
        int firstY = Math.max(0, MathUtils.floor(camera.position.y - UiConstants.VIEW_ROWS / 2f) - 1);
        int lastY = Math.min(overworldMap.height() - 1, MathUtils.floor(camera.position.y + UiConstants.VIEW_ROWS / 2f) + 1);

        IndexUtils.cartesianIndices(firstX, lastX, firstY, lastY).forEach(index -> {
            Tile tile = overworldMap.tileAt(index.x(), index.y());
            Tile ground = tile.isForeground() ? overworldMap.tileDefinition(tile.getUnderlayId()) : tile;
            Texture groundTexture = textures.textureFor(ground);
            if (groundTexture != null) {
                batch.draw(groundTexture, index.x() - 0.5f, index.y() - 0.5f, 1f, 1f);
            }
        });
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

    private record TileAt(IndexUtils.CartesianIndex index, Tile tile) { }
}
