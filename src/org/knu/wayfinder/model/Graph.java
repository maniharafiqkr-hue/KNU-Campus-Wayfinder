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
    //putIfAbsent: Map에 해당 키(key)가 존재하지 않을 때만 뒤에 입력한 값(value)을 맵에 추가. 
    // 이미 키가 존재한다면 기존 값을 그대로 유지

    /**
     * 그래프에 간선(Edge) 추가
     * (실내 길찾기는 대부분 양방향 이동이 가능하므로, 동일 간선의 역방향도 함께 보강한다.)
     */
    public void addEdge(Edge edge) {
        int from = edge.getFromId();
        int to = edge.getToId();
        
        // 1. 인접 리스트에 추가
        adjacencyList.putIfAbsent(from, new ArrayList<>());
        adjacencyList.get(from).add(edge);
        // 위에서 putIfAbsent를 통해 리스트가 확실히 생성되었음을 보장받았기 때문에, 
        // 안심하고 출발지 ID(from)의 간선 리스트를 꺼내와서(get), 
        // 새롭게 연결된 간선 객체(edge)를 추가(add)해 주는 로직
        
        // 2. Location 객체 내부의 neighbors 리스트에도 동기화
        if (nodes.containsKey(from)) {
            nodes.get(from).addNeighbor(edge);
        }

        // 3. CSV에는 단방향 간선만 존재하므로, 탐색이 한쪽으로만 끊기지 않도록 역방향도 보강
        if (!hasEdge(to, from)) {
            Edge reverseEdge = new Edge(to, from, edge.getWeight());
            adjacencyList.putIfAbsent(to, new ArrayList<>());
            adjacencyList.get(to).add(reverseEdge);

            if (nodes.containsKey(to)) {
                nodes.get(to).addNeighbor(reverseEdge);
            }
        }
    }

    private boolean hasEdge(int fromId, int toId) {
        List<Edge> edges = adjacencyList.get(fromId);
        if (edges == null) {
            return false;
        }

        for (Edge edge : edges) {
            if (edge.getToId() == toId) {
                return true;
            }
        }

        return false;
    }

    public Map<Integer, Location> getNodes() {
        return nodes;
    }

    public Map<Integer, List<Edge>> getAdjacencyList() {
        return adjacencyList;
    }

}