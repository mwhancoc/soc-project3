Team Members:

Andrew Bryan
acbryan6

Michael Hancock
mwhancoc

Section:
CSC 750 - 601

Special Instructions:

The below files must be in the same folder in order to successfuly run our project.

acbryan6_mwhancoc_p3.jar, jade.jar, jess.jar, jsr94.jar, Buyer.clp, Seller.clp, startProject3Platform.bat

Move to the "lib" folder in the unzipped archive file to locate the above files.

To run, execute the "startProject3Platform.bat".  when the jade.boot GUI opens up, first create a seller agent, then a buyer agent.
The seller agent must created before the buyer agent else the program will crash.  This is because the buyer agent uses the yellow pages (DFService)
to look up a seller agent to send a CFP message to.


Assumptions:

1.  Seller will always be created before the Buyer.  This is needed so the Buyer can look up the Seller to send the initial CFP ACLMessage.

2.  Buyer sends the initial CFP from Jade because there is no decision making for the first message.  
    All decisions are made by Jess after the initial CFP ACLMessage.


Thank You