package org.knu.wayfinder.model;

/**
 * 노드와 노드 사이의 연결 정보를 담는 클래스
 */
public class Edge {
    private int fromId;
    private int toId;
    private double weight;

    /**
     * @param fromId 출발 노드 ID
     * @param toId 도착 노드 ID
     * @param weight 이동 비용 (초기에는 픽셀거리로 계산)
     */
    public Edge(int fromId, int toId, double weight) {
        this.fromId = fromId;
        this.toId = toId;
        this.weight = weight;
    }

    public int getFromId() { return fromId; }
    public int getToId() { return toId; }
    public double getWeight() { return weight; }
}