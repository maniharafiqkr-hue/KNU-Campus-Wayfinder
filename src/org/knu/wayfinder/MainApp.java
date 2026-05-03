package org.knu.wayfinder;

import javax.swing.SwingUtilities;

/**
 * KNU Campus Wayfinder 메인 실행 클래스
 */
public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("KNU Campus Wayfinder 시스템을 시작합니다...");
            
            // 1주차에는 여기에 로딩 화면(SplashScreen)이나 메인 윈도우를 띄우는 코드가 들어갈 예정
            // 현재는 프로젝트 설정 확인용으로 콘솔 출력만
        });
    }
}