package org.knu.wayfinder.model;

/**
 * 지도 노드의 종류를 정의하는 열거형입니다.
 */
public enum NodeCategory {
    CLASSROOM,  // 강의실
    OFFICE,     // 교수실/사무실
    RESTROOM,   // 화장실
    ENTRANCE,   // 건물 입구
    STAIRS,     // 계단
    ELEVATOR,   // 엘리베이터
    HALLWAY,    // 복도(라우팅용)
    OUTDOOR     // 야외 경로
}