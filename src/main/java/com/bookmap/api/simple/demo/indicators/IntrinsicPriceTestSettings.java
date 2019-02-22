package com.bookmap.api.simple.demo.indicators;

import com.bookmap.api.simple.demo.utils.gui.BookmapSettingsPanel;
import velox.api.layer1.data.InstrumentInfo;
import velox.api.layer1.messages.indicators.Layer1ApiUserMessageModifyIndicator.GraphType;
import velox.api.layer1.settings.StrategySettingsVersion;
import velox.api.layer1.simplified.*;
import velox.gui.StrategyPanel;
import velox.gui.colors.ColorsConfigItem;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class IntrinsicPriceTestSettings implements CustomModule, CustomSettingsPanelProvider {

    public static enum UpdateCondition {
        TRADE, BBO, BAR;

        @Override
        public String toString() {
            return "";
        }
    }

    private final static Color defaultLineColor = Color.WHITE;
    private final static LineStyle defaultLineStyle = LineStyle.SOLID;
    private final static int defaultLineWidth = 3;

    @StrategySettingsVersion(currentVersion = 1, compatibleVersions = {})
    public static class Settings {
        public Color lineColor = defaultLineColor;
        public LineStyle lineStyle = defaultLineStyle;
        public int lineWidth = defaultLineWidth;
    }

    private final JLabel labelTr = new JLabel();
    private final JLabel labelAtr = new JLabel();

    protected Indicator indicator;

    protected Settings settings;
    protected Api api;

    @Override
    public void initialize(String alias, InstrumentInfo info, Api api, InitialState initialState) {
        this.api = api;
        settings = api.getSettings(Settings.class);
        indicator = api.registerIndicator("Intrinsic Price Test", GraphType.PRIMARY);
        setVisualProperties(indicator);
    }

    private void setVisualProperties(final Indicator indicator) {
        indicator.setLineStyle(settings.lineStyle);
        indicator.setWidth(settings.lineWidth);
    }

    @Override
    public void stop() {
        api.setSettings(settings);
    }

    @Override
    public StrategyPanel[] getCustomSettingsPanels() {
        StrategyPanel p1 = getStyleSettingsPanel();
        return new StrategyPanel[] { p1 };
    }

    private StrategyPanel getStyleSettingsPanel() {
        BookmapSettingsPanel panel = new BookmapSettingsPanel("Indicators style settings");
        addColorsSettings(panel);
        addLineStyleSettings(panel);
        addLineWidthSettings(panel);
        return panel;
    }

    private void addColorsSettings(final BookmapSettingsPanel panel) {
        panel.addSettingsItem("Buy Trailing Stop color:", createColorsConfigItem());
    }

    private ColorsConfigItem createColorsConfigItem() {
        Consumer<Color> c = new Consumer<Color>() {

            @Override
            public void accept(Color color) {
                settings.lineColor = color;
                indicator.setColor(settings.lineColor);
            }
        };
        Color color = settings.lineColor;
        Color defaultColor = defaultLineColor;
        return new ColorsConfigItem(color, defaultColor, c);
    }

    private void addLineStyleSettings(final BookmapSettingsPanel panel) {
        String[] lineStyles = Stream.of(LineStyle.values()).map(Object::toString).toArray(String[]::new);
        JComboBox<String> c = new JComboBox<>(lineStyles);
        setAlignment(c);
        c.setSelectedItem(settings.lineStyle.toString());
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int idx = c.getSelectedIndex();
                if (idx != settings.lineStyle.ordinal()) {
                    settings.lineStyle = LineStyle.values()[idx];
                    indicator.setLineStyle(settings.lineStyle);
                }
            }
        });
        panel.addSettingsItem("Line type:", c);
    }

    private void addLineWidthSettings(final BookmapSettingsPanel panel) {
        JComboBox<Integer> c = new JComboBox<>(new Integer[] { 1, 2, 3, 4, 5 });
        setAlignment(c);
        c.setSelectedItem(settings.lineWidth);
        c.setEditable(false);
        c.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                int newLineWidth = (int) c.getSelectedItem();
                if (newLineWidth != settings.lineWidth) {
                    settings.lineWidth = newLineWidth;
                    indicator.setWidth(settings.lineWidth);
                }
            }
        });
        panel.addSettingsItem("Line width:", c);
    }

    private void setAlignment(final JComboBox<?> c) {
        ((JLabel)c.getRenderer()).setHorizontalAlignment(JLabel.LEFT);
    }

}
