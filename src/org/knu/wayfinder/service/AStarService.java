package org.knu.wayfinder.service;

import org.knu.wayfinder.model.Edge;
import org.knu.wayfinder.model.Graph;
import org.knu.wayfinder.model.Location;
import org.knu.wayfinder.model.LocationCategory;

import java.util.*;

public class AStarService {

    /**
     * 휴리스틱 함수 (h): 현재 노드에서 목적지 노드까지의 직선 거리 (유클리드 거리 공식)
     * 층이 다를 경우, 계단이나 엘리베이터를 거쳐야 하므로 물리적 층간 이동 패널티를 더해줍니다.
     */
    private double calculateHeuristic(Location current, Location target) {
        int dx = current.getX() - target.getX();
        int dy = current.getY() - target.getY();
        double baseDistance = Math.sqrt(dx * dx + dy * dy);

        // 실내 내비게이션 특성 반영: 다른 층일 경우 층간 패널티 가중치 부여
        if (current.getFloor() != target.getFloor()) {
            int floorDiff = Math.abs(current.getFloor() - target.getFloor());
            baseDistance += floorDiff * 400; // 프로젝트 스케일에 맞춰 조정 가능
        }

        return baseDistance;
    }

    /**
     * A* 알고리즘을 이용하여 출발지 노드 ID부터 목적지 노드 ID까지의 최단 경로를 반환합니다.
     */
    public List<Location> findShortestPath(Graph graph, int startId, int targetId) throws EmptyPathException {
        Map<Integer, Location> nodes = graph.getNodes();

        // 출발지나 도착지가 지도 데이터에 없는 경우 예외 처리
        if (!nodes.containsKey(startId) || !nodes.containsKey(targetId)) {
            throw new EmptyPathException("출발지 또는 도착지 정보가 지도 데이터에 존재하지 않습니다.");
        }

        Location startNode = nodes.get(startId);
        Location targetNode = nodes.get(targetId);

        // gScore: 출발지로부터 해당 노드까지의 실제 이동 비용
        Map<Integer, Double> gScore = new HashMap<>();
        // fScore: 출발지부터 목적지까지의 예상 총 비용 (gScore + Heuristic)
        Map<Integer, Double> fScore = new HashMap<>();
        // 경로 역추적을 위한 부모 노드 매핑 (Key: 현재 노드 ID, Value: 직전 부모 노드 ID)
        Map<Integer, Integer> parentMap = new HashMap<>();

        // 모든 노드의 이동 비용을 우선 무한대(MAX_VALUE)로 초기화
        for (int id : nodes.keySet()) {
            gScore.put(id, Double.MAX_VALUE);
            fScore.put(id, Double.MAX_VALUE);
        }

        // 시작 노드 초기화
        gScore.put(startId, 0.0);
        fScore.put(startId, calculateHeuristic(startNode, targetNode));

        // fScore를 기준으로 오름차순 정렬하는 우선순위 큐 (Open Set)
        PriorityQueue<Integer> openSet = new PriorityQueue<>(Comparator.comparingDouble(fScore::get));
        openSet.add(startId);

        // 방문 및 최단 경로 확정이 완료된 노드 집합 (Closed Set)
        Set<Integer> closedSet = new HashSet<>();

        while (!openSet.isEmpty()) {
            int currentId = openSet.poll();

            // 우선순위 큐에는 같은 노드가 중복으로 남아 있을 수 있으므로, 이미 확정된 노드는 건너뛴다.
            if (closedSet.contains(currentId)) {
                continue;
            }

            // 목적지에 무사히 도달한 경우, 경로를 역추적하여 리스트로 복원
            if (currentId == targetId) {
                return reconstructPath(nodes, parentMap, targetId);
            }

            closedSet.add(currentId);
            Location currentNode = nodes.get(currentId);

            // 현재 노드와 연결된 모든 간선(이웃) 탐색
            for (Edge edge : currentNode.getNeighbors()) {
                int neighborId = edge.getToId();

                // CSV/그래프 불일치로 노드가 누락된 경우를 방어한다.
                if (!nodes.containsKey(neighborId)) {
                    continue;
                }

                // 이미 탐색이 완전히 끝난 이웃 노드는 패스
                if (closedSet.contains(neighborId)) {
                    continue;
                }

                // 기본 간선 가중치 (픽셀 거리)
                double edgeWeight = edge.getWeight();

                // [피드백 반영] 계단과 엘리베이터 카테고리에 따른 가중치 차등 설정
                Location neighborNode = nodes.get(neighborId);
                if (neighborNode != null) {
                    if (neighborNode.getCategory() == LocationCategory.STAIRS) {
                        edgeWeight *= 1.5; // 계단은 힘드니까 이동 비용을 조금 높게 설정
                    } else if (neighborNode.getCategory() == LocationCategory.ELEVATOR) {
                        edgeWeight *= 0.8; // 엘리베이터는 편하니까 이동 비용을 낮게 설정
                    }
                }

                // 현재 노드를 거쳐서 이웃 노드로 갈 때의 새로운 g(n) 비용 계산
                double tentativeGScore = gScore.get(currentId) + edgeWeight;

                // 새로 계산한 경로가 기존에 발견했던 경로보다 짧은(더 이득인) 경우 정보 갱신
                if (tentativeGScore < gScore.get(neighborId)) {
                    parentMap.put(neighborId, currentId);
                    gScore.put(neighborId, tentativeGScore);

                    // 휴리스틱은 반드시 실제 노드가 존재할 때만 계산한다.
                    double hScore = calculateHeuristic(neighborNode, targetNode);
                    fScore.put(neighborId, tentativeGScore + hScore);

                    // 큐에 들어있지 않다면 추가, 들어있다면 우선순위 갱신을 위해 재삽입
                    if (!openSet.contains(neighborId)) {
                        openSet.add(neighborId);
                    } else {
                        // Java PriorityQueue 특성상 내부 score 변경 시 뺐다 다시 넣어야 재정렬됨
                        openSet.remove(neighborId);
                        openSet.add(neighborId);
                    }
                }
            }
        }

        // 목적지를 찾지 못하고 큐가 비어버린 경우 예외 던짐
        throw new EmptyPathException("출발지에서 도착지까지 이어지는 경로가 존재하지 않습니다.");
    }

    /**
     * 부모 노드 정보를 바탕으로 목적지부터 출발지까지 역추적하여 최종 경로 List를 빌드합니다.
     */
    private List<Location> reconstructPath(Map<Integer, Location> nodes, Map<Integer, Integer> parentMap,
            int targetId) {
        LinkedList<Location> path = new LinkedList<>();
        Integer currentId = targetId;

        while (currentId != null) {
            path.addFirst(nodes.get(currentId)); // 거꾸로 거슬러 올라가므로 앞에 추가
            currentId = parentMap.get(currentId);
        }

        return path;
    }
}