;rules for Buyer Agents

;define templates for our message and quote content facts

;Send an ACL message
(deftemplate ACLMessage (slot communicative-act)(slot receiver)(slot sender)(slot itemID)(slot price (type FLOAT))(slot conversationID)(slot reply-with))

;Quote content
;(deftemplate quoteContent (slot itemId)(slot price (type FLOAT))) 

(defrule offerAccepted
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'propose' message to the same sender and retract the just arrived message"
  ?m <- (ACLMessage {communicative-act == REQUEST && price <= 4.50 }  (communicative-act ?a) (receiver ?r) (sender ?s) (itemID ?i)(price ?p) (conversationID ?id))
 => 
(send (assert (ACLMessage (communicative-act ACCEPT-PROPOSAL) (receiver ?m.receiver) (sender ?m.sender) (itemID ?m.itemID) (price ?m.price) (conversationID ?m.conversationID))))
)

(defrule offerRejected
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'reject' message to the same sender and retract the just arrived message"
  ?m <- (ACLMessage {communicative-act == REQUEST && price > 4.50 } (communicative-act ?a) (receiver ?r) (sender ?s) (itemID ?i)(price ?p) (conversationID ?id))
 => 
(send (assert (ACLMessage (communicative-act REJECT-PROPOSAL) (receiver ?m.receiver) (sender ?m.sender) (itemID ?m.itemID) (price ?m.price) (conversationID ?m.conversationID))))
)