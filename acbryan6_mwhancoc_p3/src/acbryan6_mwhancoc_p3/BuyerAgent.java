package acbryan6_mwhancoc_p3;

import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

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

}
