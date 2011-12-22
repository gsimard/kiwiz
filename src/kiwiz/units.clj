(ns kiwiz.units
  (import java.lang.Math))

(defn unit? [u]
  (and
   (map? u)
   (every? u [:type :kind :val])))

(defn unit-cast [u kind factor]
  {:pre [(or (number? u) (unit? u))]}
  (let [v (if (unit? u)
            (/ (:val (nm u)) factor)
            u)]
    {:type :unit :kind kind :val v}))

(defn unit-test [u kind]
  (and
   (unit? u)
   (= :decimils (:kind u))))

;; units in .mod files are decimils
(defn decimils [u]
  (unit-cast u :decimils 2540))
(defn decimils? [u]
  (unit-test u :decimils))

;; converts mils to decimils
(defn mils [u]
  (unit-cast u :mils 25400))
(defn mils? [u]
  (unit-test u :mils))

;; converts inches to decimils
(defn inches [u]
  (unit-cast u :inches 25400000))
(defn mils? [u]
  (unit-test u :inches))

;; converts mm to decimils
(defn mm [u]
  (unit-cast u :mm 1000000))
(defn mm? [u]
  (unit-test u :mm))

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
(defn nm? [u]
  (unit-test u :nm))

;; 1 dmil / 10000 dmils/inch * 0.0254 m/inch * 1e9 nm/m = 2540 nm/dmil
;; 1 mil / 1000 mils/inch * 0.0254 m/inch * 1e9 nm/m = 25400 nm/mil
;; 1 inch * 0.0254 m/inch * 1e9 nm/m = 25400000 nm/inch
;; 1 mm / 1000 mm/m * 1e9 nm/m = 1e6 nm/mm

(decimils? (decimils (mm 34)))

(nm (mm 34))

(nm (mils 1))

(mils (mm 1))

(nm 121.9)
