package acbryan6_mwhancoc_p3;

import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SellerAgent extends Agent{
	
	private Hashtable catalogue;
	
	protected void setup() {
		
		// Create the catalogue, hard code two items, just to test ACLMessages
		 catalogue = new Hashtable();
		 catalogue.put(1, 1.99);
		 catalogue.put(2,  2.99);
		
		 addBehaviour(new OfferRequestsServer());
		 
		 // Printout a welcome message
		 System.out.println("Hello! Seller-agent " + getAID().getName() + " is ready.");
	 }
	
	 // Put agent clean-up operations here
	 protected void takeDown() {
		 // Printout a dismissal message
		 System.out.println("Seller-agent " + getAID().getName() + " terminating.");
	 }

	 /**
	 Inner class OfferRequestsServer.
	 This is the behaviour used by Book-seller agents to serve incoming requests
	 for requestForQuote from buyer agents.
	 If the requested item is in the local catalogue the seller agent replies
	 with a PROPOSE message specifying the item and price. Otherwise a REFUSE message is
	 sent back denying the request.
	*/
	private class OfferRequestsServer extends CyclicBehaviour {
	 public void action() {
		 
		 // look for CFP messages
		 MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		 
		 ACLMessage msg = myAgent.receive(mt);
		 if (msg != null) {
			 // Message received. Process it
			 String itemID = msg.getContent();
			 ACLMessage reply = msg.createReply();
			 Integer price = (Integer) catalogue.get(itemID);
			 if (price != null) {
				 // The requested book is available for sale. Reply with the price
				 reply.setPerformative(ACLMessage.PROPOSE);
				 reply.setContent(String.valueOf(price.intValue()));
			 }
			 else {
				 // The requested book is NOT available for sale.
				 reply.setPerformative(ACLMessage.REFUSE);
				 reply.setContent("not-available");
			 }
			 myAgent.send(reply);
		 }
		 else{
			 block();
		 }
	 	}
	} // End of inner class OfferRequestsServer

	 
}
