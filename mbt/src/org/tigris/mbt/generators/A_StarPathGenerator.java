package org.tigris.mbt.generators;

import java.util.Comparator;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.tigris.mbt.Util;
import org.tigris.mbt.exceptions.FoundNoEdgeException;
import org.tigris.mbt.generators.PathGenerator;
import org.tigris.mbt.graph.Edge;
import org.tigris.mbt.graph.Vertex;
import org.tigris.mbt.machines.FiniteStateMachine;

public class A_StarPathGenerator extends PathGenerator {

	static Logger logger = Util.setupLogger(A_StarPathGenerator.class);

	private Stack<Edge> preCalculatedPath = null;
	private Vertex lastVertex;

	public void setMachine(FiniteStateMachine machine) {
		super.setMachine(machine);
	}

	public String[] getNext() throws InterruptedException {
		Util.AbortIf(!hasNext(), "Finished");
		if (lastVertex == null || lastVertex != getMachine().getCurrentVertex() || preCalculatedPath == null || preCalculatedPath.size() == 0) {
			boolean oldCalculatingPathValue = getMachine().isCalculatingPath();
			getMachine().setCalculatingPath(true);

			preCalculatedPath = a_star();

			getMachine().setCalculatingPath(oldCalculatingPathValue);

			if (preCalculatedPath == null) {
				throw new RuntimeException("No path found to " + this.getStopCondition());
			}

			// reverse path
			Stack<Edge> temp = new Stack<Edge>();
			while (preCalculatedPath.size() > 0) {
				temp.push(preCalculatedPath.pop());
			}
			preCalculatedPath = temp;
		}

		Edge edge = (Edge) preCalculatedPath.pop();
		getMachine().walkEdge(edge);
		lastVertex = getMachine().getCurrentVertex();
		String[] retur = { getMachine().getEdgeName(edge), getMachine().getCurrentVertexName() };
		return retur;
	}

	@SuppressWarnings("unchecked")
	private Stack<Edge> a_star() throws InterruptedException {
		Vector<String> closed = new Vector<String>();

		PriorityQueue<WeightedPath> a_starPath = new PriorityQueue<WeightedPath>(10, new Comparator<WeightedPath>() {
			public int compare(WeightedPath arg0, WeightedPath arg1) {
				int retur = Double.compare(arg0.getWeight(), arg1.getWeight());
				if (retur == 0)
					retur = arg0.getPath().size() - arg1.getPath().size();
				return retur;
			}
		});

		Set<Edge> availableOutEdges;
		try {
			availableOutEdges = getMachine().getCurrentOutEdges();
		} catch (FoundNoEdgeException e) {
			throw new RuntimeException("No available edges found at " + getMachine().getCurrentVertexName(), e);
		}
		for (Edge edge : availableOutEdges) {
			Stack<Edge> path = new Stack<Edge>();
			path.push(edge);
			a_starPath.add(getWeightedPath(path));
		}
		double maxWeight = 0;
		while (a_starPath.size() > 0) {
			if (Thread.interrupted()) {
		    throw new InterruptedException();
			}

			WeightedPath path = (WeightedPath) a_starPath.poll();
			if (path.getWeight() > maxWeight)
				maxWeight = path.getWeight();
			if (path.getWeight() > 0.99999) // are we done yet?
				return path.getPath();

			Edge possibleDuplicate = (Edge) path.getPath().peek();

			// have we been here before?
			if (closed.contains(possibleDuplicate.hashCode() + "." + path.getSubState().hashCode() + "." + path.getWeight()))
				continue; // ignore this and move on

			// We don't want to use this edge again as this path is
			// the fastest, and if we come here again we have used more
			// steps to get here than we used this time.
			closed.add(possibleDuplicate.hashCode() + "." + path.getSubState().hashCode() + "." + path.getWeight());

			availableOutEdges = getPathOutEdges(path.getPath());
			if (availableOutEdges != null && availableOutEdges.size() > 0) {
				for (Edge edge : availableOutEdges) {
					Stack<Edge> newStack = (Stack<Edge>) path.getPath().clone();
					newStack.push(edge);
					a_starPath.add(getWeightedPath(newStack));
				}
			}
		}
		throw new RuntimeException("No path found to satisfy stop condition " + getStopCondition() + ", best path satified only "
		    + (int) (maxWeight * 100) + "% of condition.");
	}

	private WeightedPath getWeightedPath(Stack<Edge> path) {
		double weight = 0;
		String subState = "";

		getMachine().storeVertex();
		getMachine().walkPath(path);
		weight = getConditionFulfilment();
		String currentState = getMachine().getCurrentVertexName();
		if (currentState.contains("/")) {
			subState = currentState.split("/", 2)[1];
		}
		getMachine().restoreVertex();

		return new WeightedPath(path, weight, subState);
	}

	private Set<Edge> getPathOutEdges(Stack<Edge> path) {
		Set<Edge> retur = null;
		getMachine().storeVertex();
		getMachine().walkPath(path);
		try {
			retur = getMachine().getCurrentOutEdges();
		} catch (FoundNoEdgeException e) {
			// no edges found? degrade gracefully and return the default value of
			// null.
		}
		getMachine().restoreVertex();
		return retur;
	}

	/**
	 * Will reset the generator to its initial vertex.
	 */
	public void reset() {
		preCalculatedPath = null;
	}

	public String toString() {
		return "A_STAR{" + super.toString() + "}";
	}

	private class WeightedPath {
		private double weight;
		private Stack<Edge> path;
		private String subState;

		public String getSubState() {
			return subState;
		}

		public void setSubState(String subState) {
			this.subState = subState;
		}

		public Stack<Edge> getPath() {
			return path;
		}

		public void setPath(Stack<Edge> path) {
			this.path = path;
		}

		public double getWeight() {
			return weight;
		}

		public void setWeight(double weight) {
			this.weight = weight;
		}

		public WeightedPath(Stack<Edge> path, double weight, String subState) {
			setPath(path);
			setWeight(weight);
			setSubState(subState);
		}
	}
}