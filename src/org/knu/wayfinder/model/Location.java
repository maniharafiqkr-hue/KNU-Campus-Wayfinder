package org.knu.wayfinder.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 지도의 정점(Node) 정보를 담는 클래스
 * ID 규칙: BBBFFSS (건물번호3-층번호2-고유노드번호2)
 */
public class Location {
    private int id;
    private LocationCategory category;
    private String name;
    private int floor;
    private String building;
    private int x; // 픽셀 좌표 X
    private int y; // 픽셀 좌표 Y
    private String description;
    
    // 이 노드와 연결된 간선(Edge) 리스트 (알고리즘용)
    private List<Edge> neighbors;

    public Location(int id, LocationCategory category, String name, int floor, 
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
    public void addNeighbor(Edge edge) {
        this.neighbors.add(edge);
    }
    
    
    // ================= Getters =================

    public int getId() {
        return id;
    }

    public LocationCategory getCategory() {
        return category;
    }

    public String getName() {
        return name;
    }

    public int getFloor() {
        return floor;
    }

    public String getBuilding() {
        return building;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public String getDescription() {
        return description;
    }

    public List<Edge> getNeighbors() {
        return neighbors;
    }

    // ===========================================

    @Override
    public String toString() {
        return String.format("%s", name);
    }
}