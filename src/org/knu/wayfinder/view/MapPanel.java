package org.knu.wayfinder.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.imageio.ImageIO;
import javax.swing.*;
import org.knu.wayfinder.model.Edge;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;
import org.knu.wayfinder.model.LocationCategory;

public class MapPanel extends JPanel {
    private Graph graph;
    private MainFrame mainFrame;
    
    private BufferedImage bgImage;
    
    private Location RClickedLocation = null;
    private JPopupMenu popupMenu;
    private JMenuItem setStartItem;
    private JMenuItem setEndItem;
    private JMenuItem addRouteItem;

    private double zoomFactor = 1.0;
    private double prevZoomFactor = 1.0;
    private boolean zoomer = false;
    private boolean dragger = false;
    private boolean released = true;
    private double xOffset = 0;
    private double yOffset = 0;
    private int xDiff;
    private int yDiff;
    private Point startPoint;

    private static final Color[] ROUTE_COLORS = {
        Color.RED, Color.ORANGE, Color.GREEN,  
        Color.CYAN, Color.BLUE, Color.MAGENTA, Color.BLACK
    };

    // FloorDetailPanel용
    private Location parentLocation = null; 
    private int currentFloor = 0;

    private List<Location> currentPath = Collections.emptyList();
    private Location hoveredLocation = null;

