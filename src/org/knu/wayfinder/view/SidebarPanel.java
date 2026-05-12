package org.knu.wayfinder.view;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;

public class SidebarPanel extends JPanel {
    private Graph graph;
    private MainFrame mainFrame;

    private JTextField searchField;
    private DefaultListModel<Location> searchListModel;
    private JList<Location> searchList;
    private JTextArea infoArea;

    private JComboBox<Location> startCombo;
    private JComboBox<Location> endCombo;
    private JButton findRouteBtn;

    public SidebarPanel(Graph graph, MainFrame mainFrame) {
        this.graph = graph;
        this.mainFrame = mainFrame;

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setPreferredSize(new Dimension(250, 0));
        setBorder(new EmptyBorder(10, 10, 10, 10));

        initSearchUI();
        add(Box.createRigidArea(new Dimension(0, 10)));
        initInfoUI();
        add(Box.createRigidArea(new Dimension(0, 10)));
        initRoutingUI();
        add(Box.createVerticalGlue());
    }

    private void initSearchUI() {
        JLabel title = new JLabel("장소 검색:");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);

        searchField = new JTextField();
        searchField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        searchField.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        searchField.addActionListener(e -> executeSearch());
        searchField.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { executeSearch(); }
            public void removeUpdate(DocumentEvent e) { executeSearch(); }
            public void changedUpdate(DocumentEvent e) { executeSearch(); }
        });

        JButton searchBtn = new JButton("검색");
        
        JPanel searchBox = new JPanel();
        searchBox.setLayout(new BoxLayout(searchBox, BoxLayout.X_AXIS));
        searchBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        searchBox.add(searchField);
        searchBox.add(searchBtn);
        add(searchBox);

        searchListModel = new DefaultListModel<>();
        searchList = new JList<>(searchListModel);
        searchList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(searchList);
        scrollPane.setPreferredSize(new Dimension(230, 200));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(scrollPane);

        searchBtn.addActionListener(e -> executeSearch());
        searchList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Location loc = searchList.getSelectedValue();
                if (loc != null) {
                    displayLocationInfo(loc);
                    mainFrame.getMapPanel().panTo(loc.getX(), loc.getY());
                }
            }
        });

        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem setStartItem = new JMenuItem("출발지로 설정");
        JMenuItem setEndItem = new JMenuItem("도착지로 설정");
        popupMenu.add(setStartItem);
        popupMenu.add(setEndItem);
        
        setStartItem.addActionListener(e -> {
            Location loc = searchList.getSelectedValue();
            if (loc != null && startCombo != null) startCombo.setSelectedItem(loc);
        });
        setEndItem.addActionListener(e -> {
            Location loc = searchList.getSelectedValue();
            if (loc != null && endCombo != null) endCombo.setSelectedItem(loc);
        });

        searchList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int index = searchList.locationToIndex(e.getPoint());
                    if (index != -1) {
                        searchList.setSelectedIndex(index);
                        popupMenu.show(searchList, e.getX(), e.getY());
                    }
                }
            }
        });
    }

    private void initInfoUI() {
        JLabel title = new JLabel("장소 정보:");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);

        infoArea = new JTextArea();
        infoArea.setEditable(false);
        infoArea.setLineWrap(true);
        infoArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(infoArea);
        scrollPane.setPreferredSize(new Dimension(230, 150));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        scrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(scrollPane);
    }

    private void initRoutingUI() {
        JLabel title = new JLabel("길찾기 (A* 알고리즘):");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);

        startCombo = new JComboBox<>();
        endCombo = new JComboBox<>();
        startCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        endCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        startCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        endCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Location loc : graph.getNodes().values()) {
            startCombo.addItem(loc);
            endCombo.addItem(loc);
        }

        findRouteBtn = new JButton("길찾기");
        findRouteBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        add(Box.createRigidArea(new Dimension(0, 5)));
        add(new JLabel("출발지:"));
        add(startCombo);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(new JLabel("도착지:"));
        add(endCombo);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(findRouteBtn);

        findRouteBtn.addActionListener(e -> executeRouting());
    }

    private void executeSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        searchListModel.clear();
        for (Location loc : graph.getNodes().values()) {
            if (loc.getName().toLowerCase().contains(keyword) || 
                (loc.getBuilding() != null && loc.getBuilding().toLowerCase().contains(keyword))) {
                searchListModel.addElement(loc);
            }
        }
    }

    public void displayLocationInfo(Location loc) {
        StringBuilder sb = new StringBuilder();
        sb.append("이름: ").append(loc.getName()).append("\n");
        if (loc.getBuilding() != null && !loc.getBuilding().isEmpty()) {
            sb.append("건물명: ").append(loc.getBuilding()).append("\n");
        }
        sb.append("층수: ").append(loc.getFloor()).append("층\n");
        sb.append("구분: ").append(loc.getCategory()).append("\n\n");
        sb.append("설명: ").append(loc.getDescription());
        infoArea.setText(sb.toString());
        infoArea.setCaretPosition(0);
    }

    private void executeRouting() {
        Location start = (Location) startCombo.getSelectedItem();
        Location end = (Location) endCombo.getSelectedItem();

        if (start != null && end != null) {
            if (start.getId() == end.getId()) {
                JOptionPane.showMessageDialog(this, "출발지와 도착지가 같습니다.");
                return;
            }
            // List<Edge> path = graph.findShortestPath(start.getId(), end.getId());
            if (true) {             // 나중에 채우기
                JOptionPane.showMessageDialog(this, "경로를 찾을 수 없습니다.");
            } else {
                // mainFrame.getMapPanel().setPath(path);
                System.out.println("길찾기");
            }
        }
    }
}
