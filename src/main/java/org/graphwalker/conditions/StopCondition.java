package org.graphwalker.conditions;

import org.graphwalker.machines.FiniteStateMachine;

public abstract class StopCondition {
	protected FiniteStateMachine machine;

	public FiniteStateMachine getMachine() {
		return machine;
	}

	public void setMachine(FiniteStateMachine machine) {
		this.machine = machine;
	}

	/**
	 * @return true if the condition is fulfilled
	 */
	public abstract boolean isFulfilled();

	/**
	 * @return the condition fulfilment
	 */
	public abstract double getFulfilment();
}
