package com.vision.detection;

import java.util.Optional;

/**
 * HandDetector - Estado de deteccion de mano para el frame actual.
 *
 * Mantiene un flag de presencia y expone acceso seguro a landmarks
 * para evitar operaciones sobre null cuando no hay mano detectada.
 */
public class HandDetector {

	private volatile boolean handDetected;
	private volatile HandLandmarks landmarks;

	/**
	 * Actualiza el estado de deteccion con los landmarks crudos del pipeline.
	 *
	 * @param rawLandmarks array de landmarks recibido desde processFrame().
	 */
	public void update(float[][] rawLandmarks) {
		this.landmarks = HandLandmarks.fromPipeline(rawLandmarks);
		this.handDetected = this.landmarks != null;
	}

	/**
	 * Indica si hay una mano detectada en el frame actual.
	 *
	 * @return true si hay mano; false en caso contrario.
	 */
	public boolean isHandDetected() {
		return handDetected;
	}

	/**
	 * Retorna landmarks de forma null-safe para el frame actual.
	 *
	 * @return Optional con landmarks si hay mano; Optional.empty() si no.
	 */
	public Optional<HandLandmarks> getLandmarks() {
		return Optional.ofNullable(landmarks);
	}
}
