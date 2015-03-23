;rules for both our Buyer and Seller Agents

;define templates for our message and quote content facts

;Send an ACL message
(deftemplate ACLMessage (slot communicative-act)(slot receiver)(slot sender)(slot content)(slot conversationID)(slot reply-with)) 

;Quote content
(deftemplate quoteContent (slot itemId)(slot price (type FLOAT))) 

(defrule cfp
 "When a 'cfp' message arrives from an agent ?s, this rule asserts a 
  'propose' message to the same sender and retract the just arrived message"
 ?m <- (ACLMessage (communicative-act CFP) (receiver ?r) (sender ?s) (content ?c) (conversationID ?id) (reply-with ?rw))
 =>
 ;(printout t ?m.receiver ?m.sender crlf)
(send (assert (ACLMessage (communicative-act CFP) (receiver ?m.receiver) (sender ?m.sender) (content ?m.content) (conversationID ?m.conversationID) (reply-with ?m.reply-with))))
;(assert (ACLMessage (communicative-act PROPOSE) (receiver ?m.receiver) (sender ?m.sender) (content ?m.content) (conversationID ?m.conversationID) (reply-with ?m.reply-with)))
(retract ?m)
)