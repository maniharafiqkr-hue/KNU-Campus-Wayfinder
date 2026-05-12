package org.knu.wayfinder.view;

import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.Collections;
import java.util.List;
import javax.swing.*;
import org.knu.wayfinder.model.Edge;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;
import org.knu.wayfinder.model.LocationCategory;

public class MapPanel extends JPanel {
    private Graph graph;
    private MainFrame mainFrame;

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

    private List<Edge> currentPath = Collections.emptyList();
    private Location hoveredLocation = null;

    public MapPanel(Graph graph, MainFrame mainFrame) {
        this.graph = graph;
        this.mainFrame = mainFrame;
        setBackground(new Color(240, 240, 245));

        MapMouseAdapter adapter = new MapMouseAdapter();
        addMouseListener(adapter);
        addMouseMotionListener(adapter);
        addMouseWheelListener(adapter);
    }

    public void setPath(List<Edge> path) {
        this.currentPath = path != null ? path : Collections.emptyList();
        repaint();
    }

    public void panTo(double x, double y) {
        // Center the view on a specific coordinate
        xOffset = getWidth() / 2.0 - (x * zoomFactor);
        yOffset = getHeight() / 2.0 - (y * zoomFactor);
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (zoomer) {
            AffineTransform at = new AffineTransform();
            
            // Zoom logic around center point
            double xRel = MouseInfo.getPointerInfo().getLocation().getX() - getLocationOnScreen().getX();
            double yRel = MouseInfo.getPointerInfo().getLocation().getY() - getLocationOnScreen().getY();

            double zoomDiv = zoomFactor / prevZoomFactor;

            xOffset = (zoomDiv) * (xOffset) + (1 - zoomDiv) * xRel;
            yOffset = (zoomDiv) * (yOffset) + (1 - zoomDiv) * yRel;

            prevZoomFactor = zoomFactor;
            zoomer = false;
        }

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

        // Draw all edges (roads / pathways)
        g2.setColor(new Color(200, 200, 200));
        g2.setStroke(new BasicStroke(2.0f / (float)zoomFactor));

        for (List<Edge> edges : graph.getAdjacencyList().values()) {
            for (Edge edge : edges) {
                drawEdge(g2, edge, false);
            }
        }

        // Draw Route (A*)
        if (!currentPath.isEmpty()) {
            g2.setColor(new Color(255, 50, 50, 180));
            g2.setStroke(new BasicStroke(5.0f / (float)zoomFactor, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            for (Edge edge : currentPath) {
                drawEdge(g2, edge, true);
            }
        }

        // Draw nodes
        double nodeRadius = 8.0 / zoomFactor;
        for (Location loc : graph.getNodes().values()) {
            if (LocationCategory.OUTDOOR == loc.getCategory()) continue; // Hide waypoints
            if (LocationCategory.STAIRS == loc.getCategory()) continue; 
            if (LocationCategory.ENTRANCE == loc.getCategory()) continue; 

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
                // Determine true map coordinates from screen coordinates
                AffineTransform at = new AffineTransform();
                at.translate(xOffset, yOffset);
                at.scale(zoomFactor, zoomFactor);
                
                Point2D.Double ptClick = new Point2D.Double(e.getX(), e.getY());
                Point2D.Double ptMap = new Point2D.Double();
                at.inverseTransform(ptClick, ptMap);

                // Find closest node
                Location closest = null;
                double minDist = Double.MAX_VALUE;
                for (Location loc : graph.getNodes().values()) {
                    if (LocationCategory.OUTDOOR == loc.getCategory()) continue; // Hide waypoints
                    if (LocationCategory.STAIRS == loc.getCategory()) continue; 
                    if (LocationCategory.ENTRANCE == loc.getCategory()) continue; 
                    
                    double dx = loc.getX() - ptMap.x;
                    double dy = loc.getY() - ptMap.y;
                    double dist = Math.sqrt(dx*dx + dy*dy);
                    
                    if (dist < 15 / zoomFactor && dist < minDist) {
                        closest = loc;
                        minDist = dist;
                    }
                }

                if (closest != null) {
                    mainFrame.onLocationSelectedFromMap(closest);
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
                    if (LocationCategory.STAIRS == loc.getCategory()) continue; 
                    if (LocationCategory.ENTRANCE == loc.getCategory()) continue; 
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
