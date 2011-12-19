(ns kiwiz.core)
;; TODO: Revoir item_io.cpp L730 pour les autres attributs (soldermask etc.)

;; date +"%a %d %b %Y %r %Z"
(def ^:dynamic *footprint-library-header* "PCBNEW-LibModule-V1")
(def ^:dynamic *lib-name* "my_lib")


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
(defrecord TexteModule [typ pos-x pos-y size-y size-x orient
                        thickness mirror noshow layer italic text]
  Library-Printer
  (output [this]
    (str "T" typ " "
         pos-x " "
         pos-y " "
         size-y " "
         size-x " "
         orient " "
         thickness " "
         (if mirror "M" "N") " "
         (if noshow "I" "V") " "
         layer " "
         (if italic "I" "N") " "
         (escaped-utf8 text))))

(defn make-text-reference [x y text]
  (TexteModule. "0" x y 394 394 0 99 false false 21 false text))
(defn make-text-value [x y text]
  (TexteModule. "1" x y 394 394 0 99 false false 21 false text))


                                        ; EDGE_MODULE::Save
(defrecord Segment [start-x start-y
                    end-x end-y
                    width layer]
  Library-Printer
  (output [this]
    (str "DS "
         start-x " "
         start-y " "
         end-x " "
         end-y " "
         width " "
         layer)))

(defrecord Circle [start-x start-y
                    end-x end-y
                    width layer]
  Library-Printer
  (output [this]
    (str "DC "
         start-x " "
         start-y " "
         end-x " "
         end-y " "
         width " "
         layer)))

(defrecord Arc [start-x start-y
                end-x end-y
                angle
                width layer]
  Library-Printer
  (output [this]
    (str "DA "
         start-x " "
         start-y " "
         end-x " "
         end-y " "
         angle " "
         width " "
         layer)))

(defrecord Polygon [start-x start-y
                    end-x end-y
                    points
                    width layer]
  Library-Printer
  (output [this]
    (list
     (str "DP "
          start-x " "
          start-y " "
          end-x " "
          end-y " "
          (count points) " "
          width " "
          layer)
     (map
      (fn [x y] (str "Dl " x " " y))
      points))))


                                        ; MODULE::Write_3D_Descr
(defrecord S3DMaster [file]
  Library-Printer
  (output [this]
    (list
     "$SHAPE3D"
     (str "Na " (escaped-utf8 file))
     "Sc 1.000000 1.000000 1.000000"
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
                   (Module.
                    "QFN24"
                    (make-text-reference 0 -1500 "QFN24")
                    (make-text-value 0 1500 "VAL**")
                    (list
                     (Segment. 0 0 100 100 79 21)
                     (Segment. 100 100 200 100 59 21))
                    nil
                    (S3DMaster. "smd/qfn24.wrl"))))))
