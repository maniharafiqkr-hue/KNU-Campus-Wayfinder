package org.knu.wayfinder.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 지도의 정점(Node) 정보를 담는 클래스
 * ID 규칙: BBBFFSS (건물번호3-층번호2-고유노드번호2)
 */
public class MapNode {
    private int id;
    private NodeCategory category;
    private String name;
    private int floor;
    private String building;
    private int x; // 픽셀 좌표 X
    private int y; // 픽셀 좌표 Y
    private String description;
    
    // 이 노드와 연결된 간선(Edge) 리스트 (알고리즘용)
    private List<Edge> neighbors;

    public MapNode(int id, NodeCategory category, String name, int floor, 
                   String building, int x, int y, String description) {
        this.id = id;
        this.category = category;
        this.name = name;
        this.floor = floor;
        this.building = building;
        this.x = x;
        this.y = y;
        this.description = description;
        this.neighbors = new ArrayList<>();
    }

    // Getter & Setter (CamelCase 준수)
    public int getId() { return id; }
    public NodeCategory getCategory() { return category; }
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public List<Edge> getNeighbors() { return neighbors; }

    /**
     * 인접한 노드와의 연결(간선)을 추가합니다.
     * @param edge 추가할 간선 객체
     */
    public void addNeighbor(Edge edge) {
        this.neighbors.add(edge);
    }
}