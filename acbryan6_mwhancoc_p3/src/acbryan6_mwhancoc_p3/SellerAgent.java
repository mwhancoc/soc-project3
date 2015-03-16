package acbryan6_mwhancoc_p3;

import java.util.Hashtable;

import jade.core.Agent;

public class SellerAgent extends Agent{
	
	private Hashtable catalogue;
	
	protected void setup() {
		
		// Create the catalogue, hard code two items, just to test ACLMessages
		 catalogue = new Hashtable();
		 catalogue.put(1, 1.99);
		 catalogue.put(2,  2.99);
		
		 // Printout a welcome message
		 System.out.println("Hello! Seller-agent " + getAID().getName() + " is ready.");
	 }
	
	 // Put agent clean-up operations here
	 protected void takeDown() {
		 // Printout a dismissal message
		 System.out.println("Seller-agent " + getAID().getName() + " terminating.");
	 }


}
