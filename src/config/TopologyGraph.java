package config;

import java.util.*;

/// Custom graph structure capable of pathfinding from one topology device to another
/// @author KxtR-27 (Kat)
class TopologyGraph {
	private final Map<String, Set<String>> graph = new HashMap<>();

	TopologyGraph(Map<String, String> links) {
		links.forEach(this::putEdge);
	}

	private void putVertex(String vertex) {
		boolean vertexIsNew = !graph.containsKey(vertex);

		if (vertexIsNew)
			graph.put(vertex, new HashSet<>());
	}

	private void putEdge(String vertex1, String vertex2) {
		putVertex(vertex1);
		putVertex(vertex2);

		graph.get(vertex1).add(vertex2);
		graph.get(vertex2).add(vertex1);
	}


	String[] getAdjacentDevicesOf(String id) {
		Set<String> adjacentDevices = graph.get(id);

		return adjacentDevices == null
				? null
				: adjacentDevices.toArray(new String[]{});
	}


	List<String> findShortestPathBetween(String sourceID, String destinationID) {
		Map<String, String> parentMap = bfsForPathBetween(sourceID, destinationID);
		return constructPathFrom(parentMap, destinationID);
	}

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
