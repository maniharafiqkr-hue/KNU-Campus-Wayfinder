package org.knu.wayfinder.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.knu.wayfinder.model.*;

public class dataLoader {

    public static Map<Integer, Location> loadLocations(String filePath) {
        Map<Integer, Location> locations = new HashMap<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                // ID, Category, Name, Floor, Building, X, Y, Description
                String[] parts = line.split(",", -1);
                if (parts.length >= 8) {
                    try {
                        int id = Integer.parseInt(parts[0].trim());
                        LocationCategory category = LocationCategory.valueOf(parts[1].trim());
                        String name = parts[2].trim();
                        int floor = Integer.parseInt(parts[3].trim());
                        String building = parts[4].trim();
                        int x = Integer.parseInt(parts[5].trim());
                        int y = Integer.parseInt(parts[6].trim());
                        String description = parts[7].trim();
                        
                        locations.put(id, new Location(id, category, name, floor, building, x, y, description));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing location number formatting: " + line);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return locations;
    }

    public static Map<Integer, Location> loadLocations() {
        return loadLocations("src/org/knu/wayfinder/data/locations.csv");
    }
    

    public static List<Edge> loadEdges(String filePath) {
        List<Edge> edges = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                // FromID, ToID, Weight
                String[] parts = line.split(",", -1);
                if (parts.length >= 3) {
                    try {
                        int fromId = Integer.parseInt(parts[0].trim());
                        int toId = Integer.parseInt(parts[1].trim());
                        double weight = Double.parseDouble(parts[2].trim());
            
                        edges.add(new Edge(fromId, toId, weight));
                    } catch (NumberFormatException e) {
                        System.err.println("Error parsing edge number formatting: " + line);
                    }
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return edges;
    }

    public static List<Edge> loadEdges() {
        return loadEdges("src/org/knu/wayfinder/data/edges.csv");
    }


    public static Map<Integer, Location> loadLocationsAndChild(String filePath) {
        Map<Integer, Location> locations = new HashMap<>();
        locations = loadLocations();
        File folder = new File(filePath);
        String parant;
        Location pLoc;
        Location cLoc;
        int beforeFloor = 0;
        int maxFloor = 0;
        for(File dir : folder.listFiles()) {
            beforeFloor = 0;
            maxFloor = 0;
            parant = dir.getName();
            pLoc = locations.get(Integer.parseInt(parant));
            for(File file : dir.listFiles()) {
                if(file.getName().equals("locations.csv")) {
                    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                        String line;
                        br.readLine();
                        while ((line = br.readLine()) != null) {
                            // ID, Category, Name, Floor, Building, X, Y, Description
                            String[] parts = line.split(",", -1);
                            if (parts.length >= 8) {
                                try {
                                    int id = Integer.parseInt(parts[0].trim());
                                    LocationCategory category = LocationCategory.valueOf(parts[1].trim());
                                    String name = parts[2].trim();
                                    int floor = Integer.parseInt(parts[3].trim());
                                    String building = parts[4].trim();
                                    int x = Integer.parseInt(parts[5].trim());
                                    int y = Integer.parseInt(parts[6].trim());
                                    String description = parts[7].trim();

                                    if(beforeFloor < floor) {
                                        maxFloor++;
                                        beforeFloor = floor;
                                        System.out.println(maxFloor);
                                    }
                                    
                                    cLoc = new Location(id, category, name, floor, building, x, y, description);
                                    cLoc.setParentBuilding(pLoc);
                                    locations.put(id, cLoc);
                                    pLoc.addChild(cLoc);
                                    pLoc.setChildMaxFloor(maxFloor);
                                } catch (NumberFormatException e) {
                                    System.err.println("Error parsing location number formatting: " + line);
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return locations;
    }

    public static Map<Integer, Location> loadLocationsAndChild() {
        return loadLocationsAndChild("src/org/knu/wayfinder/data/children");
    }



    public static List<Edge> loadEdgesAndChild(String filePath) {
        List<Edge> edges = new ArrayList<>();
        edges = loadEdges();
        File folder = new File(filePath);
        for(File dir : folder.listFiles()) {
            for(File file : dir.listFiles()) {
                if(file.getName().equals("edges.csv")) {
                    edges.addAll(loadEdges(file.getAbsolutePath()));
                }
            }
        }

        // 일단 이거 써보고 나중에 최적화
        return edges;
    }

    public static List<Edge> loadEdgesAndChild() {
        return loadEdgesAndChild("src/org/knu/wayfinder/data/children");
    }  

    // [ 내부 장소의 입구 <-> 외부 건물 ] 연결시켜줌 (MainApp에서 애도 돌려야됨)
    public static List<Edge> connectEntrance(Map<Integer, Location> locations, List<Edge> edges) {
        for(Location loc : locations.values()) {
            if(loc.getCategory() == LocationCategory.ENTRANCE && loc.getFloor() != 0) {
                if(loc.getParentBuilding() != null)
                    edges.add(new Edge(loc.getId(), loc.getParentBuilding().getId(), 0));
            }
        }

        return edges;
    }

}
