(ns kiwiz.units
  (import java.lang.Math))

(defn unit? [u]
  (and
   (map? u)
   (every? u [:type :kind :val])))

;; units in .mod files are decimils
(defn decimils [u]
  {:pre [(or (number? u) (unit? u))]}
  (let [v (if (unit? u)
            (:val (nm u))
            u)]
    {:type :unit :kind :decimils :val v}))

(defn decimils? [u])

;; converts mils to decimils
(defn mils [n]
  {:type :unit :kind :mils :val n})

;; converts inches to decimils
(defn inches [n]
  {:type :unit :kind :inches :val n})

;; converts mm to decimils
(defn mm [n]
  {:type :unit :kind :mm :val n})

;; the nm is the smallest unit to which all
;; other are brought back to for a conversion
(defn nm [u]
  {:pre [(or (number? u) (unit? u))]}
  (let [n (if (unit? u)
            (cond
             (= :decimils (:kind u)) (* (:val u) 2540)
             (= :mils (:kind u)) (* (:val u) 25400)
             (= :inches (:kind u)) (* (:val u) 25400000)
             (= :mm (:kind u)) (* (:val u) 1000000)
             true 0)
            u)]
    {:type :unit :kind :nm :val (int n)}))

(nm (mm 34))

;; 1 dmil / 10000 dmils/inch * 0.0254 m/inch * 1e9 nm/m = 2540 nm/dmil
;; 1 mil / 1000 mils/inch * 0.0254 m/inch * 1e9 nm/m = 25400 nm/mil
;; 1 inch * 0.0254 m/inch * 1e9 nm/m = 25400000 nm/inch
;; 1 mm / 1000 mm/m * 1e9 nm/m = 1e6 nm/mm
