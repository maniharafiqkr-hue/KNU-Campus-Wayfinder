package org.knu.wayfinder.view;

import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import javax.swing.*;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;

public class MainFrame extends JFrame {
    private Graph graph;
    private SidebarPanel sidebarPanel;
    private DetailSidebarPanel detailSidebarPanel;
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
        detailSidebarPanel = new DetailSidebarPanel(graph, null);

        detailSidebarPanel.setVisible(false);

        add(sidebarPanel, BorderLayout.WEST);
        add(mapPanel, BorderLayout.CENTER);
        add(detailSidebarPanel, BorderLayout.EAST);

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                // 기준 너비 설정 1150
                int threshold = 1150;
                
                // 창 너비가 기준보다 크거나, 전체화면 상태일 때 표시
                boolean shouldShow = getWidth() >= threshold || getExtendedState() == JFrame.MAXIMIZED_BOTH;
                
                detailSidebarPanel.setVisible(shouldShow);
                
                revalidate();
                repaint();
            }
        });
    }

    public MapPanel getMapPanel() {
        return mapPanel;
    }

    public SidebarPanel getSidebarPanel() {
        return sidebarPanel;
    }

    public DetailSidebarPanel getDetailSidebarPanel() {
        return detailSidebarPanel;
    }

    public void onLocationSelectedFromMap(Location loc) {
        sidebarPanel.displayLocationInfo(loc);
    }

    
}
