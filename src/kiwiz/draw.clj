(ns kiwiz.draw
  (:use kiwiz.grid))


;; returns the four points outside a given pad taking line width into account
;; goes from quadrant 1 (+,+) to quadrant 4 (+,-) ccw (increasing math angle)
(defn corners-outside-pad [pad line-width]
  (let [pos-xy (:pos-xy pad)
        width (:size-x pad)
        half-width (div-to-int width 2)
        height (:size-y pad)
        half-height (div-to-int height 2)
        half-line-width (div-to-int line-width 2)]
    (list
     (point-add pos-xy
                [(+ half-width) (+ half-height)]
                [(+ half-line-width) (+ half-line-width)])
     (point-add pos-xy
                [(- half-width) (+ half-height)]
                [(- half-line-width) (+ half-line-width)])
     (point-add pos-xy
                [(- half-width) (- half-height)]
                [(- half-line-width) (- half-line-width)])
     (point-add pos-xy
                [(+ half-width) (- half-height)]
                [(+ half-line-width) (- half-line-width)]))))
