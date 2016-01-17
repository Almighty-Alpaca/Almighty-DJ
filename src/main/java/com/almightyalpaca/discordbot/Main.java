package com.almightyalpaca.discordbot;

import java.awt.AWTException;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowStateListener;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.lang3.time.StopWatch;

import net.dv8tion.jda.JDA;

public class Main {
	
	private JFrame	frmAlmightyDJ;
				
	public JDA	api;
				
	StopWatch		watch;
				
	Image		trayImage	= ImageIO.read(ClassLoader.getSystemResource("trayicon.png"));
						
	TrayIcon		trayIcon	= new TrayIcon(this.trayImage);
	SystemTray	tray;
				
	public Main(final String[] args) throws Exception {
		this.watch = new StopWatch();
		this.watch.start();
		this.initialize(args);
	}
	
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			
			@Override
			public void run() {
				try {
					new Main(args);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
		
	}
	
	/**
	 * @param args
	 * @wbp.parser.entryPoint
	 */
	private void initialize(final String[] args) throws Exception {
		
		this.frmAlmightyDJ = new JFrame("Almighty DJ");
		this.frmAlmightyDJ.setTitle("Almighty DJ");
		this.frmAlmightyDJ.setPreferredSize(new Dimension(450, 300));
		this.frmAlmightyDJ.setMinimumSize(new Dimension(450, 300));
		this.frmAlmightyDJ.setMaximumSize(new Dimension(450, 300));
		this.frmAlmightyDJ.setSize(new Dimension(450, 300));
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 30, 30, 30, 30, 30, 30, 30, 30 };
		gridBagLayout.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		this.frmAlmightyDJ.getContentPane().setLayout(gridBagLayout);
		
		final JButton trayButton = new JButton("Minimize to Tray");
		trayButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				Main.this.frmAlmightyDJ.setExtendedState(Frame.ICONIFIED);
			}
		});
		final GridBagConstraints gbc_trayButton = new GridBagConstraints();
		gbc_trayButton.gridheight = 3;
		gbc_trayButton.insets = new Insets(0, 0, 5, 0);
		gbc_trayButton.gridx = 0;
		gbc_trayButton.gridy = 1;
		this.frmAlmightyDJ.getContentPane().add(trayButton, gbc_trayButton);
		
		final JButton restartButton = new JButton("Restart");
		restartButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				try {
					Main.this.restartApplication();
				} catch (URISyntaxException | IOException e1) {
					e1.printStackTrace();
				} finally {
					JOptionPane.showMessageDialog(Main.this.frmAlmightyDJ, "An error occured while trying to restart the application! Application will now exit.", "ERROR", JOptionPane.ERROR_MESSAGE, null);
					System.exit(0);
				}
			}
		});
		final GridBagConstraints gbc_restartButton = new GridBagConstraints();
		gbc_restartButton.gridheight = 3;
		gbc_restartButton.insets = new Insets(0, 0, 5, 0);
		gbc_restartButton.gridx = 0;
		gbc_restartButton.gridy = 4;
		this.frmAlmightyDJ.getContentPane().add(restartButton, gbc_restartButton);
		
		final JButton stopButton = new JButton("Stop");
		stopButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(final ActionEvent e) {
				System.exit(0);
			}
		});
		final GridBagConstraints gbc_stopButton = new GridBagConstraints();
		gbc_stopButton.gridheight = 2;
		gbc_stopButton.insets = new Insets(0, 0, 5, 0);
		gbc_stopButton.gridx = 0;
		gbc_stopButton.gridy = 7;
		this.frmAlmightyDJ.getContentPane().add(stopButton, gbc_stopButton);
		if (SystemTray.isSupported()) {
			System.out.println("system tray supported");
			this.tray = SystemTray.getSystemTray();
			
			final ActionListener exitListener = new ActionListener() {
				
				@Override
				public void actionPerformed(final ActionEvent e) {
					System.out.println("Exiting....");
					System.exit(0);
				}
			};
			final PopupMenu popup = new PopupMenu();
			MenuItem defaultItem = new MenuItem("Exit");
			defaultItem.addActionListener(exitListener);
			popup.add(defaultItem);
			defaultItem = new MenuItem("Open");
			defaultItem.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(final ActionEvent e) {
					Main.this.frmAlmightyDJ.setVisible(true);
					Main.this.frmAlmightyDJ.setExtendedState(Frame.NORMAL);
				}
			});
			popup.add(defaultItem);
			this.trayIcon = new TrayIcon(this.trayImage, "Almighty DJ", popup);
			this.trayIcon.setImageAutoSize(true);
		} else {
			System.out.println("system tray not supported");
		}
		this.frmAlmightyDJ.addWindowStateListener(new WindowStateListener() {
			
			@Override
			public void windowStateChanged(final WindowEvent e) {
				if (e.getNewState() == Frame.ICONIFIED) {
					try {
						Main.this.tray.add(Main.this.trayIcon);
						Main.this.frmAlmightyDJ.setVisible(false);
						System.out.println("added to SystemTray");
					} catch (final AWTException ex) {
						System.out.println("unable to add to tray");
					}
				}
				if (e.getNewState() == 7) {
					try {
						Main.this.tray.add(Main.this.trayIcon);
						Main.this.frmAlmightyDJ.setVisible(false);
						System.out.println("added to SystemTray");
					} catch (final AWTException ex) {
						System.out.println("unable to add to system tray");
					}
				}
				if (e.getNewState() == Frame.MAXIMIZED_BOTH) {
					Main.this.tray.remove(Main.this.trayIcon);
					Main.this.frmAlmightyDJ.setVisible(true);
					System.out.println("Tray icon removed");
				}
				if (e.getNewState() == Frame.NORMAL) {
					Main.this.tray.remove(Main.this.trayIcon);
					Main.this.frmAlmightyDJ.setVisible(true);
					System.out.println("Tray icon removed");
				}
			}
		});
		
		this.frmAlmightyDJ.setIconImage(ImageIO.read(ClassLoader.getSystemResource("trayicon.png")));
		
		// Initialize Bot
		BotMain.main(args);
		
		frmAlmightyDJ.setVisible(true);
		
	}
	
	public void restartApplication() throws URISyntaxException, IOException {
		final String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
		final File currentJar = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		
		/* is it a jar file? */
		if (!currentJar.getName().endsWith(".jar")) {
			return;
		}
		
		/* Build command: java -jar application.jar */
		final ArrayList<String> command = new ArrayList<String>();
		command.add(javaBin);
		command.add("-jar");
		command.add(currentJar.getPath());
		
		final ProcessBuilder builder = new ProcessBuilder(command);
		builder.start();
		System.exit(0);
	}
	
}
