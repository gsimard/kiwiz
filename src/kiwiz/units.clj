(ns kiwiz.units
  (import java.lang.Math))

;; 1 dmil / 10000 dmils/inch * 0.0254 m/inch * 1e9 nm/m = 2540 nm/dmil
;; 1 mil / 1000 mils/inch * 0.0254 m/inch * 1e9 nm/m = 25400 nm/mil
;; 1 inch * 0.0254 m/inch * 1e9 nm/m = 25400000 nm/inch
;; 1 mm / 1000 mm/m * 1e9 nm/m = 1e6 nm/mm

;; the nm is the smallest unit to which all
;; other are brought back to for a conversion
(defn nm [n]
  {:pre [(number? n)]}
  (int n))
(defn to-nm [n]
  {:pre [(number? n)]}
  (int n))

(defn mm [n]
  {:pre [(number? n)]}
  (* n 1000000))
(defn to-mm [n]
  {:pre [(number? n)]}
  (/ n 1000000))

;; decimils are the units used in .mod files
(defn decimils [n]
  {:pre [(number? n)]}
  (* n 2540))
(defn to-decimils [n]
  {:pre [(number? n)]}
  (/ n 2540))
(defn to-decimils-int [n]
  {:pre [(number? n)]}
  (int (/ n 2540)))

(defn mils [n]
  {:pre [(number? n)]}
  (* n 25400))
(defn to-mils [n]
  {:pre [(number? n)]}
  (/ n 25400))

(defn inches [n]
  {:pre [(number? n)]}
  (* n 25400000))
(defn to-inches [n]
  {:pre [(number? n)]}
  (/ n 25400000))
