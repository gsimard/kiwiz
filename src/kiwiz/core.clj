(ns kiwiz.core)
;; TODO: Revoir item_io.cpp L730 pour les autres attributs (soldermask etc.)

;; date +"%a %d %b %Y %r %Z"
(def ^:dynamic *footprint-library-header* "PCBNEW-LibModule-V1")

(def grid-size-smallest 5) ;; units are decimils (5 = 1/2 mil)
(def ^:dynamic *grid-size* grid-size-smallest)

(def silkscreen-width-small 50)
(def ^:dynamic *silkscreen-width* silkscreen-width-small)

(def text-size-small 250)
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


(defn escaped-utf8 [s]
  (str "\"" s "\""))

(defprotocol Library-Printer
  "A procotol for printing footprints to a pcbnew library."
  (output [this] "Returns the string representation of this structure"))


                                        ; PCB_BASE_FRAME::Save_Module_In_Library
(defrecord Library [modules]
  Library-Printer
  (output [this]
    (list
     (str *footprint-library-header* "  " "Sun 18 Dec 2011 08:44:26 PM EST")
     "# encoding utf-8"
     "$INDEX"
     (map :m-name modules)
     "$EndINDEX"
     (map output modules)
     "$EndLIBRARY")))


                                        ; TEXTE_MODULE::Save
;; size y and x really are in this order.
(defrecord TexteModule [typ pos-xy size-y size-x orient
                        thickness mirror noshow layer italic text]
  Library-Printer
  (output [this]
    (str "T" typ " "
         (first pos-xy) " "
         (second pos-xy) " "
         size-y " "
         size-x " "
         orient " "
         thickness " "
         (if mirror "M" "N") " "
         (if noshow "I" "V") " "
         layer " "
         (if italic "I" "N") " "
         (escaped-utf8 text))))

(defn make-text-reference [pos-xy text]
  (TexteModule. "0" pos-xy *text-size* *text-size* 0 *silkscreen-width* false false 21 false text))
(defn make-text-value [pos-xy text]
  (TexteModule. "1" pos-xy *text-size* *text-size* 0 *silkscreen-width* false false 21 false text))


                                        ; EDGE_MODULE::Save
(defrecord Segment [layer width
                    start-xy
                    end-xy]
  Library-Printer
  (output [this]
    (str "DS "
         (first start-xy) " "
         (second start-xy) " "
         (first end-xy) " "
         (second end-xy) " "
         width " "
         layer)))

(defn make-segment [layer width start-xy end-xy]
  (Segment. layer width start-xy end-xy))

(defrecord Circle [layer width
                   start-xy
                   end-xy]
  Library-Printer
  (output [this]
    (str "DC "
         (first start-xy) " "
         (second start-xy) " "
         (first end-xy) " "
         (second end-xy) " "
         width " "
         layer)))

(defn make-circle [layer width start-xy end-xy]
  (Circle. layer width start-xy end-xy))

(defrecord Arc [layer width
                start-xy
                end-xy
                angle]
  Library-Printer
  (output [this]
    (str "DA "
         (first start-xy) " "
         (second start-xy) " "
         (first end-xy) " "
         (second end-xy) " "
         angle " "
         width " "
         layer)))

(defn make-arc [layer width start-xy end-xy angle]
  (Arc. layer width start-xy end-xy angle))

(defrecord Polygon [layer width
                    start-xy
                    end-xy
                    points]
  Library-Printer
  (output [this]
    (list
     (str "DP "
          (first start-xy) " "
          (second start-xy) " "
          (first end-xy) " "
          (second end-xy) " "
          (count points) " "
          width " "
          layer)
     (map
      (fn [x y] (str "Dl " x " " y))
      points))))

(defn make-polygon [layer width start-xy end-xy points]
  (Polygon. layer width start-xy end-xy points))

(defrecord Pad [shape size-x size-y delta-x delta-y
                drill-shape drill-x drill-y offset-x offset-y
                attribut layer-mask orientation net net-name
                m-name pos-xy]
  Library-Printer
  (output [this]
    (list
     (str "$PAD")
     (str "Sh " (escaped-utf8 m-name) " "
          shape " "
          size-x " "
          size-y " "
          delta-x " "
          delta-y " "
          orientation)
     (str "Dr "
          drill-x " "
          offset-x " "
          offset-y
          (if (= drill-shape 'O') (str " O " drill-x " " drill-y)))
     (str "At " attribut " N " layer-mask)
     (str "Ne " net " " (escaped-utf8 net-name))
     (str "Po " (first pos-xy) " " (second pos-xy))
     "$EndPAD")))

