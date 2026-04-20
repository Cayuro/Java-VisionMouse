package com.vision.control;

import com.vision.detection.HandLandmarks;
import com.vision.gesture.EMAFilter;
import java.awt.Robot;
import java.awt.AWTException;
import java.awt.event.InputEvent;
import java.awt.geom.Point2D;

/**
 * MouseController - Maneja la lógica del cursor con consistencia y ejecución real.
 * Implementa suavizado EMA, debounce y ejecución mediante java.awt.Robot.
 */
public class MouseController {
	private double lastCursorX = -1;
	private double lastCursorY = -1;
	private boolean leftClickPerformed;
	private boolean smoothingApplied;

	private final EMAFilter filterX = new EMAFilter(0.2);
	private final EMAFilter filterY = new EMAFilter(0.2);

	private String currentState = "NONE";
	private String pendingGesture = "NONE";
	private int debounceCounter = 0;
	private int debounceThreshold = 3;

	private Robot robot;
	private boolean isDragActive = false;
	private String lastError = null;

	public MouseController() {
		this(createDefaultRobot());
	}

	public MouseController(Robot robot) {
		this.robot = robot;
		if (this.robot == null) {
			this.lastError = "Robot no inicializado";
		}
	}

	private static Robot createDefaultRobot() {
		try {
			Robot r = new Robot();
			r.setAutoDelay(0);
			return r;
		} catch (AWTException | RuntimeException e) {
			return null;
		}
	}

	public String processGesture(String gesture, HandLandmarks landmarks, int screenWidth, int screenHeight) {
		leftClickPerformed = false;
		smoothingApplied = false;

		if (landmarks == null || !landmarks.isValid()) {
			filterX.reset();
			filterY.reset();
			currentState = "NONE";
			pendingGesture = "NONE";
			debounceCounter = 0;
			return "NONE";
		}

		// Lógica de Debounce
		String inputGesture = (gesture == null) ? "NONE" : gesture;
		if (!inputGesture.equals(pendingGesture)) {
			pendingGesture = inputGesture;
			debounceCounter = 1;
		} else {
			debounceCounter++;
		}

		if (debounceCounter >= debounceThreshold || "NONE".equals(currentState)) {
			currentState = inputGesture;
		}

		// Calcular Coordenadas (siempre que el estado no sea NONE)
		if (!"NONE".equals(currentState)) {
			Point2D middleFingerTip = landmarks.getMiddleFingerTip();
			double mirroredX = 1.0 - middleFingerTip.getX();
			
			lastCursorX = filterX.filter(mirroredX * screenWidth);
			lastCursorY = filterY.filter(middleFingerTip.getY() * screenHeight);
			
			smoothingApplied = true;
		}

		if ("CLICK".equals(currentState)) {
			leftClickPerformed = true;
		}

		return currentState;
	}

	/**
	 * Ejecuta la acción física en el sistema operativo usando Robot.
	 */
	public void execute() {
		if (robot == null || lastCursorX < 0) return;

		int x = getLastCursorX();
		int y = getLastCursorY();

		try {
			switch (currentState) {
				case "MOVE":
					if (isDragActive) {
						robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						isDragActive = false;
					}
					robot.mouseMove(x, y);
					break;
					
				case "DRAG":
					if (!isDragActive) {
						robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
						isDragActive = true;
					}
					robot.mouseMove(x, y);
					break;
					
				case "CLICK":
					if (isDragActive) {
						robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
						isDragActive = false;
					}
					robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
					robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
					currentState = "MOVE"; 
					break;
			}
			lastError = null; // Limpiar error si la ejecución fue exitosa
		} catch (Exception e) {
			this.lastError = "Error de ejecución: " + e.getMessage();
		}
	}

	public String getLastError() {
		return lastError;
	}

	public boolean hasError() {
		return lastError != null;
	}

	public void setDebounceThreshold(int threshold) {
		this.debounceThreshold = threshold;
	}

	public int getLastCursorX() {
		return (int) Math.round(lastCursorX);
	}

	public int getLastCursorY() {
		return (int) Math.round(lastCursorY);
	}

	public boolean wasLeftClickPerformed() {
		return leftClickPerformed;
	}

	public boolean wasSmoothingApplied() {
		return smoothingApplied;
	}

	public String getCurrentState() {
		return currentState;
	}
}
