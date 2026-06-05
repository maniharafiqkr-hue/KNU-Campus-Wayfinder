package org.knu.wayfinder.view;


import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;
import org.knu.wayfinder.model.LocationCategory;
import org.knu.wayfinder.service.AStarService;
import org.knu.wayfinder.service.EmptyPathException;

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
    private JButton multipleFindRouteBtn;

    private JScrollPane routesScrollPane; 
    private JPanel routesContainer;
    private List<Location> routesList;
    private int nowRouteIndex = 0;
    private JButton nextButton;

    private List<Location> multipleLocList = new ArrayList<>();
    private MultipleRouteDialog multipleRouteDialog;

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
        add(Box.createRigidArea(new Dimension(0, 20)));
        // add(Box.createVerticalGlue());
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
        scrollPane.setPreferredSize(new Dimension(230, 150));
        scrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
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
        JMenuItem addRouteItem = new JMenuItem("경유지로 추가");
        popupMenu.add(setStartItem);
        popupMenu.add(setEndItem);
        popupMenu.add(addRouteItem);
        
        setStartItem.addActionListener(e -> {
            Location loc = searchList.getSelectedValue();
            if (loc != null && startCombo != null) startCombo.setSelectedItem(loc);
        });
        setEndItem.addActionListener(e -> {
            Location loc = searchList.getSelectedValue();
            if (loc != null && endCombo != null) endCombo.setSelectedItem(loc);
        });
        addRouteItem.addActionListener(e -> {
            mainFrame.getSidebarPanel().addMultipleLocList(searchList.getSelectedValue());
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
        JLabel title = new JLabel("길찾기");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        add(title);

        startCombo = new JComboBox<>();
        endCombo = new JComboBox<>();
        startCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        endCombo.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        startCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
        endCombo.setAlignmentX(Component.LEFT_ALIGNMENT);

        for (Location loc : graph.getNodes().values()) {
            if (LocationCategory.OUTDOOR == loc.getCategory()) continue; // Hide waypoints
            else if (LocationCategory.HALLWAY == loc.getCategory()) continue;
            else if (LocationCategory.STAIRS == loc.getCategory()) continue; 
            else if (LocationCategory.ENTRANCE == loc.getCategory()) continue; 
            else if (loc.getFloor() != 0) continue;
            startCombo.addItem(loc);
            endCombo.addItem(loc);
        }

        findRouteBtn = new JButton("길찾기");
        multipleFindRouteBtn = new JButton("경유지 리스트");
        // findRouteBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new BoxLayout(btnPanel, BoxLayout.X_AXIS));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.add(findRouteBtn);
        btnPanel.add(Box.createRigidArea(new Dimension(5, 0)));
        btnPanel.add(multipleFindRouteBtn);

        add(Box.createRigidArea(new Dimension(0, 5)));
        add(new JLabel("출발지:"));
        add(startCombo);
        add(Box.createRigidArea(new Dimension(0, 5)));
        add(new JLabel("도착지:"));
        add(endCombo);
        add(Box.createRigidArea(new Dimension(0, 10)));
        add(btnPanel);

        findRouteBtn.addActionListener(e -> executeRouting());
        multipleFindRouteBtn.addActionListener(e -> openMultipleRouteDialog());
    }

    private void makeRouteList() {
        List<Location> currentPath = mainFrame.getMapPanel().getCurrentPath();
        List<Location> routes = new ArrayList<>();

        Location first = currentPath.getFirst();
        if(first.getCategory() != LocationCategory.BUILDING)
            routes.add(first);

        Location temp = first;

        for(Location loc : currentPath) {
            if(temp != null){
                // 이 로직이 버그가 없을까?
                if(loc.getCategory() == LocationCategory.BUILDING)
                    routes.add(loc);
                else if(temp.getCategory() == LocationCategory.STAIRS && loc.getCategory() != LocationCategory.STAIRS)
                    routes.add(temp);
                else if(temp.getCategory() == LocationCategory.ELEVATOR && loc.getCategory() != LocationCategory.ELEVATOR)
                    routes.add(temp);
                else if(loc.getCategory() == LocationCategory.ENTRANCE && temp.getCategory() == LocationCategory.BUILDING)
                    routes.add(loc);
            }

            temp = loc;
        }

        // Location last = currentPath.getLast();
        // if(last.getCategory() != LocationCategory.BUILDING)
        //     routes.add(last);

        this.routesList = routes; 

    }

    

    private void makeRouteUI() {

        // 초기화
        if (routesScrollPane != null) remove(routesScrollPane);
        if(nextButton != null) remove(nextButton);
        nowRouteIndex = 0;
        
        // 창 다 닫기
        for(FloorDetailPanel panel : mainFrame.getOpenFloorPanels())
            panel.dispose();

        makeRouteList();

        routesContainer = new JPanel();
        routesContainer.setLayout(new BoxLayout(routesContainer, BoxLayout.Y_AXIS));
        routesContainer.setOpaque(false);
        routesContainer.setAlignmentX(Component.LEFT_ALIGNMENT);
        routesContainer.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200));


        nextButton = new JButton("다음 장소");
        nextButton.setMaximumSize(new Dimension(100, 35));
        nextButton.setBackground(Color.white);
        nextButton.setAlignmentX(Component.LEFT_ALIGNMENT);
        nextButton.addActionListener(e -> {
            
            for(FloorDetailPanel panel : mainFrame.getOpenFloorPanels())
                panel.dispose();

            Location nowLoc = routesList.get(nowRouteIndex);
            if (nowLoc.getParentBuilding() != null) {
                FloorDetailPanel dialog = new FloorDetailPanel(mainFrame, graph, nowLoc.getParentBuilding(), nowLoc.getFloor());
                dialog.setVisible(true);
            } else {
                mainFrame.getMapPanel().panTo(nowLoc.getX(), nowLoc.getY());
            }

            int counter = 0;
            for(Component com : routesContainer.getComponents()) {
                if(com instanceof JButton) {
                    if(counter == nowRouteIndex) com.setBackground(Color.CYAN);
                    else com.setBackground(Color.WHITE);
                    counter++;
                }
            } 
            
            nowRouteIndex = (nowRouteIndex + 1) % routesList.size();
        });
        add(nextButton);
        

        JButton button;
        routesContainer.add(Box.createVerticalStrut(5));
        // for (Location loc :  routesList) {
        for(int i=0; i<routesList.size(); i++){

            // 개어려운 람다 뭐시기 때문에 final로 해야됨
            final Location loc = routesList.get(i);
            final int index = i;

            if (loc.getParentBuilding() != null) {
                button = new JButton(loc.getParentBuilding() + " " + loc.getFloor() + "층");
            } else {
                button = new JButton(loc.getName());
            }
            button.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
            button.setBackground(Color.white);
            button.setAlignmentX(Component.LEFT_ALIGNMENT);
            button.addActionListener(e -> {
                for(FloorDetailPanel panel : mainFrame.getOpenFloorPanels())
                    panel.dispose();

                if (loc.getParentBuilding() != null) {
                    FloorDetailPanel dialog = new FloorDetailPanel(mainFrame, graph, loc.getParentBuilding(), loc.getFloor());
                    dialog.setVisible(true);
                } else {
                    mainFrame.getMapPanel().panTo(loc.getX(), loc.getY());
                }

                nowRouteIndex = index;

                int counter = 0;
                for(Component com : routesContainer.getComponents()) {
                    if(com instanceof JButton) {
                        if(counter == nowRouteIndex) com.setBackground(Color.CYAN);
                        else com.setBackground(Color.WHITE);
                        counter++;
                    }
                }

                nowRouteIndex = (nowRouteIndex + 1) % routesList.size();

            });

            routesContainer.add(button);
            routesContainer.add(Box.createVerticalStrut(5));
            if(routesList.getLast() != loc){
                routesContainer.add(new JLabel("↓"));
                routesContainer.add(Box.createVerticalStrut(5));
            }
        }

        routesContainer.add(Box.createVerticalGlue());

        routesScrollPane = new JScrollPane(routesContainer,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        routesScrollPane.setBorder(null);
        routesScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
        routesScrollPane.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));
        routesScrollPane.getVerticalScrollBar().setUnitIncrement(24);

        add(routesScrollPane);
        revalidate();
        repaint();
    }





    private void executeSearch() {
        String keyword = searchField.getText().toLowerCase().trim();
        searchListModel.clear();
        for (Location loc : graph.getNodes().values()) {
            if (LocationCategory.OUTDOOR == loc.getCategory()) continue; // Hide waypoints
            else if (LocationCategory.HALLWAY == loc.getCategory()) continue;
            else if (LocationCategory.STAIRS == loc.getCategory()) continue; 
            else if (LocationCategory.ENTRANCE == loc.getCategory()) continue; 
            else if (loc.getFloor() != 0) continue;
            
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
            try {
                AStarService aStarService = new AStarService();
                List<Location> path = new ArrayList<>();
                if(multipleLocList.isEmpty()){
                    path = aStarService.findShortestPath(graph, start.getId(), end.getId());
                }else{
                    // path.addAll(aStarService.findShortestPath(graph, start.getId(), multipleLocList.getFirst().getId()));
                    Location temp = start;
                    for(Location loc : multipleLocList) {
                        path.addAll(aStarService.findShortestPath(graph, temp.getId(), loc.getId()));
                        path.removeLast();
                        temp = loc;
                    }
                    path.addAll(aStarService.findShortestPath(graph, temp.getId(), end.getId()));
                }
                // 경로가 계산되면 지도 패널에 바로 전달해 렌더링
                // mainFrame.getMapPanel().setPath(path);
                mainFrame.setPathToAllPanels(path);


                JOptionPane.showMessageDialog(this, "경로를 찾았습니다.");
            } catch (EmptyPathException ex) {
                mainFrame.getMapPanel().setPath(Collections.emptyList());
                JOptionPane.showMessageDialog(this, ex.getMessage());
            }
        }

        makeRouteUI();
    }

    private void openMultipleRouteDialog() {
        if (multipleRouteDialog == null || !multipleRouteDialog.isVisible()) {
            multipleRouteDialog = new MultipleRouteDialog(mainFrame, multipleLocList);
            multipleRouteDialog.setVisible(true);
        } else {
            multipleRouteDialog.toFront(); // 이미 열려있으면 앞으로
        }
    }

    public JComboBox<Location> getStartCombo() {
        return this.startCombo;
    }

    public JComboBox<Location> getEndCombo() {
        return this.endCombo;
    }

    public void addMultipleLocList(Location loc) {
        if(multipleLocList.contains(loc)){
            JOptionPane.showMessageDialog(this, "이미 경유지에 있는 장소입니다.");
            return;
        }

        multipleLocList.add(loc);
        if (multipleRouteDialog != null && multipleRouteDialog.isVisible()) {
            multipleRouteDialog.refreshList();
        }
    }

    public MultipleRouteDialog getMultipleRouteDialog() {
        return multipleRouteDialog;
    }

    public List<Location> getMultipleLocList() {
        return multipleLocList;
    }

    public List<Location> getRoutesList() {
        return routesList;
    }

    public int getNowRouteIndex(){
        return nowRouteIndex;
    }

}
