package io.jbnu.ac.kr;


import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class CameraManager {
    private float shakeDuration = 0f;
    private float shakeTimer = 0f;
    private float shakeIntensity = 0f;
    private Vector2 originalPosition = new Vector2();
    public boolean isShakedStarted;
    private Camera camera;

    public CameraManager(Camera camera) {
        this.camera = camera;
        isShakedStarted = false;
    }

    public void startShake(float Duration, float Intensity) {
        if (shakeDuration > 0)
            return; // 이미 흔들린다면 무시
        this.shakeDuration = Duration;
        this.shakeIntensity = Intensity;
        this.shakeTimer = 0f;

        originalPosition.set(camera.position.x, camera.position.y);
        isShakedStarted = true;

    }

    public void updateShake(float delta) {
        if (shakeTimer < shakeDuration) {
            shakeTimer += delta;

            float decayFactor = shakeTimer / shakeDuration;
            float currentIntensity = shakeIntensity * (1f - decayFactor);

            float offsetX = MathUtils.random(-1f, 1f) * currentIntensity;
            float offsetY = MathUtils.random(-1f, 1f) * currentIntensity;

            camera.position.x = originalPosition.x + offsetX;
            camera.position.y = originalPosition.y + offsetY;
        } else if (shakeDuration > 0) {
            camera.position.set(originalPosition, 0);
            shakeDuration = 0f;
            shakeTimer = 0f;
            isShakedStarted = false;
        }

    }
}




