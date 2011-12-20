(ns kiwiz.units
  (import java.lang.Math))


;; base unit is decimils
(defn decimils [n]
  (int n))

;; converts mils to decimils
(defn mils [n]
  (int (Math/round (* 10.0 n))))

;; converts inches to decimils
(defn inches [n]
  (int (Math/round (* 10000.0 n))))

;; converts mm to decimils
(defn mm [n]
  (int (Math/round (* (/ n 25.4) 10000.0))))
