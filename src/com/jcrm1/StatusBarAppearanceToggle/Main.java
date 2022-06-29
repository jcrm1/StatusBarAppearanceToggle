package com.jcrm1.StatusBarAppearanceToggle;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.imageio.ImageIO;

public class Main {
	private static TrayIcon trayIcon = null;
	private static Image darkImage = null;
	private static Image lightImage = null;
	private static Mode mode = Mode.DARK;
	public static void main(String[] args){
		System.setProperty("apple.awt.UIElement", "true");
		if (darkImage == null) {
			try {
				darkImage = ImageIO.read(Main.class.getResourceAsStream("/google_dark.png"));
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		if (lightImage == null) {
			try {
				lightImage = ImageIO.read(Main.class.getResourceAsStream("/google_light.png"));
			} catch (Exception e) {
				System.out.println(e);
			}
		}
		if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
			System.out.println("OS (" + System.getProperty("os.name") + ") not supported (macOS/OSX only)");
			System.exit(0);
		}
	    if (SystemTray.isSupported()) {
	        SystemTray tray = SystemTray.getSystemTray();
	        ActionListener darkListener = new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	setMode(Mode.DARK);
	            }
	        };
	        ActionListener lightListener = new ActionListener() {
	            public void actionPerformed(ActionEvent e) {
	            	setMode(Mode.LIGHT);
	            }
	        };
	        PopupMenu popup = new PopupMenu();
	        MenuItem lightMode = new MenuItem("Light Mode");
	        lightMode.addActionListener(lightListener);
	        MenuItem darkMode = new MenuItem("Dark Mode");
	        darkMode.addActionListener(darkListener);
	        trayIcon = new TrayIcon(darkImage, "System Appearance", popup);
	        trayIcon.addMouseListener(new MouseAdapter() {
	            public void mouseClicked(MouseEvent e) {
	                if (e.getClickCount() == 1) {
	                	if (e.getButton() == MouseEvent.BUTTON3) {
	                		System.exit(0);
	                	} else {
		                	if (mode == Mode.DARK) { 
		                		setMode(Mode.LIGHT);
		                	} else if (mode == Mode.LIGHT) { 
		                		setMode(Mode.DARK);
		                	}
	                	}
	                }
	            }
	        });
	        setMode(getMode());
	        try {
	            tray.add(trayIcon);
	        } catch (AWTException e) {
	            System.err.println(e);
	        }
	    } else {
	    	System.out.println("OS (" + System.getProperty("os.name") + ") not supported (macOS/OSX only)");
			System.exit(0);
	    }
	}
	private static void setMode(Mode m) {
		if (m == Mode.DARK) { 
			trayIcon.setImage(darkImage);
			mode = Mode.DARK;
			try {
				Runtime runtime = Runtime.getRuntime();
				String applescriptCommand =  "tell application \"System Events\"\n"
						+ "tell appearance preferences\n"
						+ "set dark mode to true\n"
						+ "end tell\n"
						+ "end tell";
				String[] args = { "osascript", "-e", applescriptCommand };
				runtime.exec(args);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else if (m == Mode.LIGHT) { 
			trayIcon.setImage(lightImage);
			mode = Mode.LIGHT;
			try {
				Runtime runtime = Runtime.getRuntime();
				String applescriptCommand =  "tell application \"System Events\"\n"
						+ "tell appearance preferences\n"
						+ "set dark mode to false\n"
						+ "end tell\n"
						+ "end tell";
				String[] args = { "osascript", "-e", applescriptCommand };
				runtime.exec(args);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	private static Mode getMode() {
		try {
			Runtime runtime = Runtime.getRuntime();
			String applescriptCommand =  "tell application \"System Events\"\n"
					+ "tell appearance preferences\n"
					+ "if dark mode then\n"
					+ "return \"dark\"\n"
					+ "else\n"
					+ "return \"light\"\n"
					+ "end if\n"
					+ "end tell\n"
					+ "end tell";
			String[] args = { "osascript", "-e", applescriptCommand };
			Process process = runtime.exec(args);
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(process.getInputStream()));
			String output = stdInput.readLine();
			if (output.contains("light")) {
				return Mode.LIGHT;
			} else if (output.contains("dark")) {
				return Mode.DARK;
			}
		} catch (IOException e) {
			System.out.println("Unable to get system appearance (is osascript installed to /usr/bin/osascript ?)");
			return null;
		}
		return null;
	}
}
