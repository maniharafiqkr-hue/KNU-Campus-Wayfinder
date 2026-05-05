package org.knu.wayfinder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.SwingUtilities;
import org.knu.wayfinder.data.dataLoader;
import org.knu.wayfinder.model.Edge;
import org.knu.wayfinder.model.Location;

/**
 * KNU Campus Wayfinder 메인 실행 클래스
 */
public class MainApp {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            System.out.println("KNU Campus Wayfinder 시스템을 시작합니다...");
            
            // testing
            Map<Integer, Location> locations = new HashMap<>();
            List<Edge> edges = new ArrayList<>();
            
            locations = dataLoader.loadLocations("src/org/knu/wayfinder/data/locations.csv");
            edges = dataLoader.loadEdges("src/org/knu/wayfinder/data/edges.csv");
            
            System.out.println(locations);
            System.out.println(edges);
            
            
        });
    }
}