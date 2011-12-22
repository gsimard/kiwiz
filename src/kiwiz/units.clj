(ns kiwiz.units
  (import java.lang.Math))


;; base unit is decimils
(defn decimils [n]
  {:type :unit :kind :decimils :val n})

;; converts mils to decimils
(defn mils [n]
  (decimils (Math/round (* 10.0 n))))

;; converts inches to decimils
(defn inches [n]
  (decimils (Math/round (* 10000.0 n))))

;; converts mm to decimils
(defn mm [n]
  (decimils (Math/round (* (/ n 25.4) 10000.0))))

(defn nm [n]
  {:type :unit :kind :decimils :val (int n)}