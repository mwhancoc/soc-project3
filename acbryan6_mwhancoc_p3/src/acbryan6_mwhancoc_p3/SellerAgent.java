package acbryan6_mwhancoc_p3;

import java.util.Hashtable;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class SellerAgent extends Agent{
	
	private Hashtable<Integer, Float> catalogue;
	
	protected void setup() {
		
		 // Register the seller agent service in the yellow pages
		 DFAgentDescription dfd = new DFAgentDescription();
		 dfd.setName(getAID());
		 ServiceDescription sd = new ServiceDescription();
		 sd.setType("seller");
		 sd.setName("seller-agent");
		 dfd.addServices(sd);
		 try {
			 DFService.register(this, dfd);
		 }
		 catch (FIPAException fe) {
			 fe.printStackTrace();
		 }
		
		// Create the catalogue, hard code two items, just to test ACLMessages
		 catalogue = new Hashtable<Integer, Float>();		 
		 catalogue.put(1, (float) 1.99);
		 catalogue.put(2,  (float) 1.49);
		
		 addBehaviour(new OfferRequestsServer());
		 
		 // Printout a welcome message
		 System.out.println("Hello! Seller-agent " + getAID().getName() + " is ready.");
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
			 Float price = (Float) catalogue.get(Integer.parseInt(itemID));
			 
			 /**
			  * Jade reason whether to propose a quote or reject the buyer
			  */
			 
			 if (price != null) {
				 // The requested book is available for sale. Reply with the price
				 reply.setPerformative(ACLMessage.PROPOSE);
				 reply.setContent(itemID + "," + String.valueOf(price.floatValue()));
				 System.out.println("recieved cfp, found price: " + price + " of item: " + itemID + " proposal");
			 }
			 else {
				 // The requested book is NOT available for sale.
				 reply.setPerformative(ACLMessage.REFUSE);
				 reply.setContent("not-available");
				 System.out.println("Refuse CFP!!!");
				 System.out.println("Item: " + itemID + " not found in catalogue");
			 }
			 myAgent.send(reply);
		 }
		 else{
			 block();
		 }
	 	}
	} // End of inner class OfferRequestsServer

	 
}
