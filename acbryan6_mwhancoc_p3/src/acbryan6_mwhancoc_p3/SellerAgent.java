package acbryan6_mwhancoc_p3;

import jade.core.Agent;

public class SellerAgent extends Agent{
	
	protected void setup() {
		 // Printout a welcome message
		 System.out.println("Hello! Seller-agent " + getAID().getName() + " is ready.");
	 }
	
	 // Put agent clean-up operations here
	 protected void takeDown() {
		 // Printout a dismissal message
		 System.out.println("Seller-agent " + getAID().getName() + " terminating.");
	 }


}
