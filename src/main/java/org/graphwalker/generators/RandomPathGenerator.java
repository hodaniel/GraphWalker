package org.graphwalker.generators;

import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.graphwalker.Util;
import org.graphwalker.exceptions.FoundNoEdgeException;
import org.graphwalker.graph.Edge;

public class RandomPathGenerator extends PathGenerator {

	private static Logger logger = Util.setupLogger(RandomPathGenerator.class);

	private Random random = new Random();

	public String[] getNext() throws InterruptedException {
		Set<Edge> availableEdges;
		try {
			availableEdges = getMachine().getCurrentOutEdges();
		} catch (FoundNoEdgeException e) {
			throw new RuntimeException("No possible edges available for path", e);
		}
		if (Thread.interrupted()) {
	    throw new InterruptedException();
		}
		Edge edge = (getMachine().isWeighted() ? getWeightedEdge(availableEdges) : getRandomEdge(availableEdges));
		getMachine().walkEdge(edge);
		logger.debug(edge.getFullLabelKey());
		logger.debug(edge);
		String[] retur = { getMachine().getEdgeName(edge), getMachine().getCurrentVertexName() };
		return retur;
	}

	private Edge getWeightedEdge(Set<Edge> availableEdges) {
		Object[] edges = availableEdges.toArray();
		Edge edge = null;
		float probabilities[] = new float[availableEdges.size()];
		int numberOfZeros = 0;
		float sum = 0;

		for (int i = 0; i < edges.length; i++) {
			edge = (Edge) edges[i];

			if (edge.getWeightKey() > 0) {
				Float weight = edge.getWeightKey();
				probabilities[i] = weight.floatValue();
				sum += probabilities[i];
			} else {
				numberOfZeros++;
				probabilities[i] = 0;
			}
		}

		if (sum > 1) {
			throw new RuntimeException("The sum of all weights in edges from vertex: '" + getMachine().getModel().getSource(edge).getLabelKey()
			    + "', adds up to more than 1.00");
		}

		float rest = (1 - sum) / numberOfZeros;
		int index = random.nextInt(100);
		logger.debug("Randomized integer index = " + index);

		float weight = 0;
		for (int i = 0; i < edges.length; i++) {
			if (probabilities[i] == 0) {
				probabilities[i] = rest;
			}
			logger.debug("The edge: '" + (String) ((Edge) edges[i]).getLabelKey() + "' is given the probability of " + probabilities[i] * 100
			    + "%");

			weight = weight + probabilities[i] * 100;
			logger.debug("Current weight is: " + weight);
			if (index < weight) {
				edge = (Edge) edges[i];
				logger.debug("Selected edge is: " + edge);
				break;
			}
		}

		return edge;
	}

	private Edge getRandomEdge(Set<Edge> availableEdges) {
		return (Edge) availableEdges.toArray()[random.nextInt(availableEdges.size())];
	}

	public String toString() {
		return "RANDOM{" + super.toString() + "}";
	}
}
