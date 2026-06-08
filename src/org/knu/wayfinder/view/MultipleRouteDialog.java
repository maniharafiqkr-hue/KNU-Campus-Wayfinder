package org.knu.wayfinder.view;

import org.knu.wayfinder.model.Location;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class MultipleRouteDialog extends JDialog {

    private List<Location> multipleLocList;
    private JPanel listPanel;
    private JScrollPane scrollPane;

    public MultipleRouteDialog(MainFrame mainFrame, List<Location> multipleLocList) {
        super(mainFrame, "다중 길찾기", false);
        this.multipleLocList = multipleLocList;

        setLayout(new BorderLayout());
        setSize(300, 400);
        setLocationRelativeTo(mainFrame);
        setResizable(false);

        // 상단 타이틀
        JLabel title = new JLabel("경유지 목록");
        title.setBorder(new EmptyBorder(10, 10, 5, 10));
        title.setFont(title.getFont().deriveFont(Font.BOLD, 14f));
        add(title, BorderLayout.NORTH);

        // 중앙 리스트 패널
        listPanel = new JPanel();
        listPanel.setLayout(new BoxLayout(listPanel, BoxLayout.Y_AXIS));
        listPanel.setBorder(new EmptyBorder(5, 10, 5, 10));

        scrollPane = new JScrollPane(listPanel,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.add(closeBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        refreshList();
    }

    public void refreshList() {
        listPanel.removeAll();

        if (multipleLocList.isEmpty()) {
            JLabel emptyLabel = new JLabel("경유지가 없습니다.");
            emptyLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
            emptyLabel.setForeground(Color.GRAY);
            listPanel.add(emptyLabel);
        } else {
            for (int i = 0; i < multipleLocList.size(); i++) {
                Location loc = multipleLocList.get(i);
                listPanel.add(makeRow(i, loc));
                listPanel.add(Box.createVerticalStrut(5));
            }
        }

        listPanel.add(Box.createVerticalGlue());
        listPanel.revalidate();
        listPanel.repaint();
    }

    private JPanel makeRow(int index, Location loc) {
        JPanel row = new JPanel();
        row.setLayout(new BoxLayout(row, BoxLayout.X_AXIS));
        row.setAlignmentX(Component.LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));

        // 순서 번호
        JLabel indexLabel = new JLabel((index + 1) + ". ");
        indexLabel.setPreferredSize(new Dimension(25, 20));

        // 장소 이름
        JLabel nameLabel = new JLabel(loc.toString());
        nameLabel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));

        // 삭제 버튼
        JButton deleteBtn = new JButton("✕"); // 이 엒스가 좀 멋짐
        deleteBtn.setMargin(new Insets(0, 5, 0, 5));
        deleteBtn.setForeground(Color.RED);
        deleteBtn.addActionListener(e -> {
            multipleLocList.remove(index);
            refreshList();
        });

        row.add(indexLabel);
        row.add(nameLabel);
        row.add(Box.createHorizontalGlue());
        row.add(deleteBtn);

        return row;
    }
}