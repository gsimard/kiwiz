(ns kiwiz.draw
  (:use kiwiz.grid))


;; returns the four points outside a given pad taking line width into account
;; goes from quadrant 1 (+,+) to quadrant 4 (+,-) ccw (increasing math angle)
(defn corners-outside-pad [pad]
  (let [pos-xy (:pos-xy pad)
        width (:size-x pad)
        half-width (div-to-int width 2)
        height (:size-y pad)
        half-height (div-to-int height 2)]
    (list
     (point-add pos-xy [(+ half-width) (+ half-height)])
     (point-add pos-xy [(- half-width) (+ half-height)])
     (point-add pos-xy [(- half-width) (- half-height)])
     (point-add pos-xy [(+ half-width) (- half-height)]))))

(defn bounding-box [points]
  (let [x-min (min (map first points))
        x-max (max (map first points))
        y-min (min (map second points))
        y-max (max (map second points))]
    (list
     [x-max y-max]
     [x-min y-max]
     [x-min y-min]
     [x-max y-min])))

(defn stretch-box [delta [p1 p2 p3 p4]]
  (list
   (point-add p1 [(+ delta) (+ delta)])
   (point-add p2 [(- delta) (+ delta)])
   (point-add p3 [(- delta) (- delta)])
   (point-add p4 [(+ delta) (- delta)])))

(defn draw-box [layer width [p1 p2 p3 p4]]
  (list
   (make-segment layer width p1 p2)
   (make-segment layer width p2 p3)
   (make-segment layer width p3 p4)
   (make-segment layer width p4 p1)))
