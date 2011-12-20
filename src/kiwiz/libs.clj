(ns kiwiz.libs
  (:use kiwiz.grid kiwiz.module kiwiz.draw))

;;SOLDER PAD RECOMMENDATIONS FOR SURFACE-MOUNT DEVICE
;; http://www.ti.com/lit/an/sbfa015a/sbfa015a.pdf

;; http://tinymicros.com/mediawiki/images/3/3c/IPC-SM-782A.pdf
;; http://landpatterns.ipc.org/IPC-7351BNamingConvention.pdf


(defn footprint-sm [m-name length gap width]
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
       (binding [*grid-size* grid-size-small]
         (doall
          (let [points-around-pad-1 (cycle
                                     (map round-point-to-grid-outwards
                                          (corners-outside-pad
                                           (get-pad-by-name pads "1")
                                           *silkscreen-width*)))
                points-around-pad-2 (cycle
                                     (map round-point-to-grid-outwards
                                          (corners-outside-pad
                                           (get-pad-by-name pads "2")
                                           *silkscreen-width*)))]
            (concat
             (map (partial make-segment 21 *silkscreen-width*)
                  (take 3 points-around-pad-1)
                  (take 3 (drop 1 points-around-pad-1)))
             (map (partial make-segment 21 *silkscreen-width*)
                  (take 3 (drop 2 points-around-pad-2))
                  (take 3 (drop 3 points-around-pad-2)))))))
       pads
       (S3DMaster. "smd/chip_cms.wrl" 0.05))))) ;; FIXME


                                        ; IPC-SM-782

                                        ; 8.1 Chip resistors
(def IPC-resistors-chip
  (Library.
   (list
    (footprint-sm "RESC-1005-[0402]-IPC" (mm 2.20) (mm 0.40) (mm 0.70))
    (footprint-sm "RESC-1608-[0603]-IPC" (mm 2.80) (mm 0.60) (mm 1.00))
    (footprint-sm "RESC-2012-[0805]-IPC" (mm 3.20) (mm 0.60) (mm 1.50))
    (footprint-sm "RESC-3216-[1206]-IPC" (mm 4.40) (mm 1.20) (mm 1.80))
    (footprint-sm "RESC-3225-[1210]-IPC" (mm 4.40) (mm 1.20) (mm 2.70))
    (footprint-sm "RESC-5025-[2010]-IPC" (mm 6.20) (mm 2.60) (mm 2.70))
    (footprint-sm "RESC-6332-[2512]-IPC" (mm 7.40) (mm 3.80) (mm 3.20)))))