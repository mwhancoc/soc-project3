package acbryan6_mwhancoc_p3;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.Behaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class BuyerAgent extends Agent{
	
	protected void setup() {
		 // Register the buyer agent service in the yellow pages
		 DFAgentDescription dfd = new DFAgentDescription();
		 dfd.setName(getAID());
		 ServiceDescription sd = new ServiceDescription();
		 sd.setType("buyer");
		 sd.setName("buyer-agent");
		 dfd.addServices(sd);
		 try {
			 DFService.register(this, dfd);
		 }
		 catch (FIPAException fe) {
			 fe.printStackTrace();
		 }
		 
		 addBehaviour(new TickerBehaviour(this, 60000) {
			 protected void onTick() {
				 myAgent.addBehaviour(new RequestPerformer());
			 	}
			 } );

		 
		 
		// Printout a welcome message
		 System.out.println("Hello! Buyer-agent " + getAID().getName() + " is ready.");
	 }

	// Put agent clean-up operations here
	protected void takeDown() {
		 // Deregister from the yellow pages
		 try {
			 DFService.deregister(this);
		 }
		 catch (FIPAException fe) {
			 fe.printStackTrace();
		 }
		// Printout a dismissal message
		 System.out.println("Buyer-agent " + getAID().getName() + " terminating.");
	}

	
	/**
	 Inner class RequestPerformer.
	 This is the behaviour used by Book-buyer agents to request seller
	 agents the target book.
	*/
	private class RequestPerformer extends Behaviour {
		 private AID bestSeller; // The agent who provides the best offer
		 private int bestPrice; // The best offered price
		 private int repliesCnt = 0; // The counter of replies from seller agents
		 private MessageTemplate mt; // The template to receive replies
		 private int step = 0;
		 
		 public void action() {
			 switch (step) {
			 case 0:
				 // Send the cfp to all sellers
				 ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				 /**
				  * code to look up a seller in the yellow pages
				  */
				 
				 //for (int i = 0; i < sellerAgents.length; ++i) {
					// cfp.addReceiver(sellerAgents[i]);
				 //}
				 cfp.setContent("1");
				 cfp.setConversationId("purchase-item");
				 cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
				 myAgent.send(cfp);
				 // Prepare the template to get proposals
				 mt = MessageTemplate.and(MessageTemplate.MatchConversationId("purchase-item"),
						 MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
				 step = 1;
				 break;
			 case 1:
				 // Receive all proposals/refusals from seller agents
				 ACLMessage reply = myAgent.receive(mt);
				 if (reply != null) {
					 // Reply received
					 if (reply.getPerformative() == ACLMessage.PROPOSE) {
						 // This is an offer
						 int price = Integer.parseInt(reply.getContent());
						 
						 /**
						  * Reason with Jess whether to accept quote or reject qoute
						  * 
						  * step = 2 if accept_proposal
						  * step = 3 if reject_proposal
						  */
						 
						 //if (bestSeller == null || price < bestPrice) {
							 // This is the best offer at present
						//	 bestPrice = price;
						//	 bestSeller = reply.getSender();
						 //}
					 }
					 
					 /**
					  * check if the ACLMessage is a rejection from the seller
					  */
					 
					 //repliesCnt++;
					 //if (repliesCnt >= sellerAgents.length) {
						 // We received all replies
					//	 step = 2;
					 //}
				 }
				 else {
		
					 block();
				 }
				 break;
			 case 2:
				 // Send the purchase order to the seller that provided the best offer
				 ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				 order.addReceiver(bestSeller);
				 order.setContent("1");
				 order.setConversationId("purchase-item");
				 order.setReplyWith("order" + System.currentTimeMillis());
				 myAgent.send(order);
				 // Prepare the template to get the purchase order reply
				 mt = MessageTemplate.and(MessageTemplate.MatchConversationId("purchase-item"),
						 MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				 step = 3;
				 break;
			 case 3:
				 
				 /**
				  * block to send a Reject_Proposal message
				  */
				 // Receive the purchase order reply
				 //reply = myAgent.receive(mt);
				 //if (reply != null) {
					 // Purchase order reply received
				//	 if (reply.getPerformative() == ACLMessage.INFORM) {
						 // Purchase successful. We can terminate
				//		 System.out.println("item: 1" + " successfully purchased.");
				//		 System.out.println("Price = " + bestPrice);
				//		 myAgent.doDelete();
				//	 }
				//	 step = 4;
				// }
				// else {
				//	 block();
				// }
				 break;
			 }
		 	}
		 
		 	public boolean done() {
		 		return ((step == 2 && bestSeller == null) || step == 4);
		 	}
	} // End of inner class RequestPerformer
}
