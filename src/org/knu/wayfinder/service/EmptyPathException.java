package org.knu.wayfinder.service;

/**
 * 경로 탐색 중 길을 찾을 수 없을 때 발생하는 커스텀 예외 클래스
 */
public class EmptyPathException extends Exception {
    public EmptyPathException(String message) {
        super(message);
    }
}