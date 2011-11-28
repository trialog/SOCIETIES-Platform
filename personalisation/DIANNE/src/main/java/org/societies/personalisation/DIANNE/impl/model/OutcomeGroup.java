package org.societies.personalisation.DIANNE.impl.model;

import java.util.Iterator;

import org.societies.personalisation.common.api.model.ServiceResourceIdentifier;

public class OutcomeGroup extends Group
{
	private OutcomeNode winnerNode;
	private OutcomeNode previousWinner;
	private ServiceResourceIdentifier serviceId;
	private String serviceType;

	//gradient variables
	private double gradient;
	private double step;
	private double upperBound;
	private double lowerBound;

	//boosting variables
	private OutcomeNode boost;
	private OutcomeNode unboost;

	public OutcomeGroup(String groupName){
		super(groupName);
		step = 10;
		upperBound = step;
		lowerBound = -step;
		gradient = 2/(upperBound-lowerBound);
		boost = null;
		unboost = null;
		winnerNode = null;
	}
	
	public OutcomeGroup(ServiceResourceIdentifier serviceId, String serviceType, String groupName){
		super(groupName);
		this.serviceId = serviceId;
		this.serviceType = serviceType;
		step = 10;
		upperBound = step;
		lowerBound = -step;
		gradient = 2/(upperBound-lowerBound);
		boost = null;
		unboost = null;
		winnerNode = null;
	}

	/*
	 * Over-ride inherited method
	 */
	public void addNode(OutcomeNode node)
	{
		node.initialiseGradient(gradient);
		groupNodes.add(node);
		if(groupNodes.size()<2)//if first node added
		{
			winnerNode = node;
		}
	}

	public void userActivateNode(OutcomeNode node){
		activateNode(node);
	}

	/*
	 * Context change methods
	 */
	public void refreshOutcomes() //User input so calculate highest potential and activate
	{
		if(inputAvailable())
		{
			updateNodePotentials();  //calculate new potentials
			activateHighestPotential();  //get node with highest potential and activate
			checkGradient();  //check group gradient
		}
	}

	private void updateNodePotentials()
	{
		Iterator<Node> groupNodes_it = groupNodes.iterator();
		while(groupNodes_it.hasNext())
		{
			OutcomeNode nextNode = (OutcomeNode)groupNodes_it.next();
			nextNode.calculatePotential();
		}
	}

	private void activateHighestPotential()
	{
		OutcomeNode highestPotentialNode = getHighestPotentialNode();
		activateNode(highestPotentialNode);
	}

	public boolean inputAvailable()
	{
		boolean input = false;
		if(!groupNodes.isEmpty())
		{
			//check if first node in group has synapses
			if(groupNodes.get(0).getSynapses().size()>0) 
			{
				input = true;
			}
		}
		return input;
	}


	/* 
	 * Reinforcement cycle methods
	 */
	public void updateGroupOutput()
	{
		if(inputAvailable())
		{
			updateNodePotentials();  //calculate new potentials
			calculateWinnerNode();  //get winner node
			checkGradient();  //check group gradient

			//check for conflicts
			if(conflicts())  //conflicts have occurred
			{
				resolveConflicts();
			}else{ //no conflicts
				boost = null;
				unboost = null;
				//check if we have a new winner node
				if(!winnerNode.equals(previousWinner))  //new winner node
				{
					//alert services to new network output
					sendOutput();
				}
			}
		}
	}	

	private void calculateWinnerNode() //find winner and activate if new
	{
		//get node with the highest potential
		OutcomeNode highestPotentialNode = getHighestPotentialNode();

		//set as the winner node
		previousWinner = winnerNode;
		winnerNode = highestPotentialNode;
	}

	private boolean conflicts()
	{
		if(!winnerNode.equals(activeNode))
		{
			return true;
		}else{
			return false;
		}
	}

	public void sendOutput()
	{
		System.out.println("******DIANNE sending new outcome: "+winnerNode.getGroupName()+" = "+winnerNode.getNodeName());
	}


	/*
	 *  Gradient methods
	 */
	private void checkGradient()
	{
		double highPotential = winnerNode.getPotential();
		double lowPotential = getLoserPotential();

		if(highPotential >= 1 || lowPotential <= -1)
		{
			//potentials about to saturate
			//increase gradient
			upperBound = upperBound+step;
			lowerBound = lowerBound-step;

			gradient = 2/(upperBound-lowerBound);

			//alert nodes of new gradient
			setGroupGradient();
		}
	}

	private double getLoserPotential()
	{
		double loserPot;

		Iterator<Node> groupNodes_it = groupNodes.iterator();

		OutcomeNode firstNode = (OutcomeNode)groupNodes_it.next();
		loserPot = firstNode.getPotential();

		while(groupNodes_it.hasNext())
		{
			OutcomeNode nextNode = (OutcomeNode)groupNodes_it.next();

			if(loserPot > nextNode.getPotential())
			{
				loserPot = nextNode.getPotential();
			}
		}

		return loserPot;
	}

	private void setGroupGradient()
	{
		Iterator<Node> groupNodes_it = groupNodes.iterator();
		while(groupNodes_it.hasNext())
		{
			OutcomeNode nextNode = (OutcomeNode)groupNodes_it.next();
			nextNode.setGradient(gradient);
		}
	}


	/*
	 * Conflict resolution methods
	 */
	public void resolveConflicts()
	{

		//check if ongoing conflict
		if(!winnerNode.equals(unboost) || !activeNode.equals(boost))
		{
			//new conflict
			boost = (OutcomeNode)activeNode;
			unboost = winnerNode;

			resolve();
		}
	}

	private void resolve()
	{
		double winnerNode_p = winnerNode.getPotential();
		double activeNode_p = ((OutcomeNode)activeNode).getPotential();
		double delta_p = winnerNode_p - activeNode_p;  //difference in potentials
		double step_value = ((delta_p)*(delta_p))/2; //get step value
		double activeNode_p_prime = activeNode_p + step_value; //calculate new sigmoid_potential of active node and set
		((OutcomeNode)activeNode).setPotential(activeNode_p_prime);
		//calculate weight update value
		double activeNode_raw = activeNode_p / gradient;
		double activeNode_raw_prime = activeNode_p_prime/gradient;
		double activeNode_raw_delta = Math.abs(activeNode_raw_prime - activeNode_raw);
		((OutcomeNode)activeNode).boost(activeNode_raw_delta);
	}

	/*
	 * Helper methods
	 */
	private OutcomeNode getHighestPotentialNode()
	{
		double highestPot;
		OutcomeNode highestPotNode;

		Iterator<Node> groupNodes_it = groupNodes.iterator();

		OutcomeNode firstNode = (OutcomeNode)groupNodes_it.next();
		highestPot = firstNode.getPotential();
		highestPotNode = firstNode;

		while(groupNodes_it.hasNext())
		{
			OutcomeNode nextNode = (OutcomeNode)groupNodes_it.next();

			if(highestPot < nextNode.getPotential())
			{
				highestPot = nextNode.getPotential();
				highestPotNode = nextNode;
			}
		}
		return highestPotNode;
	}


	/* 
	 * Getter methods
	 */
	public OutcomeNode getWinnerNode()
	{
		return winnerNode;
	}
	
	public ServiceResourceIdentifier getServiceId(){
		return serviceId;
	}
	
	public String getServiceType(){
		return serviceType;
	}

	public double getGradient(){
		return this.gradient;
	}

	public void setGradient(double gradient){
		this.gradient = gradient;
	}
}
