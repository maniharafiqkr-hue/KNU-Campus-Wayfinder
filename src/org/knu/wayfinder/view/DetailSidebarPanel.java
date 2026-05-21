package org.knu.wayfinder.view;

import java.awt.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;

public class DetailSidebarPanel extends JPanel {
    private Graph graph;
    private MainFrame mainFrame;
    private JTextPane infoArea;
    private JLabel imageLabel;

    public DetailSidebarPanel(Graph graph, MainFrame mainFrame) {
        this.graph = graph;
        this.mainFrame = mainFrame;

        // 레이아웃 및 여백 설정
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        // ImageIcon originalIcon = new ImageIcon(getClass().getResource("/org/knu/wayfinder/data/img/sample.png"));
        // Image scaledImage = originalIcon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
        // JLabel imageLabel = new JLabel(new ImageIcon(scaledImage));
        imageLabel = new JLabel();
        imageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        add(imageLabel);

        // 여백 추가
        add(Box.createVerticalStrut(15));

        // infoArea = new JTextArea(20, 30); // rows, columns
        // infoArea.setText("여기에 상세 정보가 들어갑니다...\n".repeat(60)); // 스크롤 확인용 긴 텍스트
        // infoArea.setLineWrap(true);
        // infoArea.setEditable(false);

        infoArea = new JTextPane();
        infoArea.setContentType("text/html"); // HTML 형식 사용 선언
        infoArea.setEditable(false);
        infoArea.setOpaque(false); // 배경을 투명하게 하여 패널과 색상 통일
        
        
        add(infoArea);

    }
    
    public void updateLocationDetail(Location loc) {
        if (loc == null) return;

        String htmlContent = "<html>"
                + "<body style='font-family: sans-serif; padding: 5px;'>"
                + "  <h2 style='margin: 0 0 10px 0; color: #333333; font-size: 20px; font-weight: bold;'>" 
                +      loc.getName() 
                + "  </h2>"
                + "  <hr style='border: 0; height: 1px; background: #dddddd; margin-bottom: 15px;'>"
                + "  <p style='margin: 0; color: #666666; font-size: 14px; line-height: 1.6;'>" 
                +      loc.getDescription().replace("\n", "<br>") // 줄바꿈 적용
                + "  </p>"
                + "</body>"
                + "</html>";
        
        infoArea.setText(htmlContent);
        infoArea.setCaretPosition(0); // 스크롤 맨 위로 리셋

        // 이미지 변경
        try {
            // ImageIcon originalIcon = new ImageIcon(getClass().getResource(loc.getImagePath()));
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/org/knu/wayfinder/data/img/" + loc.getId() + ".png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        } catch (Exception e) {
            // 이미지가 없을 때의 예외 처리 (기본 샘플 이미지 띄우기)
            ImageIcon originalIcon = new ImageIcon(getClass().getResource("/org/knu/wayfinder/data/img/sample.png"));
            Image scaledImage = originalIcon.getImage().getScaledInstance(400, 400, Image.SCALE_SMOOTH);
            imageLabel.setIcon(new ImageIcon(scaledImage));
        }

        revalidate();
        repaint();
    }
}
