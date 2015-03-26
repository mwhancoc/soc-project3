;rules for Seller Agents

;define templates for our message and quote content facts

;Send an ACL message
(deftemplate ACLMessage (slot communicative-act)(slot receiver)(slot sender)(slot itemID)(slot price (type FLOAT))(slot conversationID)(slot reply-with))

;Quote content
;(deftemplate quoteContent (slot itemId)(slot price (type FLOAT))) 

(defrule cfpProposeItem1
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'propose' message to the same sender and retract the just arrived message"
  ?m <- (ACLMessage {communicative-act == CFP && itemID == 1}  (communicative-act ?a) (receiver ?r) (sender ?s) (itemID ?i)(price ?p) (conversationID ?id))
 => 
(send (assert (ACLMessage (communicative-act PROPOSE) (receiver ?m.receiver) (sender ?m.sender) (itemID 1) (price 2.49) (conversationID ?m.conversationID))))
)

(defrule cfpProposeItem2
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'propose' message to the same sender and retract the just arrived message"
  ?m <- (ACLMessage {communicative-act == CFP && itemID == 2}  (communicative-act ?a) (receiver ?r) (sender ?s) (itemID ?i)(price ?p) (conversationID ?id))
 => 
(send (assert (ACLMessage (communicative-act PROPOSE) (receiver ?m.receiver) (sender ?m.sender) (itemID 2) (price 5.00) (conversationID ?m.conversationID))))
)

(defrule cfpREFUSE
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'reject' message to the same sender and retract the just arrived message"
  ?m <- (ACLMessage {communicative-act == CFP && itemID != 1 && itemID != 2}  (communicative-act ?a) (receiver ?r) (sender ?s) (itemID ?i)(price ?p) (conversationID ?id))
 => 
(send (assert (ACLMessage (communicative-act REFUSE) (receiver ?m.receiver) (sender ?m.sender) (itemID ?m.itemID) (price 1.00) (conversationID ?m.conversationID))))
)