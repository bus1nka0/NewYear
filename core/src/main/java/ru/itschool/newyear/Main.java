package ru.itschool.newyear;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;

public class Main extends ApplicationAdapter {
    private SpriteBatch sb;
    private Texture bg;
    private TextureRegion bgRegion;
    private Texture closeIcon;
    private Texture[] flakes;
    private Array<Rectangle>[] flakeLists;
    private Music[] songs;
    private int currentSong = 0;
    private OrthographicCamera cam;
    private Rectangle closeBounds;
    private ShapeRenderer sr;
    private Rectangle volumeBar;
    private Rectangle volumeKnob;
    private float volume = 0.25f; // Значение по умолчанию 25%
    private BitmapFont font;

    private static final int screenWidth = 1920;
    private static final int screenHeight = 1080;
    private static final int iconSize = 100;
    private static final int padding = 20;
    private static final int flakeCount = 40;
    private static final int flakeSize = 128;
    private static final int volumeBarWidth = 200;
    private static final int volumeBarHeight = 20;

    @Override
    public void create() {
        Gdx.graphics.setFullscreenMode(Gdx.graphics.getDisplayMode());
        Gdx.graphics.setVSync(true);

        cam = new OrthographicCamera();
        cam.setToOrtho(false, screenWidth, screenHeight);
        sb = new SpriteBatch();
        sr = new ShapeRenderer();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2);

        bg = new Texture("backgroundNewYear.png");
        bgRegion = new TextureRegion(bg);
        closeIcon = new Texture("closeIcon.png");
        flakes = new Texture[3];
        flakes[0] = new Texture("snowflake1.png");
        flakes[1] = new Texture("snowflake2.png");
        flakes[2] = new Texture("snowflake3.png");

        songs = new Music[3];
        songs[0] = Gdx.audio.newMusic(Gdx.files.internal("newYearMusic1.mp3"));
        songs[1] = Gdx.audio.newMusic(Gdx.files.internal("newYearMusic2.mp3"));
        songs[2] = Gdx.audio.newMusic(Gdx.files.internal("newYearMusic3.mp3"));

        songs[currentSong].play();
        songs[currentSong].setOnCompletionListener(music -> playNextSong());

        closeBounds = new Rectangle(padding, screenHeight - iconSize - padding, iconSize, iconSize);

        flakeLists = new Array[3];
        for (int i = 0; i < 3; i++) {
            flakeLists[i] = new Array<>();
            for (int j = 0; j < flakeCount; j++) {
                Rectangle flake = new Rectangle();
                flake.x = MathUtils.random(0, screenWidth - flakeSize);
                flake.y = MathUtils.random(screenHeight, screenHeight * 2);
                flake.width = flakeSize;
                flake.height = flakeSize;
                flakeLists[i].add(flake);
            }
        }

        volumeBar = new Rectangle(screenWidth - volumeBarWidth - padding, padding, volumeBarWidth, volumeBarHeight);
        volumeKnob = new Rectangle(volumeBar.x + volumeBarWidth * volume, volumeBar.y, volumeBarHeight, volumeBarHeight);
    }

    private void playNextSong() {
        songs[currentSong].stop();
        currentSong = (currentSong + 1) % songs.length;
        songs[currentSong].play();
        songs[currentSong].setOnCompletionListener(music -> playNextSong());
    }

    @Override
    public void render() {
        ScreenUtils.clear(0, 0, 0, 0);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        cam.update();
        sb.setProjectionMatrix(cam.combined);
        sr.setProjectionMatrix(cam.combined);

        sb.begin();
        sb.draw(bgRegion, 0, -30, screenWidth, screenHeight + 30);
        sb.draw(closeIcon, closeBounds.x, closeBounds.y, closeBounds.width, closeBounds.height);

        for (int i = 0; i < 3; i++) {
            for (Rectangle flake : flakeLists[i]) {
                sb.draw(flakes[i], flake.x, flake.y, flake.width, flake.height);
            }
        }
        sb.end();

        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(Color.DARK_GRAY);
        sr.rect(volumeBar.x, volumeBar.y, volumeBar.width, volumeBar.height);
        sr.setColor(Color.LIGHT_GRAY);
        sr.rect(volumeKnob.x, volumeKnob.y, volumeKnob.width, volumeKnob.height);
        sr.end();

        sb.begin();
        String volumePercent = String.valueOf(Math.round(volume * 100)) + "%";
        font.draw(sb, volumePercent, volumeBar.x + volumeBarWidth / 2f - font.getCapHeight(), volumeBar.y + volumeBarHeight + font.getCapHeight());
        sb.end();

        for (Music song : songs) {
            song.setVolume(volume);
        }

        if (Gdx.input.isTouched()) {
            Vector3 touchPos = new Vector3();
            touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            cam.unproject(touchPos);
            Vector2 touch = new Vector2(touchPos.x, touchPos.y);

            if (closeBounds.contains(touchPos.x, touchPos.y)) {
                Gdx.app.exit();
            }
            if (volumeBar.contains(touch)) {
                volumeKnob.x = touch.x - volumeKnob.width / 2f;
                volumeKnob.x = MathUtils.clamp(volumeKnob.x, volumeBar.x, volumeBar.x + volumeBar.width - volumeKnob.width);
                volume = (volumeKnob.x - volumeBar.x) / (volumeBar.width - volumeKnob.width);
            }
        }

        for (int i = 0; i < 3; i++) {
            for (Rectangle flake : flakeLists[i]) {
                flake.y -= 200 * Gdx.graphics.getDeltaTime();
                if (flake.y + flake.height < 0) {
                    flake.x = MathUtils.random(0, screenWidth - flakeSize);
                    flake.y = screenHeight + flake.height;
                }
            }
        }
    }

    @Override
    public void dispose() {
        sb.dispose();
        bgRegion.getTexture().dispose();
        closeIcon.dispose();
        for (Texture flake : flakes) {
            flake.dispose();
        }
        for (Music song : songs) {
            song.dispose();
        }
        sr.dispose();
        font.dispose();
    }
}
