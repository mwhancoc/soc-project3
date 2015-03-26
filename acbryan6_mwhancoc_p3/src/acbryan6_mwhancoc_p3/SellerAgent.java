package acbryan6_mwhancoc_p3;

import java.io.File;
import java.io.FileReader;
import java.util.Hashtable;
import java.util.Iterator;

import acbryan6_mwhancoc_p3.BuyerAgent.RequestPerformer;
import acbryan6_mwhancoc_p3.BuyerAgent.RequestPerformer.JessSend;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jess.Context;
import jess.Funcall;
import jess.Jesp;
import jess.JessException;
import jess.RU;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

public class SellerAgent extends Agent{	
	
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
		
		 addBehaviour(new OfferRequestsServer("Seller.clp"));
		 
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
		
		 private jess.Rete jess;
		 
		 public OfferRequestsServer(String jessFile)
		 {
			 
		    try {		    	
		    	jess = new jess.Rete();
		    	
		    	File jarFile;
		    	File clpFile = null;
				try {
					jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
					System.out.println("Loading " + jarFile.getParentFile().getAbsolutePath() + "\\" + jessFile);
					clpFile = new File(jarFile.getParentFile().getAbsolutePath() + "\\" + jessFile);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}		    	
		    	
		    	  // Open the file test.clp
	            FileReader fr = new FileReader(clpFile);

	            // Create a parser for the file, telling it where to take input
	            // from and which engine to send the results to
	            jess.Jesp j = new Jesp(fr, jess);
		    	
	            // Then I add the send function
	            //jess.addUserfunction(new JessSend(myAgent, this));

	            // parse and execute one construct, without printing a prompt
	            j.parse(false);
	        
	        } catch (Exception e) {
	            System.out.println(e);
	        }
	       
		 }

	 public void action() {
		 
		  // Then I add the send function
         jess.addUserfunction(new JessSend(myAgent, this));
		 
		 // look for CFP messages
		 MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
		 
		 try{
			 jess.run();
		 }catch(JessException re) {
	          re.printStackTrace(System.err);
	     }
	
		 ACLMessage msg = myAgent.receive();
		 if (msg != null) {
			
			 // If the message is a Call for Proposal
			 if(msg.getPerformative() == ACLMessage.CFP) {
				 // Message received. Process it				
				String itemID = msg.getContent();
				ACLMessage reply = msg.createReply();
				reply.setPerformative(ACLMessage.CFP);
				reply.setContent(msg.getContent());
				System.out.println("Seller received CFP from buyer for Item: " + itemID);
				 
				 /**
				  * Jade reason whether to propose a quote or reject the buyer
				  */
				 makeassert(ACL2JessString(reply));
			}
			 // If the Buyer accepted the Seller's proposal
			 else if(msg.getPerformative() == ACLMessage.ACCEPT_PROPOSAL) {
				 String[] content = msg.getContent().split(":");
				 String itemID = content[0];
				 String price = content[1];
				 System.out.println("Buyer ACCEPTED Seller's proposal for item: " + itemID + " at price: " + price);
			 }
			 // If the Buyer rejected the Seller's proposal
			 else if(msg.getPerformative() == ACLMessage.REJECT_PROPOSAL) {
				 String[] content = msg.getContent().split(":");
				 String itemID = content[0];
				 String price = content[1];
				 System.out.println("Buyer REJECTED Seller's proposal for item: " + itemID + " at price: " + price);
			 }
		 }
		 else{			 
			 block();
		 }
	 	}
	 
	 public void makeassert(String fact) {
	        try {	        	
	            jess.executeCommand(fact);
	        } catch (Exception re) {
	            re.printStackTrace(System.err);
	        }	       
	    }
	 
	 public class JessSend implements Userfunction {
	        // data
	        Agent my_agent;
	        OfferRequestsServer rpb;

	        public JessSend(Agent a, OfferRequestsServer b) {
	            my_agent = a;
	            rpb = b;
	        }

	        // The name method returns the name by which the function appears in Jess
	        public String getName() {
	            return ("send");
	        }

	        //Called when (send ...) is encountered
	        public Value call(ValueVector vv, Context context)
	            throws JessException {
	           
	            //////////////////////////////////
	            // Case where JESS calls (send ?m)
	            if (vv.get(1).type() == RU.VARIABLE) {
	                // Uncomment for JESS 5.0 vv =  context.getEngine().findFactByID(vv.get(1).factIDValue(context));
	                vv = context.getEngine().findFactByID(vv.get(1)
	                                                        .factValue(context)
	                                                        .getFactId()); //JESS6.0
	            }
	            //////////////////////////////////
	            // Case where JESS calls (send (assert (ACLMessage ...)))
	            else if (vv.get(1).type() == RU.FUNCALL) {	            	
	                Funcall fc = vv.get(1).funcallValue(context);
	                vv = fc.get(1).factValue(context);
	            }

	            ACLMessage msg = rpb.JessFact2ACL(context, vv);
	            msg.setSender(my_agent.getAID());
	            my_agent.send(msg);
	            return Funcall.TRUE;
	        }
	    } // end JessSend class
	    
	    public ACLMessage JessFact2ACL(Context context, jess.ValueVector vv)
		        throws jess.JessException {
		        
		        int perf = ACLMessage.getInteger(vv.get(0).stringValue(context));
		        ACLMessage msg = new ACLMessage(perf);
		        msg.addReceiver(new AID(vv.get(1).stringValue(context)));
		        
		        msg.setSender(new AID(vv.get(2).stringValue(context)));		 
		        msg.setContent(vv.get(3).stringValue(context)  + ":" + vv.get(4).stringValue(context));
		        msg.setConversationId(vv.get(5).stringValue(context));
		       
		        return msg;
		    }

		    public String ACL2JessString(ACLMessage msg) {
		        String fact;

		        if (msg == null) {
		            return "";
		        }

		        // I create a string that asserts the template fact
		        fact = "(assert (ACLMessage (communicative-act " +
		            ACLMessage.getPerformative(msg.getPerformative());

		        Iterator iter = msg.getAllReceiver();
		        
		        while(iter.hasNext()){
		        	AID aid = (AID) iter.next();
		        	fact = fact + ") (receiver " + aid.getName();
		        }
		       
		            fact = fact + ") (sender " + myAgent.getAID().getName();		          

		            String[] content = msg.getContent().split(":");
		            String itemID = content[0];
		            //String price = content[1];
		            fact = fact + ") (itemID " + itemID + ") (price " + 0 + ") (conversationID purchase-item";
		            fact = fact + ")))";

		        return fact;
		    }
	 
	} // End of inner class OfferRequestsServer
}
