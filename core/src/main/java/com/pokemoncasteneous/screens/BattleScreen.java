package com.pokemoncasteneous.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.ScreenViewport;
import com.pokemoncasteneous.PokemonGame;

public final class BattleScreen extends ScreenAdapter {
    private final PokemonGame game;
    private final Stage stage = new Stage(new ScreenViewport());
    private final ShapeRenderer scenery = new ShapeRenderer();
    private final Skin skin = new Skin();
    private final Table root = new Table();
    private final Label message;
    private final Label playerHp;
    private final Label enemyHp;
    private final Texture enemySprite;
    private String mode = "main";

    public BattleScreen(PokemonGame game) {
        this.game = game;
        BitmapFont font = new BitmapFont();
        skin.add("default-font", font);
        Pixmap px = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
        px.setColor(new Color(0.09f, 0.14f, 0.18f, 0.94f)); px.fill();
        Texture panel = new Texture(px); px.dispose();
        skin.add("panel", panel, Texture.class);
        Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.WHITE);
        skin.add("default", labelStyle);
        TextButton.TextButtonStyle buttonStyle = new TextButton.TextButtonStyle();
        buttonStyle.font = font;
        buttonStyle.fontColor = Color.WHITE;
        buttonStyle.up = skin.newDrawable("panel", new Color(0.16f, 0.25f, 0.29f, 1f));
        buttonStyle.down = skin.newDrawable("panel", new Color(0.31f, 0.48f, 0.43f, 1f));
        buttonStyle.over = skin.newDrawable("panel", new Color(0.23f, 0.37f, 0.37f, 1f));
        skin.add("default", buttonStyle);
        Pixmap enemy = new Pixmap(100, 82, Pixmap.Format.RGBA8888);
        enemy.setColor(0.43f, 0.70f, 0.43f, 1f); enemy.fillCircle(50, 39, 34);
        enemy.setColor(0.87f, 0.78f, 0.46f, 1f); enemy.fillCircle(31, 65, 13); enemy.fillCircle(69, 65, 13);
        enemy.setColor(0.12f, 0.20f, 0.18f, 1f); enemy.fillCircle(39, 44, 4); enemy.fillCircle(61, 44, 4);
        enemySprite = new Texture(enemy); enemy.dispose();
        message = new Label(game.state().message, labelStyle);
        playerHp = new Label("", labelStyle);
        enemyHp = new Label("", labelStyle);
        stage.addActor(root);
        Gdx.input.setInputProcessor(stage);
        rebuildMenu();
    }

    private TextButton button(String text, Runnable action) {
        TextButton button = new TextButton(text, skin);
        button.addListener(new com.badlogic.gdx.scenes.scene2d.utils.ClickListener() {
            @Override public void clicked(com.badlogic.gdx.scenes.scene2d.InputEvent event, float x, float y) { action.run(); }
        });
        return button;
    }

    private void rebuildMenu() {
        root.clearChildren();
        root.setFillParent(true);
        root.pad(24);
        root.add(message).colspan(2).left().expandX().height(42).row();
        root.add(enemyHp).colspan(2).left().height(34).row();
        if (mode.equals("main")) {
            root.add(button("FIGHT", () -> { mode = "fight"; rebuildMenu(); })).width(190).height(54).pad(5);
            root.add(button("BAG", () -> { mode = "bag"; rebuildMenu(); })).width(190).height(54).pad(5).row();
            root.add(button("BENCH", () -> { mode = "bench"; rebuildMenu(); })).width(190).height(54).pad(5);
            root.add(button("RUN", this::runAway)).width(190).height(54).pad(5).row();
        } else if (mode.equals("fight")) {
            root.add(button("LEAF LASH", () -> attack(9))).width(190).height(54).pad(5);
            root.add(button("QUICK PAW", () -> attack(6))).width(190).height(54).pad(5).row();
            root.add(button("BACK", () -> { mode = "main"; rebuildMenu(); })).colspan(2).width(190).height(48).pad(5).row();
        } else if (mode.equals("bag")) {
            root.add(button("POTION  x" + game.state().potions, this::usePotion)).colspan(2).width(390).height(54).pad(5).row();
            root.add(button("BACK", () -> { mode = "main"; rebuildMenu(); })).colspan(2).width(190).height(48).pad(5).row();
        } else {
            root.add(button("EMBERFOX  Lv. 8", () -> switchParty("Emberfox"))).colspan(2).width(390).height(50).pad(4).row();
            root.add(button("MOSSBO  Lv. 6", () -> switchParty("Mossbo"))).colspan(2).width(390).height(50).pad(4).row();
            root.add(button("BACK", () -> { mode = "main"; rebuildMenu(); })).colspan(2).width(190).height(48).pad(5).row();
        }
        root.add(playerHp).colspan(2).left().height(34).padTop(8).row();
        playerHp.setText("EMBERFOX   " + game.state().playerHp + " / " + game.state().playerMaxHp + " HP");
        enemyHp.setText("MOSSLING   " + game.state().enemyHp + " / " + game.state().enemyMaxHp + " HP");
    }

    private void attack(int damage) {
        game.state().enemyHp = Math.max(0, game.state().enemyHp - damage);
        if (game.state().enemyHp == 0) {
            message.setText("Mossling fainted! You won!");
            Gdx.input.setInputProcessor(null);
            Gdx.app.postRunnable(() -> Gdx.input.setInputProcessor(stage));
            mode = "won";
            root.clearChildren();
            root.add(message).expand().center().row();
            root.add(button("BACK TO FERNWOOD", this::returnToOverworld)).width(300).height(58).center();
            return;
        }
        game.state().playerHp = Math.max(0, game.state().playerHp - 5);
        message.setText("Your move dealt " + damage + " damage. Mossling counters for 5!");
        playerHp.setText("EMBERFOX   " + game.state().playerHp + " / " + game.state().playerMaxHp + " HP");
        enemyHp.setText("MOSSLING   " + game.state().enemyHp + " / " + game.state().enemyMaxHp + " HP");
        if (game.state().playerHp == 0) {
            message.setText("Emberfox is tired. Your party recovers!");
            game.state().playerHp = game.state().playerMaxHp;
            mode = "main";
        }
    }

    private void usePotion() {
        if (game.state().potions == 0) message.setText("Your bag is out of potions.");
        else if (game.state().playerHp == game.state().playerMaxHp) message.setText("Your Emberfox is already at full health.");
        else {
            game.state().potions--;
            game.state().playerHp = Math.min(game.state().playerMaxHp, game.state().playerHp + 14);
            message.setText("Potion used. Emberfox recovered 14 HP.");
        }
        mode = "main";
        rebuildMenu();
    }

    private void switchParty(String name) {
        message.setText(name + " is ready to battle! The opponent takes a turn.");
        game.state().playerHp = Math.min(game.state().playerMaxHp, game.state().playerHp + 4);
        game.state().enemyHp = Math.max(0, game.state().enemyHp - 3);
        mode = "main";
        rebuildMenu();
    }

    private void runAway() {
        game.state().message = "You got away safely.";
        game.state().returnToOverworld();
        game.getScreen().dispose();
        game.setScreen(new OverworldScreen(game));
    }

    private void returnToOverworld() {
        game.state().returnToOverworld();
        game.getScreen().dispose();
        game.setScreen(new OverworldScreen(game));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0.35f, 0.54f, 0.49f, 1f);
        SpriteBatch batch = game.batch();
        OrthographicCamera camera = new OrthographicCamera();
        camera.setToOrtho(false, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.update();
        scenery.setProjectionMatrix(camera.combined);
        scenery.begin(ShapeRenderer.ShapeType.Filled);
        scenery.setColor(0.70f, 0.78f, 0.62f, 1f);
        scenery.rect(0, Gdx.graphics.getHeight() * 0.47f, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.53f);
        scenery.setColor(0.38f, 0.59f, 0.46f, 1f);
        scenery.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight() * 0.53f);
        scenery.setColor(0.54f, 0.70f, 0.48f, 1f);
        scenery.ellipse(Gdx.graphics.getWidth() * 0.52f, Gdx.graphics.getHeight() * 0.35f, 320, 96);
        scenery.setColor(0.64f, 0.77f, 0.54f, 1f);
        scenery.ellipse(Gdx.graphics.getWidth() * 0.12f, Gdx.graphics.getHeight() * 0.13f, 380, 105);
        scenery.end();
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.setColor(0.47f, 0.70f, 0.53f, 1f);
        batch.draw(enemySprite, Gdx.graphics.getWidth() * 0.68f, Gdx.graphics.getHeight() * 0.42f, 150, 123);
        batch.setColor(Color.WHITE);
        batch.end();
        playerHp.setText("EMBERFOX   " + game.state().playerHp + " / " + game.state().playerMaxHp + " HP");
        enemyHp.setText("MOSSLING   " + game.state().enemyHp + " / " + game.state().enemyMaxHp + " HP");
        stage.act(delta);
        stage.draw();
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) runAway();
    }

    @Override public void resize(int width, int height) { stage.getViewport().update(width, height, true); }
    @Override public void dispose() { stage.dispose(); skin.dispose(); enemySprite.dispose(); scenery.dispose(); }
}
