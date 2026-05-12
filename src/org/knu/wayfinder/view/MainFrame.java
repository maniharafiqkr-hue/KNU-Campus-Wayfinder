package org.knu.wayfinder.view;

import java.awt.*;
import javax.swing.*;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;

public class MainFrame extends JFrame {
    private Graph graph;
    private SidebarPanel sidebarPanel;
    private MapPanel mapPanel;

    public MainFrame(Graph graph) {
        this.graph = graph;
        setTitle("경북대학교 데모 맵");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        setLayout(new BorderLayout());

        sidebarPanel = new SidebarPanel(graph, this);
        mapPanel = new MapPanel(graph, this);

        add(sidebarPanel, BorderLayout.WEST);
        add(mapPanel, BorderLayout.CENTER);
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public SidebarPanel getSidebarPanel() {
        return sidebarPanel;
    }

    public void onLocationSelectedFromMap(Location loc) {
        sidebarPanel.displayLocationInfo(loc);
    }
}