    public MapPanel(Graph graph, MainFrame mainFrame) {
        this.graph = graph;
        this.mainFrame = mainFrame;
        
        // setBackground(new Color(240, 240, 245));
        try {
            this.bgImage = ImageIO.read(getClass().getResource("/org/knu/wayfinder/data/img/knu_map.png"));
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 우클릭 선택창
        popupMenu = new JPopupMenu();
        setStartItem = new JMenuItem("출발지로 설정");
        setEndItem = new JMenuItem("도착지로 설정");
        addRouteItem = new JMenuItem("경유지로 추가");

        popupMenu.add(setStartItem);
        popupMenu.add(setEndItem);
        popupMenu.add(addRouteItem);

        
        // DefaultComboBoxModel에 getIndexOf 있음
        JComboBox<Location> startCombo = mainFrame.getSidebarPanel().getStartCombo();
        DefaultComboBoxModel<Location> sModel = (DefaultComboBoxModel<Location>) startCombo.getModel();
        
        JComboBox<Location> endCombo = mainFrame.getSidebarPanel().getEndCombo();
        DefaultComboBoxModel<Location> eModel = (DefaultComboBoxModel<Location>) endCombo.getModel();

        setStartItem.addActionListener(e -> {
            if(sModel.getIndexOf(RClickedLocation) == -1){
                startCombo.addItem(RClickedLocation);
            }
            if (RClickedLocation != null) startCombo.setSelectedItem(RClickedLocation);
        });
        setEndItem.addActionListener(e -> {
            if(eModel.getIndexOf(RClickedLocation) == -1){
                endCombo.addItem(RClickedLocation);
            }
            if (RClickedLocation != null) endCombo.setSelectedItem(RClickedLocation);
        });
        addRouteItem.addActionListener(e -> {
            mainFrame.getSidebarPanel().addMultipleLocList(RClickedLocation);
        });

        MapMouseAdapter adapter = new MapMouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    public MapPanel(Graph graph, MainFrame mainFrame, Location parentLocation, int floor) {
        this.graph = graph;
        this.mainFrame = mainFrame;
        this.parentLocation = parentLocation;
        this.currentFloor = floor;
        
        
        String imgPath = "/org/knu/wayfinder/data/children/" + parentLocation.getId() + "/img/" + currentFloor + ".png"; // 실내 도면

        try {
            this.bgImage = ImageIO.read(getClass().getResource(imgPath));
        } catch (Exception e) {
            e.printStackTrace();
        }
        

        // 우클릭 선택창
        popupMenu = new JPopupMenu();
        setStartItem = new JMenuItem("출발지로 설정");
        setEndItem = new JMenuItem("도착지로 설정");
        addRouteItem = new JMenuItem("경유지로 추가");
        popupMenu.add(setStartItem);
        popupMenu.add(setEndItem);
        popupMenu.add(addRouteItem);

        
        // DefaultComboBoxModel에 getIndexOf 있음
        JComboBox<Location> startCombo = mainFrame.getSidebarPanel().getStartCombo();
        DefaultComboBoxModel<Location> sModel = (DefaultComboBoxModel<Location>) startCombo.getModel();
        
        JComboBox<Location> endCombo = mainFrame.getSidebarPanel().getEndCombo();
        DefaultComboBoxModel<Location> eModel = (DefaultComboBoxModel<Location>) endCombo.getModel();

        setStartItem.addActionListener(e -> {
            if(sModel.getIndexOf(RClickedLocation) == -1){
                
                startCombo.addItem(RClickedLocation);
            }
            if (RClickedLocation != null) startCombo.setSelectedItem(RClickedLocation);
        });
        setEndItem.addActionListener(e -> {
            if(eModel.getIndexOf(RClickedLocation) == -1){
                endCombo.addItem(RClickedLocation);
            }
            if (RClickedLocation != null) endCombo.setSelectedItem(RClickedLocation);
        });
        addRouteItem.addActionListener(e -> {
            mainFrame.getSidebarPanel().addMultipleLocList(RClickedLocation);
        });
        

        MapMouseAdapter adapter = new MapMouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    public void setPath(List<Location> path) {
        this.currentPath = path != null ? path : Collections.emptyList();
        repaint();
    }

    // 시야 이동
    public void panTo(double x, double y) {    
        xOffset = getWidth() / 2.0 - (x * zoomFactor);
        yOffset = getHeight() / 2.0 - (y * zoomFactor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // 마우스 휠로 확대/축소
        if (zoomer) {
            double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
            double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

            double zoomDiv = zoomFactor / prevZoomFactor;

            xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * xRel;
            yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * yRel;

            prevZoomFactor = zoomFactor;
            zoomer = false;
        }

        // 드래그로 움직이기
        if (dragger) {
            AffineTransform at = new AffineTransform();
            at.translate(xOffset + xDiff, yOffset + yDiff);
            at.scale(zoomFactor, zoomFactor);
            dragger = false;
        }

        AffineTransform at = new AffineTransform();
        at.translate(xOffset, yOffset);
        at.scale(zoomFactor, zoomFactor);
        g2.transform(at);

        g2.drawImage(this.bgImage, 0, 0, this);

        // edge 그리기
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(2.0f / (float)zoomFactor));

        Set<String> drawnEdges = new HashSet<>();

        for (List<Edge> edges : graph.getAdjacencyList().values()) {
            for (Edge edge : edges) {
                if(parentLocation != null){
                   if(graph.getNodes().get(edge.getFromId()).getParentBuilding() != parentLocation ||
                    graph.getNodes().get(edge.getFromId()).getFloor() != currentFloor ||
                    graph.getNodes().get(edge.getToId()).getFloor() != currentFloor) 
                        continue;
                }
                else if(graph.getNodes().get(edge.getFromId()).getFloor() != 0 ||
                    graph.getNodes().get(edge.getToId()).getFloor() != 0){
                        continue;
                }

                String edgeKey = edge.getFromId() < edge.getToId()
                        ? edge.getFromId() + "-" + edge.getToId()
                        : edge.getToId() + "-" + edge.getFromId();

                if (drawnEdges.add(edgeKey)) {
                    drawEdge(g2, edge, false);
                }
            }
        }

        // 루트 그리기
        if (!currentPath.isEmpty()) {
            // g2.setColor(new Color(100, 150, 255, 180));
            g2.setStroke(new BasicStroke(5.0f / (float)zoomFactor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            mainFrame.setPathToAllPanels(currentPath);
            drawPath(g2, currentPath);
        }

        // 노드 그리기
        double nodeRadius = 8.0 / zoomFactor;
        for (Location loc : graph.getNodes().values()) {
            if (LocationCategory.OUTDOOR == loc.getCategory()) continue; // Hide waypoints
            else if (LocationCategory.HALLWAY == loc.getCategory()) continue;
            else if (LocationCategory.STAIRS == loc.getCategory()) continue; 
            else if (LocationCategory.ENTRANCE == loc.getCategory()) continue;
            else if (loc.getFloor() != 0 && parentLocation == null) continue;
            
            if (parentLocation != null) {
                if(loc.getParentBuilding() != parentLocation || loc.getFloor() != currentFloor) continue;
            }
            

            if (loc.equals(hoveredLocation)) {
                g2.setColor(Color.ORANGE);
            } else {
                g2.setColor(new Color(50, 100, 200));
            }
            
            g2.fillOval((int)(loc.getX() - nodeRadius), (int)(loc.getY() - nodeRadius), (int)(nodeRadius*2), (int)(nodeRadius*2));
            
            // Labels
            g2.setColor(Color.BLACK);
            Font prevFont = g2.getFont();
            g2.setFont(prevFont.deriveFont(12f / (float)zoomFactor));
            g2.drawString(loc.getName(), (float)(loc.getX() + nodeRadius + 2), (float)(loc.getY() + nodeRadius));
        }
    }

    private void drawEdge(Graphics2D g2, Edge edge, boolean isHighlight) {
        Location fromLoc = graph.getNodes().get(edge.getFromId());
        Location toLoc = graph.getNodes().get(edge.getToId());

        if (fromLoc == null || toLoc == null) return;

        Path2D.Double path = new Path2D.Double();
        path.moveTo(fromLoc.getX(), fromLoc.getY());
        
        path.lineTo(toLoc.getX(), toLoc.getY());
        g2.draw(path);
    }

    private void drawPath(Graphics2D g2, List<Location> path) {
        if (path.size() < 2) return;

        List<Location> routesList = mainFrame.getSidebarPanel().getRoutesList();

        List<Integer> boundaryIndices = new ArrayList<>();
        boundaryIndices.add(0); // 시작점

        // 구간 따기
        if(routesList == null) return;

        for (Location waypoint : routesList.subList(1, routesList.size())) {
            for (int i = 1; i < path.size() - 1; i++) {
                if (path.get(i).getId() == waypoint.getId() && boundaryIndices.getLast() < i) {
                    boundaryIndices.add(i);
                    break;
                }
            }
        }
        boundaryIndices.add(path.size() - 1);
        // System.out.println(boundaryIndices);
        // System.out.println(routesList);
        

        int from, to;
        for (int seg = 0; seg < boundaryIndices.size() - 1; seg++) {
            from = boundaryIndices.get(seg);
            to = boundaryIndices.get(seg + 1);

            g2.setColor(ROUTE_COLORS[seg % ROUTE_COLORS.length]);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f));
            

            Path2D.Double route = new Path2D.Double();
            boolean started = false;

            for (int i = from; i <= to; i++) {
                Location loc = path.get(i);
                if (loc.getFloor() != currentFloor || loc.getParentBuilding() != parentLocation) continue;

                if (!started) {
                    route.moveTo(loc.getX(), loc.getY());
                    started = true;
                } else {
                    route.lineTo(loc.getX(), loc.getY());
                }
            }

            g2.draw(route);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
        }

        int nowIndex = mainFrame.getSidebarPanel().getNowRouteIndex();
        // System.out.println(nowIndex);
        if(boundaryIndices.size()-1 < nowIndex) return;

        if(nowIndex == 0){
            from  = boundaryIndices.get(boundaryIndices.size()-2);
            to = boundaryIndices.getLast();
        }else{
            from = boundaryIndices.get(nowIndex-1);
            to = boundaryIndices.get(nowIndex);
        }
        g2.setColor(Color.RED);

        Path2D.Double route = new Path2D.Double();
        boolean started = false;
        for (int i = from; i <= to; i++) {
            Location loc = path.get(i);
            if (loc.getFloor() != currentFloor || loc.getParentBuilding() != parentLocation) continue;

            if (!started) {
                route.moveTo(loc.getX(), loc.getY());
                started = true;
            } else {
                route.lineTo(loc.getX(), loc.getY());
            }
        }

        g2.draw(route);

    }

    public List<Location> getCurrentPath(){
        return this.currentPath;
    }

    private class MapMouseAdapter extends MouseAdapter {
        @Override
        public void mousePressed(MouseEvent e) {
            released = false;
            startPoint = MouseInfo.getPointerInfo().getLocation();
        }

        @Override
        public void mouseDragged(MouseEvent e) {
            Point p = MouseInfo.getPointerInfo().getLocation();
            xDiff = p.x - startPoint.x;
            yDiff = p.y - startPoint.y;

            xOffset += xDiff;
            yOffset += yDiff;

            startPoint = p;
            dragger = true;
            repaint();
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            released = true;
            repaint();
        }

        @Override
        public void mouseWheelMoved(MouseWheelEvent e) {
            zoomer = true;
            if (e.getWheelRotation() < 0) {
                zoomFactor *= 1.1; // Zoom in
            }
            if (e.getWheelRotation() > 0) {
                zoomFactor /= 1.1; // Zoom out
            }
            repaint();
        }

        @Override
        public void mouseClicked(MouseEvent e) {
             try {
                // 화면 좌표 -> 절대 좌표
                AffineTransform at = new AffineTransform();
                at.translate(xOffset, yOffset);
                at.scale(zoomFactor, zoomFactor);
                
                Point2D.Double ptClick = new Point2D.Double(e.getX(), e.getY());
                Point2D.Double ptMap = new Point2D.Double();
                at.inverseTransform(ptClick, ptMap);

                // 가장 가까운 노드 찾기
                Location closest = null;
                double minDist = Double.MAX_VALUE;
                for (Location loc : graph.getNodes().values()) {
                    if (LocationCategory.OUTDOOR == loc.getCategory()) continue; // Hide waypoints
                    else if (LocationCategory.HALLWAY == loc.getCategory()) continue;
                    else if (LocationCategory.STAIRS == loc.getCategory()) continue; 
                    else if (LocationCategory.ENTRANCE == loc.getCategory()) continue; 
                    else if (loc.getFloor() != 0 && parentLocation == null) continue;

                    if (parentLocation != null) {
                        if(loc.getParentBuilding() != parentLocation || loc.getFloor() != currentFloor) continue;
                    }
                    
                    double dx = loc.getX() - ptMap.x;
                    double dy = loc.getY() - ptMap.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    
                    if (dist < 15 / zoomFactor && dist < minDist) {
                        closest = loc;
                        minDist = dist;
                    }
                }

                if (closest != null && SwingUtilities.isLeftMouseButton(e)) {
                    mainFrame.onLocationSelectedFromMap(closest);
                }
                else if (closest != null && SwingUtilities.isRightMouseButton(e)) {
                    RClickedLocation = closest;
                    popupMenu.show(e.getComponent(), e.getX(), e.getY());
                }

            } catch (NoninvertibleTransformException ex) {
                ex.printStackTrace();
            }
        }
        
        @Override
        public void mouseMoved(MouseEvent e) {
             try {
                AffineTransform at = new AffineTransform();
                at.translate(xOffset, yOffset);
                at.scale(zoomFactor, zoomFactor);
                
                Point2D.Double ptHover = new Point2D.Double(e.getX(), e.getY());
                Point2D.Double ptMap = new Point2D.Double();
                at.inverseTransform(ptHover, ptMap);

                Location previousHover = hoveredLocation;
                hoveredLocation = null;
                double minDist = Double.MAX_VALUE;
                for (Location loc : graph.getNodes().values()) {
                    if (LocationCategory.OUTDOOR == loc.getCategory()) continue; // Hide waypoints
                    else if (LocationCategory.HALLWAY == loc.getCategory()) continue;
                    else if (LocationCategory.STAIRS == loc.getCategory()) continue; 
                    else if (LocationCategory.ENTRANCE == loc.getCategory()) continue; 
                    else if (loc.getFloor() != 0 && parentLocation == null) continue;

                    if (parentLocation != null) {
                        if(loc.getParentBuilding() != parentLocation || loc.getFloor() != currentFloor) continue;
                    }

                    double dx = loc.getX() - ptMap.x;
                    double dy = loc.getY() - ptMap.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    
                    if (dist < 15 / zoomFactor && dist < minDist) {
                        hoveredLocation = loc;
                        minDist = dist;
                    }
                }
                
                if (hoveredLocation != previousHover) {
                    repaint();
                }

            } catch (NoninvertibleTransformException ex) {
                ex.printStackTrace();
            }
        }
    }
}
