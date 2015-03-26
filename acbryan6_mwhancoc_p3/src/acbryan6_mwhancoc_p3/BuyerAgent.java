package acbryan6_mwhancoc_p3;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.net.URISyntaxException;
import java.util.Iterator;

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
import jess.Context;
import jess.Funcall;
import jess.Jesp;
import jess.JessException;
import jess.RU;
import jess.Rete;
import jess.Userfunction;
import jess.Value;
import jess.ValueVector;

public class BuyerAgent extends Agent{
	private static String REQUESTED_ITEM = "2";	
	private static final long serialVersionUID = 1L;

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
		 
		 try{
		 addBehaviour(new RequestPerformer("Buyer.clp"));
		 }catch(Exception e)
		 {
			 System.out.println(e.getMessage());
		 
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

	
	/**
	 Inner class RequestPerformer.
	 This is the behaviour used by Book-buyer agents to request seller
	 agents the target book.
	*/
	public class RequestPerformer extends Behaviour {
		
		private static final long serialVersionUID = 1L;
		private AID seller; // The agent who provides the best offer		
		private MessageTemplate mt; // The template to receive replies
		private int step = 0;		 
		private String itemID;
		private Float price;		 
		private jess.Rete jess;
		 
		 public RequestPerformer(String jessFile)
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
		 
		 int item = 1;
		 
		 public void action() {
			 
			   // Then I add the send function
	            jess.addUserfunction(new JessSend(myAgent, this));
	            
			 switch (step) {
			 case 0:
				 // Send the cfp to all sellers
				 ACLMessage cfp = new ACLMessage(ACLMessage.CFP);
				// look up the seller agent
				 DFAgentDescription template = new DFAgentDescription();
				 ServiceDescription sd2 = new ServiceDescription();
				 sd2.setType("seller");
				 template.addServices(sd2);				 
				 AID sellerAgent = null;

				 /**
				  * code to look up a seller in the yellow pages
				  */
				 try {
				     DFAgentDescription[] result = DFService.search(myAgent, template);
				     sellerAgent= result[0].getName();
				 }

				 catch (FIPAException fe) {
					 fe.printStackTrace();
				 }
				
				 cfp.addReceiver(sellerAgent);
				 REQUESTED_ITEM = "" + item;
				 cfp.setContent(REQUESTED_ITEM);
				 cfp.setConversationId("purchase-item");				 
				 
				 System.out.println("\n################################################################\n");
				 System.out.println("Buyer sending CFP for item: " + item);
				 
				 myAgent.send(cfp);	// Decided to send the initial CFP through Jade/not Jess since there is no decision reasoning			 
				 //makeassert(ACL2JessString(cfp)); 
		
				 step = 1;
				 break;
			 case 1:
				 // Receive all proposals/refusals from seller agents
				 ACLMessage reply = myAgent.receive(mt);
				 if (reply != null) {
					 // Reply received
					 if (reply.getPerformative() == ACLMessage.PROPOSE) {
						 // This is an offer
						 String[] content = reply.getContent().split(":");
						 itemID = content[0];
						 price = Float.parseFloat(content[1]);
						 
						 System.out.println("Buyer recieved proposal from Seller for item: " + itemID + " with price: " + price);
						 seller = reply.getSender();
					
						 ACLMessage proposal = new ACLMessage(ACLMessage.REQUEST);
						 proposal.setPerformative(ACLMessage.REQUEST);							
						 proposal.addReceiver(seller);
						 proposal.setContent(itemID + ":" + price);
						 proposal.setConversationId("purchase-item");
						 
						 System.out.println("Deciding...");
						 
						 makeassert(ACL2JessString(proposal));
						 
						 try{
							 jess.run();
						 }catch(JessException re) {
					          re.printStackTrace(System.err);
					     }
						 
						 if(item++ < 3)
							 step = 0;
						 else
							 step = 4;						 
						
					 }
					 else if(reply.getPerformative() == ACLMessage.REFUSE) {
						 String[] content = reply.getContent().split(":");
						 itemID = content[0];
						 System.out.println("Seller REFUSED Buyer's request for item: " + itemID);
						 if(item++ < 3)
							 step = 0;
						 else
							 step = 4;	
					 }	
				 }
				 else {
		
					 block();
				 }
				 break;
			 case 2:

				 break;
			 case 3:			 
				
				 break;
			 }
		 	}
		 
		  public void makeassert(String fact) {
		        try {		        	
		            jess.executeCommand(fact);
		        } catch (Exception re) {
		            re.printStackTrace(System.err);
		        }		       
		    }
		 
		 	public AID getSellerAID()
		 	{
		 		return this.seller;
		 	}
		 	
		 	public boolean done() {
		 		return ((step == 2 && seller == null) || step == 4);
		 	}
		 	
		 	
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
				            String price = content[1];
				            //String price = content[1];
				            fact = fact + ") (itemID " + itemID + ") (price " + price + ") (conversationID purchase-item";
				  
					        fact = fact + ")))";					       

				        return fact;
				    }
		 	
		    public class JessSend implements Userfunction {
		        // data
		        Agent my_agent;
		        RequestPerformer rpb;

		        public JessSend(Agent a, RequestPerformer b) {
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
		    
	} // End of inner class RequestPerformer
}
