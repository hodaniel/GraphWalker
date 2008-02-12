package org.tigris.mbt.generators;

import java.util.Iterator;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.tigris.mbt.FiniteStateMachine;
import org.tigris.mbt.conditions.StopCondition;

public class CombinedPathGenerator extends PathGenerator {

	static Logger logger = Logger.getLogger(CombinedPathGenerator.class);

	private Vector paths = new Vector();
	private int currentGenerator = 0;
	
	public CombinedPathGenerator() {
		super();
	}
	
	public CombinedPathGenerator(PathGenerator generator) {
		super();
		addPathGenerator(generator);
	}
	
	public void addPathGenerator(PathGenerator generator)
	{
		logger.debug("Adding PathGenerator: " + generator);
		paths.add(generator);
	}

	public void setMachine(FiniteStateMachine machine) {
		for(Iterator i = paths.iterator();i.hasNext();)
		{
			((PathGenerator)i.next()).setMachine(machine);
		}
	}
	
	public void setStopCondition(StopCondition stopCondition) {
		for(Iterator i = paths.iterator();i.hasNext();)
		{
			((PathGenerator)i.next()).setStopCondition(stopCondition);
		}
	}
	
	private PathGenerator getActivePathGenerator()
	{
		return (PathGenerator) paths.get(currentGenerator);
	}

	private boolean hasPath()
	{
		return paths.size() > currentGenerator;
	}
	
	private void scrapActivePathGenerator()
	{
		logger.debug("Removing PathGenerator: " + getActivePathGenerator());
		currentGenerator++;
	}
	
	public boolean hasNext() 
	{
		boolean nextIsAvailable = false;
		while(hasPath() && !nextIsAvailable)
		{
			nextIsAvailable = getActivePathGenerator().hasNext();
			if(!nextIsAvailable) scrapActivePathGenerator();
		}
		return nextIsAvailable;
	}
	
	public String[] getNext() {
		String[] retur = {"",""};

		boolean nextIsAvailable = false;
		while(hasPath() && !nextIsAvailable)
		{
			nextIsAvailable = getActivePathGenerator().hasNext();
			if(!nextIsAvailable) scrapActivePathGenerator();
		}
		if(!nextIsAvailable) return retur;
		return getActivePathGenerator().getNext();
	}
	
	
	public String toString() {
		String retur = "";
		for(Iterator i = paths.iterator();i.hasNext();)
		{
			retur +=((PathGenerator)i.next()).toString() + "\n";
		}
		return retur.trim();
	}

}
