package org.knu.wayfinder.data;

import java.io.BufferedReader;
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
            br.readLine(); // Skip header
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

    public static List<Edge> loadEdges(String filePath) {
        List<Edge> edges = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                // FromID, ToID, Weight
                String[] parts = line.split(",", -1);
                
                try {
                    int fromId = Integer.parseInt(parts[0].trim());
                    int toId = Integer.parseInt(parts[1].trim());
                    double weight = Double.parseDouble(parts[2].trim());
         
                    edges.add(new Edge(fromId, toId, weight));
                } catch (NumberFormatException e) {
                    System.err.println("Error parsing edge number formatting: " + line);
                }
                
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return edges;
    }
}
