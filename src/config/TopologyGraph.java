package config;

import java.util.*;

/// A custom graph structure capable of pathfinding from one topology device to another.
/// Used only in the `ConfigParser`.
///
/// @author KxtR-27 (Kat)
/// @see ConfigParser
class TopologyGraph {
	/// The graph representing the topology.
	private final Map<String, Set<String>> graph = new HashMap<>();

	/// @param links A map of links, like the map seen in the `links` section of `config.example.json`.
	TopologyGraph(Map<String, String> links) {
		links.forEach(this::putEdge);
	}

	private void putVertex(String vertex) {
		if (!graph.containsKey(vertex))
			graph.put(vertex, new HashSet<>());
	}

	private void putEdge(String vertex1, String vertex2) {
		putVertex(vertex1);
		putVertex(vertex2);

		graph.get(vertex1).add(vertex2);
		graph.get(vertex2).add(vertex1);
	}

	/// Return the vertices at the end of all a given vertex's edges.
	String[] getAdjacentDevicesOf(String id) {
		Set<String> adjacentDevices = graph.get(id);

		return adjacentDevices == null ? null
				: adjacentDevices.toArray(new String[]{});
	}

	/// Uses chained parents from a breadth-first search to trace a path.
	List<String> findShortestPathBetween(String sourceID, String destinationID) {
		Map<String, String> parentMap = bfsForPathBetween(sourceID, destinationID);
		return constructPathFrom(parentMap, destinationID);
	}

	/// Creates the chained parent map from a layer-by-layer search
	private Map<String, String> bfsForPathBetween(String sourceID, String destinationID) {
		Queue<String> currentLayer = new LinkedList<>();
		Queue<String> nextLayer = new LinkedList<>();

		Map<String, String> parentMap = new HashMap<>();

		currentLayer.offer(sourceID);
		parentMap.put(sourceID, null);

		while (!currentLayer.isEmpty()) {
			String currentVertex = currentLayer.poll();

			if (currentVertex.equals(destinationID))
				return parentMap;

			for (String adjacent : graph.get(currentVertex)) {
				if (!parentMap.containsKey(adjacent)) {
					nextLayer.offer(adjacent);
					parentMap.put(adjacent, currentVertex);
				}
			}

			if (currentLayer.isEmpty()) {
				currentLayer = nextLayer;
				nextLayer = new LinkedList<>();
			}
		}

		return null;
	}

	/// Uses the parent map from `#bfsForPathBetween` to create an array of IDs
	private List<String> constructPathFrom(Map<String, String> parentMap, String destinationID) {
		List<String> path = new ArrayList<>();
		String currentVertex = destinationID;

		// sourceID has null parent, so that's how we know we're there
		while (currentVertex != null) {
			path.addFirst(currentVertex);
			currentVertex = parentMap.get(currentVertex);
		}

		return path;
	}
}
