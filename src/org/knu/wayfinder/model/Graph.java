package org.knu.wayfinder.model;

import java.util.*;

/**
 * 지도 데이터를 관리하고 경로 탐색(A* 알고리즘)을 수행하는 Graph 클래스
 */
public class Graph {
    // 노드들을 저장하는 맵 (Key: Location ID)
    private Map<Integer, Location> nodes;
    
    // 인접 리스트 (Key: 출발지 ID, Value: 출발지와 연결된 간선 리스트)
    private Map<Integer, List<Edge>> adjacencyList;

    public Graph() {
        this.nodes = new HashMap<>();
        this.adjacencyList = new HashMap<>();
    }

    /**
     * 그래프에 노드(Location) 추가
     */
    public void addLocation(Location location) {
        nodes.put(location.getId(), location);
        adjacencyList.putIfAbsent(location.getId(), new ArrayList<>());
    }
    //putIfAbsent?

    /**
     * 그래프에 간선(Edge) 추가
     * (주의: 무방향 그래프를 원할 경우 DataLoader에서 양방향 Edge를 모두 넣어주거나,
     * 여기서 반대 방향 Edge도 강제로 넣어주는 방식을 선택해야 합니다. 현재는 입력된 Edge만 처리합니다.)
     */
    public void addEdge(Edge edge) {
        int from = edge.getFromId();
        
        // 1. 인접 리스트에 추가
        adjacencyList.putIfAbsent(from, new ArrayList<>());
        adjacencyList.get(from).add(edge);//?
        
        // 2. Location 객체 내부의 neighbors 리스트에도 동기화
        if (nodes.containsKey(from)) {
            nodes.get(from).addNeighbor(edge);
        }
    }

    public Map<Integer, Location> getNodes() {
        return nodes;
    }

    public Map<Integer, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

}