(defn make-pad [shape size-x size-y delta-x delta-y
                drill-shape drill-x drill-y offset-x offset-y
                attribut layer-mask orientation net net-name
                m-name pos-xy]
  (Pad. shape size-x size-y delta-x delta-y
        drill-shape drill-x drill-y offset-x offset-y
        attribut layer-mask orientation net net-name
        m-name pos-xy))

(defn get-pad-by-name [pads p-name]
  (first (filter #(= (:m-name %) p-name) pads)))


                                        ; MODULE::Write_3D_Descr
(defrecord S3DMaster [file scale]
  Library-Printer
  (output [this]
    (list
     "$SHAPE3D"
     (str "Na " (escaped-utf8 file))
     (str "Sc "
          (format "%.6f" scale) " "
          (format "%.6f" scale) " "
          (format "%.6f" scale))
     "Of 0.000000 0.000000 0.000000"
     "Ro 0.000000 0.000000 0.000000"
     "$EndSHAPE3D")))


                                        ; MODULE::Save
(defrecord Module [m-name m-ref m-val drawings pads s3d]
  Library-Printer
  (output [this]
    (list
     (str "$MODULE " m-name)
     "Po 0 0 0 15 48A948E6 4EEE9403 ~~"
     (str "Li " m-name)
     "Sc 4EEE9403"
     "AR "
     "Op 0 0 0"
     (output m-ref)
     (output m-val)
     (map output drawings)
     (map output pads)
     (output s3d)
     (str "$EndMODULE  " m-name))))

;; returns the four points outside a given pad taking line width into account
;; goes from quadrant 1 (+,+) to quadrant 4 (+,-) ccw (increasing math angle)
(defn corners-outside-pad [pad line-width]
  (let [pos-xy (:pos-xy pad)
        width (:size-x pad)
        half-width (div-to-int width 2)
        height (:size-y pad)
        half-height (div-to-int height 2)
        half-line-width (div-to-int line-width 2)]
    (map round-point-to-grid-down
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
                     [(+ half-line-width) (- half-line-width)])))))

(defn footprint-sm [m-name length width gap]
  (let [pad-height (round-to-grid width)
        pad-width (round-to-grid (div-to-int (- length gap) 2))
        pad-offset (round-to-grid (div-to-int (+ pad-width gap) 2))]
    (let [pads (list
                (Pad. "R" pad-width pad-height 0 0
                      "C" 0 0 0 0
                      "SMD" "00888000"
                      0 0 ""
                      "1" [(- pad-offset) 0])
                (Pad. "R" pad-width pad-height 0 0
                      "C" 0 0 0 0
                      "SMD" "00888000"
                      0 0 ""
                      "2" [(+ pad-offset) 0]))]
      (Module.
       m-name
       (make-text-reference [0 0] m-name)
       (make-text-value [0 0] "VAL**")
       (let [points-around-pad-1 (cycle (corners-outside-pad
                                         (get-pad-by-name pads "1")
                                         *silkscreen-width*))
             points-around-pad-2 (cycle (corners-outside-pad
                                         (get-pad-by-name pads "2")
                                         *silkscreen-width*))]
         (concat
          (map (partial make-segment 21 *silkscreen-width*)
               (take 3 points-around-pad-1)
               (take 3 (drop 1 points-around-pad-1)))
          (map (partial make-segment 21 *silkscreen-width*)
               (take 3 (drop 2 points-around-pad-2))
               (take 3 (drop 3 points-around-pad-2)))))
       pads
       (S3DMaster. "smd/chip_cms.wrl" 0.05))))) ;; FIXME

(defn write-library [file library]
  (spit file
        (with-out-str
          (doall
           (map println
                (flatten
                 (output library)))))))

(defn -main [& args]
  (write-library "junk/my-lib.mod"
                 (Library.
                  (list
                   (footprint-sm "SM0805-GS" 1100 550 400)))))

;; (Module.
;;  "QFN24"
;;  (make-text-reference 0 -1500 "QFN24")
;;  (make-text-value 0 1500 "VAL**")
;;  (list
;;   (Segment. 0 0 100 100 79 21)
;;   (Segment. 100 100 200 100 59 21))
;;  (list
;;   (Pad. "O" 315 98 0 0
;;         "C" 0 0 0 0
;;         "SMD" "00888000"
;;         0 0 ""
;;         "1" [-787 -492]))
;;  (S3DMaster. "smd/qfn24.wrl" 1.0))
