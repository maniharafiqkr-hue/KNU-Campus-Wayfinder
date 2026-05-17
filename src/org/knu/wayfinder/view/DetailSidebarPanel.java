package org.knu.wayfinder.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.knu.wayfinder.model.Graph;

public class DetailSidebarPanel extends JPanel{
    private Graph graph;
    private MainFrame mainFrame;

    private JTextArea infoArea;

    Image img = new ImageIcon(getClass().getResource("/org/knu/wayfinder/data/img/sample.png")).getImage();

    public DetailSidebarPanel(Graph graph, MainFrame mainFrame) {
        this.graph = graph;
        this.mainFrame = mainFrame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(450, 0));
        setBorder(new EmptyBorder(10, 10, 10, 10));
        
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(img, 0, 0, 450, 450, this);
    }

    
}
