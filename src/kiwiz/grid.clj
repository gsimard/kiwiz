(ns kiwiz.grid
  (:use kiwiz.units))


(def grid-size-smallest (decimils 5)) ;; units are decimils (5 = 1/2 mil)
(def grid-size-small (mils 5))
(def ^:dynamic *grid-size* grid-size-smallest)

(def silkscreen-width-small (mils 5))
(def ^:dynamic *silkscreen-width* silkscreen-width-small)

(def text-size-small (mils 25))
(def ^:dynamic *text-size* text-size-small)

                                        ; Grid and point related operations
(defn div-to-int [& args]
  (int (apply / args)))

(defn point-add [& args]
  [(apply + (map first args))
   (apply + (map second args))])

;; ie.: 30 remains 30, but 29 does down to 25
(defn round-to-grid-down [n]
  (- n (mod n *grid-size*)))

;; ie.: 30 remains 30, but 31 goes to 35
(defn round-to-grid-up [n]
  (let [n (dec n)]
  (+ n *grid-size* (- (mod n *grid-size*)))))

(defn round-to-grid [n]
  (round-to-grid-down n))

(defn round-point-to-grid-down [[x y]]
  [(round-to-grid-down x)
   (round-to-grid-down y)])

(defn round-point-to-grid-up [[x y]]
  [(round-to-grid-up x)
   (round-to-grid-up y)])

(defn round-point-to-grid [xy]
  (round-point-to-grid-down [xy]))

(defn round-point-to-grid-outwards [[x y]]
  [(if (>= x 0)
     (round-to-grid-up x)
     (round-to-grid-down x))
   (if (>= y 0)
     (round-to-grid-up y)
     (round-to-grid-down y))])

(defn round-point-to-grid-inwards [[x y]]
  [(if (< x 0)
     (round-to-grid-up x)
     (round-to-grid-down x))
   (if (< y 0)
     (round-to-grid-up y)
     (round-to-grid-down y))])
