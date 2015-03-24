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
	

	/**
	 * 
	 */
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
		 
		 
		
		 
		 //addBehaviour(new TickerBehaviour(this, 60000) {
		//	 protected void onTick() {
		//		 myAgent.addBehaviour(new RequestPerformer());
		//	 	}
		//	 } );
		 System.out.println("adding behavior");
		 try{
		 addBehaviour(new RequestPerformer("jessAgent.clp"));
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
		 /**
		 * 
		 */
		private static final long serialVersionUID = 1L;
		private AID seller; // The agent who provides the best offer
		 private int bestPrice; // The best offered price
		 private int repliesCnt = 0; // The counter of replies from seller agents
		 private MessageTemplate mt; // The template to receive replies
		 private int step = 0;
		 
		 private String itemID;
		 private Float price;
		 
		 private jess.Rete jess;
		 
		 public RequestPerformer(String jessFile)
		 {
			 
		    try {
		    	System.out.println("in constructor");
		    	jess = new jess.Rete();
	            // First I define the ACLMessage template
	            //rete.executeCommand(ACLJessTemplate());

	            // Then I define the myagent template
	            //rete.executeCommand("(deftemplate MyAgent (slot name))");
		    	
		    	
		    	File jarFile;
		    	File clpFile = null;
				try {
					jarFile = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().getPath());
					System.out.println(jarFile.getParentFile().getAbsolutePath() + "\\" + jessFile);
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

				 try {

				     DFAgentDescription[] result = DFService.search(myAgent, template);

				     sellerAgent= result[0].getName();

				 }

				 catch (FIPAException fe) {

				 fe.printStackTrace();
				 }
				
				 cfp.addReceiver(sellerAgent);
				 
				 /**
				  * code to look up a seller in the yellow pages
				  */
				 
				 //for (int i = 0; i < sellerAgents.length; ++i) {
					// cfp.addReceiver(sellerAgents[i]);
				 //}
				 cfp.setContent("2");
				 cfp.setConversationId("purchase-item");
				 cfp.setReplyWith("cfp" + System.currentTimeMillis()); // Unique value
				 //myAgent.send(cfp);
				 System.out.println("makeassert");
				 makeassert(ACL2JessString(cfp));
				 
				 
				 try{
					 jess.run();
				 }catch(JessException re) {
			          re.printStackTrace(System.err);
			     }
				 
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
						 String[] content = reply.getContent().split(",");
						 itemID = content[0];
						 price = Float.parseFloat(content[1]);
						 
						 System.out.println("recieve message from seller with price: " + price);
						 seller = reply.getSender();
						 if(price > 1.50){
							 step = 2;
						 }else
						 {
							 step = 3; 
						 }
						 
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
					 else if(reply.getPerformative() == ACLMessage.REFUSE) {
						 System.out.println("Seller rejected buyer's request");
						 step = 4;
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
				 ACLMessage acceptProposal = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
				 acceptProposal.addReceiver(seller);
				 acceptProposal.setContent(itemID + "," + price);
				 acceptProposal.setConversationId("purchase-item");
				 acceptProposal.setReplyWith("order" + System.currentTimeMillis());
				 //myAgent.send(acceptProposal);
				 makeassert(ACL2JessString(acceptProposal));

				 System.out.println("accepted proposal from seller");
				 // Prepare the template to get the purchase order reply
				 //mt = MessageTemplate.and(MessageTemplate.MatchConversationId("purchase-item"),
				//		 MessageTemplate.MatchInReplyTo(order.getReplyWith()));
				 step = 4;
				 break;
			 case 3:
				 
				 /**
				  * block to send a Reject_Proposal message
				  */
				 // Send the purchase order to the seller that provided the best offer
				 ACLMessage rejectProposal = new ACLMessage(ACLMessage.REJECT_PROPOSAL);
				 rejectProposal.addReceiver(seller);
				 rejectProposal.setContent(itemID + "," + price);
				 rejectProposal.setConversationId("purchase-item");
				 rejectProposal.setReplyWith("order" + System.currentTimeMillis());
				 //myAgent.send(rejectProposal);
				 makeassert(ACL2JessString(rejectProposal));
				 
				 System.out.println("rejected proposal from seller");
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
				 step = 4;
				 break;
			 }
		 	}
		 
		  public void makeassert(String fact) {
		        try {
		        	System.out.println(fact);
		            jess.executeCommand(fact);
		        } catch (Exception re) {
		            re.printStackTrace(System.err);
		        }
		        
		        // if blocked, wake up!
		        //if(!isRunnable()) restart();
		        // message asserted
		        //return true;
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
		        // System.err.println("JessFact2ACL "+vv.toString());
		        int perf = ACLMessage.getInteger(vv.get(0).stringValue(context));
		        ACLMessage msg = new ACLMessage(perf);
		        System.out.println("******** Sender ********* " + vv.get(2).stringValue(context));

		       		        
		        msg.addReceiver(new AID(vv.get(1).stringValue(context)));
		        //msg.addReceiver(seller);
		        msg.setSender(new AID(vv.get(2).stringValue(context)));
		        msg.setContent(vv.get(3).stringValue(context));
		        msg.setConversationId(vv.get(4).stringValue(context));
		        //msg.setReplyWith("msg" + System.currentTimeMillis());
		        msg.setReplyWith(vv.get(5).stringValue(context));
		        //msg.addReplyTo(myAgent.getAID());
		        
		
		        //System.out.println("JessFact2ACL: " + msg.toString());
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
		        //if (msg.getSender() != null) {
		            //fact = fact + ") (receiver " + msg.getAllReceiver();
		            //putAIDInCache(msg.getSender());
		        //}
		            fact = fact + ") (sender " + myAgent.getAID().getName();
		            fact = fact + ") (content 2) (conversationID purchase-item";
	

		        //if (!isEmpty(msg.getReplyWith())) {
		            fact = fact + ") (reply-with " + msg.getReplyWith();
		        //}
		  
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
		            //for (int i=0; i<vv.size(); i++) {
		            //  System.out.println(" parameter " + i + "=" + vv.get(i).toString() +
		            //   " type=" + vv.get(i).type());
		            //  }
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
		            	System.out.println("this is send RU.FUNCALL");
		                Funcall fc = vv.get(1).funcallValue(context);
		                vv = fc.get(1).factValue(context);
		            }

		            ACLMessage msg = rpb.JessFact2ACL(context, vv);
		        
		            System.out.println("in send func/Java that Jess calls" + " " + msg.toString());
		            
		            msg.setSender(my_agent.getAID());
		           
		            my_agent.send(msg);

		            return Funcall.TRUE;
		        }
		    } // end JessSend class
		    
	} // End of inner class RequestPerformer
}
