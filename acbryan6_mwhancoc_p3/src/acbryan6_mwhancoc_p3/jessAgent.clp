;rules for both our Buyer and Seller Agents

;define templates for our message and quote content facts

;Send an ACL message
(deftemplate ACLMessage (slot performative)(slot receiver)(slot name)(slot conversationID)(slot quoteContent)) 

;Quote content
(deftemplate quoteContent (slot itemId)(slot price (type FLOAT))) 