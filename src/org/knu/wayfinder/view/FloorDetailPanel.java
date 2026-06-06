package org.knu.wayfinder.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;

public class FloorDetailPanel extends JDialog {
    private JTextPane infoArea;
    private MapPanel indoorMapPanel;
    // private JLabel imageLabel;
    private JPanel buttonContainer;

    // parentFrame: 부모 창(MainFrame), pLoc: 부모 장소, floor: 선택한 층
    public FloorDetailPanel(MainFrame parentFrame, Graph graph, Location pLoc, int floor) {
        super(parentFrame, pLoc.getName() + " " + floor + "층 상세", false);
        setSize(1000, 650);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel mainPanel = new JPanel(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));

        indoorMapPanel = new MapPanel(graph, parentFrame, pLoc, floor);
        indoorMapPanel.setPreferredSize(new Dimension(650, 560));
        mainPanel.add(indoorMapPanel, BorderLayout.CENTER);

        // 왼쪽에 제목이랑 버튼들
        JPanel leftPanel = new JPanel();
        leftPanel.setLayout(new BorderLayout(0, 10));
        leftPanel.setPreferredSize(new Dimension(220, 560));

        infoArea = new JTextPane();
        infoArea.setContentType("text/html");
        infoArea.setEditable(false);
        infoArea.setOpaque(false);
        infoArea.setPreferredSize(new Dimension(220, 60));
        infoArea.setText("<html><body style='font-family:sans-serif;padding:5px;'>"
                + "<h2 style='margin:0 0 10px 0;color:#333;font-size:18px;font-weight:bold;'>"
                + pLoc.getName() + " " + floor + "층</h2>"
                + "</body></html>");
        leftPanel.add(infoArea, BorderLayout.NORTH);

        // 버튼들을 세로로 나열
        buttonContainer = new JPanel();
        buttonContainer.setLayout(new BoxLayout(buttonContainer, BoxLayout.Y_AXIS));
        buttonContainer.setOpaque(false);

        if (pLoc.getChildren() != null) {
            for (Location cLoc : pLoc.getChildren()) {
                if (cLoc.getFloor() == floor
                        && cLoc.getCategory() == org.knu.wayfinder.model.LocationCategory.CLASSROOM) {
                    JButton button = new JButton(cLoc.getName());
                    button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
                    button.setAlignmentX(Component.LEFT_ALIGNMENT);
                    button.addActionListener(e -> {
                        System.out.println(cLoc.getName() + " 클릭됨");
                        indoorMapPanel.panTo(cLoc.getX(), cLoc.getY());
                    });
                    buttonContainer.add(button);
                    buttonContainer.add(Box.createVerticalStrut(5));
                }
            }
        }

        JScrollPane buttonScroll = new JScrollPane(buttonContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        buttonScroll.setBorder(null);
        buttonScroll.getVerticalScrollBar().setUnitIncrement(24);
        // routesScrollPane.getVerticalScrollBar().setUnitIncrement(24);
        leftPanel.add(buttonScroll, BorderLayout.CENTER);

        mainPanel.add(leftPanel, BorderLayout.WEST);

        add(mainPanel);
        setLocationRelativeTo(parentFrame);

        parentFrame.registerFloorPanel(this);
    }


    public MapPanel getMapPanel() {
        return indoorMapPanel;
    }


}
