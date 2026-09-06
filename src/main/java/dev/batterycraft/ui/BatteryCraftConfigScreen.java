package dev.batterycraft.ui;

import dev.batterycraft.config.BatteryCraftConfig;
import dev.batterycraft.profile.ManualMode;
import dev.batterycraft.profile.PowerProfile;
import dev.batterycraft.profile.ProfileSettings;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.GridLayout;
import java.io.IOException;
import java.util.EnumMap;
import java.util.Map;
import java.util.Locale;
import java.util.function.Supplier;

public final class BatteryCraftConfigScreen {
    private static JFrame current;

    private BatteryCraftConfigScreen() { }

    public static void open(BatteryCraftConfig config, Runnable afterSave, Supplier<String> sessionSummary,
                            Supplier<String> diagnostics) {
        SwingUtilities.invokeLater(() -> {
            if (current != null) {
                current.toFront();
                return;
            }
            current = build(config, afterSave, sessionSummary, diagnostics);
            current.setVisible(true);
        });
    }

    private static JFrame build(BatteryCraftConfig config, Runnable afterSave, Supplier<String> sessionSummary,
                                Supplier<String> diagnostics) {
        boolean spanish = Locale.getDefault().getLanguage().equalsIgnoreCase("es");
        JFrame frame = new JFrame("BatteryCraft " + dev.batterycraft.BatteryCraftClient.VERSION);
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        frame.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override public void windowClosed(java.awt.event.WindowEvent event) { current = null; }
        });

        JCheckBox enabled = new JCheckBox(text(spanish, "Enable BatteryCraft", "Activar BatteryCraft"), config.enabled());
        JCheckBox notifications = new JCheckBox(text(spanish, "Show notifications", "Mostrar avisos"), config.notifications());
        JCheckBox indicator = new JCheckBox(text(spanish, "Show periodic status message", "Mostrar estado periódicamente"), config.hudIndicator());
        JCheckBox sodium = new JCheckBox(text(spanish, "Sodium integration", "Integración con Sodium"), config.sodiumIntegration());
        JCheckBox thermal = new JCheckBox(text(spanish, "Experimental thermal mode", "Modo térmico experimental"), config.thermalMode());
        JSpinner low = new JSpinner(new SpinnerNumberModel(config.lowThreshold(), 2, 99, 1));
        JSpinner critical = new JSpinner(new SpinnerNumberModel(config.criticalThreshold(), 1, 98, 1));
        JSpinner poll = new JSpinner(new SpinnerNumberModel(config.pollSeconds(), 5, 300, 1));
        JSpinner transitionDelay = new JSpinner(new SpinnerNumberModel(config.transitionDelaySeconds(), 0, 60, 1));
        JSpinner hudInterval = new JSpinner(new SpinnerNumberModel(config.hudIntervalSeconds() % 60 == 0 ? config.hudIntervalSeconds() / 60 : config.hudIntervalSeconds(), 1, Integer.MAX_VALUE, 1));
        JComboBox<String> intervalUnit = new JComboBox<>(new String[]{text(spanish, "Seconds", "Segundos"), text(spanish, "Minutes", "Minutos")});
        intervalUnit.setSelectedIndex(config.hudIntervalSeconds() % 60 == 0 ? 1 : 0);
        JPanel intervalControls = new JPanel(new GridLayout(1, 2, 6, 0));
        intervalControls.add(hudInterval); intervalControls.add(intervalUnit);
        hudInterval.setToolTipText(text(spanish, "For a 15-minute reminder, enter 15 and select Minutes, then Save and apply.",
                "Para un aviso cada 15 minutos, escribe 15 y elige Minutos; después pulsa Guardar y aplicar."));
        JComboBox<ManualMode> mode = new JComboBox<>(ManualMode.values());
        mode.setSelectedItem(config.manualMode());

        JPanel general = new JPanel(new GridLayout(0, 2, 8, 8));
        general.setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
        general.add(enabled); general.add(notifications);
        general.add(indicator); general.add(sodium);
        general.add(thermal); general.add(new JLabel(""));
        general.add(new JLabel(text(spanish, "Low battery (%)", "Batería baja (%)"))); general.add(low);
        general.add(new JLabel(text(spanish, "Critical battery (%)", "Batería crítica (%)"))); general.add(critical);
        general.add(new JLabel(text(spanish, "Battery poll (seconds)", "Revisión de batería (segundos)"))); general.add(poll);
        general.add(new JLabel(text(spanish, "Transition delay (seconds)", "Espera de transición (segundos)"))); general.add(transitionDelay);
        general.add(new JLabel(text(spanish, "Status interval", "Intervalo del estado"))); general.add(intervalControls);
        general.add(new JLabel(text(spanish, "Mode", "Modo"))); general.add(mode);
        general.add(new JLabel(text(spanish, "Shortcuts", "Atajos")));
        general.add(new JLabel(text(spanish, "Change them in Options > Controls > BatteryCraft",
                "Cámbialos en Opciones > Controles > BatteryCraft")));

        Map<PowerProfile, ProfilePanel> panels = new EnumMap<>(PowerProfile.class);
        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab(text(spanish, "General", "General"), general);
        addProfileTab(tabs, panels, config, PowerProfile.BATTERY, text(spanish, "Battery", "Batería"), spanish);
        addProfileTab(tabs, panels, config, PowerProfile.LOW_BATTERY, text(spanish, "Low battery", "Batería baja"), spanish);
        addProfileTab(tabs, panels, config, PowerProfile.CRITICAL_BATTERY, text(spanish, "Critical", "Crítico"), spanish);
        JPanel diagnosticPanel = new JPanel(new BorderLayout());
        JLabel diagnosticLabel = new JLabel(diagnostics.get());
        diagnosticLabel.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));
        diagnosticPanel.add(diagnosticLabel, BorderLayout.NORTH);
        tabs.addTab(text(spanish, "Diagnostics", "Diagnóstico"), diagnosticPanel);

        JLabel summary = new JLabel(sessionSummary.get());
        summary.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
        JButton folder = new JButton(text(spanish, "Open configuration folder", "Abrir carpeta de configuración"));
        folder.addActionListener(event -> {
            try { Desktop.getDesktop().open(config.path().getParent().toFile()); }
            catch (IOException error) { JOptionPane.showMessageDialog(frame, error.getMessage(), "BatteryCraft", JOptionPane.ERROR_MESSAGE); }
        });
        JButton save = new JButton(text(spanish, "Save and apply", "Guardar y aplicar"));
        save.addActionListener(event -> {
            try {
                hudInterval.commitEdit();
                long intervalSeconds = ((Number) hudInterval.getValue()).longValue() * (intervalUnit.getSelectedIndex() == 1 ? 60L : 1L);
                if (intervalSeconds < 5 || intervalSeconds > Integer.MAX_VALUE) {
                    throw new java.text.ParseException(text(spanish, "Choose an interval between 5 seconds and 35,791,394 minutes.", "Elige un intervalo entre 5 segundos y 35.791.394 minutos."), 0);
                }
                for (ProfilePanel panel : panels.values()) panel.commit();
                low.commitEdit(); critical.commitEdit(); poll.commitEdit(); transitionDelay.commitEdit();
                config.enabled(enabled.isSelected());
                config.notifications(notifications.isSelected());
                config.hudIndicator(indicator.isSelected());
                config.sodiumIntegration(sodium.isSelected());
                config.thermalMode(thermal.isSelected());
                config.lowThreshold((Integer) low.getValue());
                config.criticalThreshold((Integer) critical.getValue());
                config.pollSeconds((Integer) poll.getValue());
                config.transitionDelaySeconds((Integer) transitionDelay.getValue());
                config.hudIntervalSeconds((int) intervalSeconds);
                config.manualMode((ManualMode) mode.getSelectedItem());
                panels.forEach((profile, panel) -> config.profile(profile, panel.value()));
                config.save();
                afterSave.run();
                summary.setText(sessionSummary.get());
                diagnosticLabel.setText(diagnostics.get());
                JOptionPane.showMessageDialog(frame, text(spanish, "Configuration saved.", "Configuración guardada."),
                        "BatteryCraft", JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException | java.text.ParseException error) {
                JOptionPane.showMessageDialog(frame, error.getMessage(), "BatteryCraft", JOptionPane.ERROR_MESSAGE);
            }
        });

        JPanel buttons = new JPanel();
        buttons.add(folder);
        buttons.add(save);
        JPanel bottom = new JPanel();
        bottom.setLayout(new BoxLayout(bottom, BoxLayout.Y_AXIS));
        bottom.add(summary);
        bottom.add(buttons);
        frame.add(tabs, BorderLayout.CENTER);
        frame.add(bottom, BorderLayout.SOUTH);
        frame.setSize(720, 590);
        frame.setLocationByPlatform(true);
        return frame;
    }

    private static void addProfileTab(JTabbedPane tabs, Map<PowerProfile, ProfilePanel> panels,
                                      BatteryCraftConfig config, PowerProfile profile, String title, boolean spanish) {
        ProfilePanel panel = new ProfilePanel(config.profile(profile), spanish);
        panels.put(profile, panel);
        tabs.addTab(title, panel);
    }

    private static final class ProfilePanel extends JPanel {
        private final JSpinner fps;
        private final JSpinner render;
        private final JSpinner simulation;
        private final JSpinner particles;
        private final JSpinner biomeBlend;
        private final JSpinner entityDistance;
        private final JCheckBox clouds;
        private final JCheckBox shadows;

        private ProfilePanel(ProfileSettings settings, boolean spanish) {
            super(new GridLayout(0, 2, 8, 8));
            setBorder(BorderFactory.createEmptyBorder(14, 14, 14, 14));
            fps = spinner(settings.maxFps(), 0, 260, 5);
            fps.setToolTipText(text(spanish, "0 keeps Minecraft's original FPS cap; it does not necessarily mean unlimited FPS.",
                    "0 conserva el límite original de Minecraft; no significa necesariamente FPS ilimitados."));
            render = spinner(settings.renderDistance(), 2, 64, 1);
            simulation = spinner(settings.simulationDistance(), 2, 32, 1);
            particles = spinner(settings.particleLevel(), 0, 2, 1);
            biomeBlend = spinner(settings.biomeBlendRadius(), 0, 7, 1);
            entityDistance = new JSpinner(new SpinnerNumberModel(settings.entityDistanceScale(), 0.25, 1.0, 0.05));
            clouds = new JCheckBox(text(spanish, "Enabled", "Activadas"), settings.clouds());
            shadows = new JCheckBox(text(spanish, "Enabled", "Activadas"), settings.entityShadows());
            row(text(spanish, "Maximum FPS on battery", "FPS máximos en batería"), fps);
            row(text(spanish, "Render distance", "Distancia de renderizado"), render);
            row(text(spanish, "Simulation distance", "Distancia de simulación"), simulation);
            row(text(spanish, "Particles (0-2)", "Partículas (0-2)"), particles);
            row(text(spanish, "Clouds", "Nubes"), clouds);
            row(text(spanish, "Entity shadows", "Sombras de entidades"), shadows);
            row(text(spanish, "Biome blend", "Mezcla de biomas"), biomeBlend);
            row(text(spanish, "Entity distance", "Distancia de entidades"), entityDistance);
        }

        private static JSpinner spinner(int value, int min, int max, int step) {
            return new JSpinner(new SpinnerNumberModel(value, min, max, step));
        }

        private void row(String label, java.awt.Component component) { add(new JLabel(label)); add(component); }

        private void commit() throws java.text.ParseException {
            for (JSpinner input : new JSpinner[]{fps, render, simulation, particles, biomeBlend, entityDistance}) input.commitEdit();
        }

        private ProfileSettings value() {
            return new ProfileSettings((Integer) fps.getValue(), (Integer) render.getValue(),
                    (Integer) simulation.getValue(), (Integer) particles.getValue(), clouds.isSelected(),
                    shadows.isSelected(), (Integer) biomeBlend.getValue(), (Double) entityDistance.getValue());
        }
    }

    private static String text(boolean spanish, String english, String translated) {
        return spanish ? translated : english;
    }
}
