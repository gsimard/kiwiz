(ns kiwiz.module
  (:use kiwiz.grid kiwiz.units))


;; TODO: Revoir item_io.cpp L730 pour les autres attributs (soldermask etc.)

;; date +"%a %d %b %Y %r %Z"
(def ^:dynamic *footprint-library-header* "PCBNEW-LibModule-V1")


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
         (to-decimils-int (first pos-xy)) " "
         (to-decimils-int (second pos-xy)) " "
         (to-decimils-int size-y) " "
         (to-decimils-int size-x) " "
         orient " "
         (to-decimils-int thickness) " "
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
         (to-decimils-int (first start-xy)) " "
         (to-decimils-int (second start-xy)) " "
         (to-decimils-int (first end-xy)) " "
         (to-decimils-int (second end-xy)) " "
         (to-decimils-int width) " "
         layer)))

(defn make-segment [layer width start-xy end-xy]
  (Segment. layer width start-xy end-xy))

(defrecord Circle [layer width
                   start-xy
                   end-xy]
  Library-Printer
  (output [this]
    (str "DC "
         (to-decimils-int (first start-xy)) " "
         (to-decimils-int (second start-xy)) " "
         (to-decimils-int (first end-xy)) " "
         (to-decimils-int (second end-xy)) " "
         (to-decimils-int width) " "
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
         (to-decimils-int (first start-xy)) " "
         (to-decimils-int (second start-xy)) " "
         (to-decimils-int (first end-xy)) " "
         (to-decimils-int (second end-xy)) " "
         (int angle) " "
         (to-decimils-int width) " "
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
          (to-decimils-int (first start-xy)) " "
          (to-decimils-int (second start-xy)) " "
          (to-decimils-int (first end-xy)) " "
          (to-decimils-int (second end-xy)) " "
          (int (count points)) " "
          (to-decimils-int width) " "
          layer)
     (map
      (fn [x y] (str "Dl " (to-decimils-int x) " " (to-decimils-int y)))
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
          (to-decimils-int size-x) " "
          (to-decimils-int size-y) " "
          (to-decimils-int delta-x) " "
          (to-decimils-int delta-y) " "
          (int orientation))
     (str "Dr "
          (to-decimils-int drill-x) " "
          (to-decimils-int offset-x) " "
          (to-decimils-int offset-y)
          (if (= drill-shape 'O') (str " O " (to-decimils-int drill-x) " " (to-decimils-int drill-y))))
     (str "At " attribut " N " layer-mask)
     (str "Ne " net " " (escaped-utf8 net-name))
     (str "Po " (to-decimils-int (first pos-xy)) " " (to-decimils-int (second pos-xy)))
     "$EndPAD")))

(defn make-pad [shape size-x size-y delta-x delta-y
                drill-shape drill-x drill-y offset-x offset-y
                attribut layer-mask orientation net net-name
                m-name pos-xy]
  (Pad. shape size-x size-y delta-x delta-y
        drill-shape drill-x drill-y offset-x offset-y
        attribut layer-mask orientation net net-name
        m-name pos-xy))

(defn make-pad-smt [width height pad-name pos-xy]
  (Pad. "R" width height 0 0
        "C" 0 0 0 0
        "SMD" "00888000"
        0 0 ""
        pad-name pos-xy))

(defn get-pad-by-name [pads p-name]
  (first (filter #(= (:m-name %) p-name) pads)))


                                        ; MODULE::Write_3D_Descr
(defrecord S3DMaster [file scale orient]
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
     (str "Ro 0.000000 0.000000 " (format "%.6f" orient))
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
