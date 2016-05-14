(ns logic-tutorial.tut1
  (:refer-clojure :exclude [==])
  (:use [clojure.core.logic])
  (:use [clojure.core.logic.pldb]))

(db-rel parent x y)
(db-rel male x)
(db-rel female x)

(defn child [x y]
  (parent y x))

(defn son [x y]
  (all
   (child x y)
   (male x)))

(defn daughter [x y]
  (all
   (child x y)
   (female x)))

(defn grandparent [x y]
  (fresh [z]
    (parent x z)
    (parent z y)))

(defn granddaughter [x y]
  (fresh [z]
    (daughter x z)
    (child z y)))



(db-rel man x)

;; And then we want to define some men:

(def men
  (db
    [man 'Bob]
    [man 'John]))

;; Now we can ask who are men. Questions are always asked with `run` or `run*`.
;; By convention we'll declare a logic variable `q` and ask the computer to give
;; use the possible values for `q`. Here's an example.

(with-db men
   (run 1 [q] (man q)))

;; We're asking the computer to give us at least one answer to the question - "Who is a man?".
;; We can ask for more than one answer:

(with-db men
  (run 2 [q] (man q)))

;; Now that is pretty cool. What happens if we ask for even more answers?

(with-db men
  (run 3 [q] (man q)))

;; The same result. That's because we’ve only told the computer that two men
;; exist in the world. It can't give results for things it doesn't know about.
;; Let's define another kind of relationship and a fact:

(db-rel fun x)

(def fun-people
  (db
    [fun 'Bob]))

;; Let's ask a new kind of question:

(with-dbs [men fun-people]
  (run* [q]
    (man q)
    (fun q)))

;; There's a couple of new things going on here. We use `run*`. This means we want
;; all the answers the computer can find. The question itself is formulated
;; differently than before because we're asking who is a man *and* is fun. Enter in
;; the following:

(db-rel woman x)

(def facts
  (db
   [woman 'Lucy]
   [woman 'Mary]))

(db-rel likes x y)

;; We have now switched to a more generic name for the database of 'facts', which
;; we will expand with facts about different relations. Relations don't have to be
;; about a single entity. We can define relationship between things!

(def facts
  (-> facts
    (db-fact likes 'Bob 'Mary)
    (db-fact likes 'John 'Lucy)))

(with-dbs [men facts] (run* [q] (likes 'Bob q)))

;; We've added two facts to the 'facts' database and can now ask who likes who!

;; However, let's try this:

(with-dbs [men facts]
  (run* [q]
    (likes 'Mary q)))

;; Hmm that doesn't work. This is because we never actually said *who Mary liked*,
;; only that Bob liked Mary. Try the following:

(def facts
  (db-fact facts likes 'Mary 'Bob))

(with-dbs [men facts]
  (run* [q]
    (fresh [x y]
      (likes x y)
      (== q [x y]))))

;; Wow that's a lot of new information. The fresh expression isn't something we've
;; seen before. Why do we need it? By convention `run` returns single values for
;; `q` which answer the question. In this case we want to know who likes who. This
;; means we need to create logic variables to store these values in. We then assign
;; both these values to `q` by putting them in a Clojure vector (which is like an
;; array in other programming languages).

;; Try the following:

(with-dbs [men facts]
  (run* [q]
    (fresh [x y]
      (likes x y)
      (likes y x)
      (== q [x y]))))

;; Here we only want the list of people who like each other. Note that the order of
;; how we pose our question doesn't matter:

(with-dbs [men facts]
  (run* [q]
    (fresh [x y]
      (likes x y)
      (== q [x y])
      (likes y x))))


;; Some Genealogy
;; --------------

(def genealogy
  (db
    [parent 'John 'Bobby]
    [male 'Bobby]))

(with-db genealogy
  (run* [q]
    (son q 'John)))

;; Let's add another fact:

(def genealogy
  (-> genealogy
    (db-fact parent 'George 'John)))

(with-db genealogy
  (run* [q] (grandparent q 'Bobby)))

(defn sibling [x y]
  (fresh [z]
    (all
     (parent x z)
     (parent y z)
     (!= x y))))

(defn brother [x y]
  (all
   (sibling x y)
   (male y)))

(defn sister [x y]
  (all
   (sibling x y)
   (female y)))

(def family
  (db
   [male 'Jack]
   [female 'Julie]
   [male 'Steve]
   [female 'Jenifer]
   [parent 'Julie 'Steve]
   [parent 'Jack 'Steve]))

(with-db family
  (run* [q] (sibling 'Julie q)))

(with-db family
  (run* [q] (brother 'Julie q)))

(with-db family
  (run* [q] (sister 'Jack q)))


(run* [q] (== q 5))
(run* [q] (== q 5) (== q 4))
(run* [q] (fresh [x y] (== [x 2] [1 y]) (== q [x y])))

(run* [q] (fresh [x y] (== x y) (== q [x y])))
(run* [q] (fresh [x y] (== x y) (== y 1) (== q [x y])))

(with-dbs [facts fun-people] (run* [q] (fun q) (likes q 'Mary)))

(with-dbs [family] (run* [q] (female q) (sibling 'Jack q)))

(with-dbs [facts fun-people]
  (run* [q]
    (conde
     ((fun q))
     ((likes q 'Mary)))))



