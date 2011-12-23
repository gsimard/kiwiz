(ns kiwiz.libs
  (:use incanter.core)
  (:use kiwiz.grid kiwiz.units kiwiz.module kiwiz.draw)
  (:import [kiwiz.module Library Module Pad S3DMaster]))

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
       (S3DMaster. "smd/chip_cms.wrl" 0.05 0.0))))) ;; FIXME


(binding [*grid-size* grid-size-smallest]
  (let [X 4.2
        Y 10.2]
    (grid-syms [X Y]
               (+ X Y)
               (list X Y))))

(defn footprint-sot-23 [m-name Z G X Y C E]
  (grid-syms
   [Z G X Y C E]
   (let [X2 (round-to-grid ($= 2 * (E - X / 2)))]
     (let [pads (list
                 (make-pad-smt X Y "1" [($= (X + X2) / -2.0) ($= C / 2.0)])
                 (make-pad-smt X Y "2" [($= (X + X2) / 2.0) ($= C / 2.0)])
                 (make-pad-smt X2 Y "3" [0.0 ($= C / -2.0)]))]
       (Module.
        m-name
        (make-text-reference [0 0] m-name)
        (make-text-value [0 0] "VAL**")
        (draw-box 21 *silkscreen-width*
                  (stretch-box (div-to-int *silkscreen-width* 2)
                               (bounding-box (map corners-outside-pad pads))))
        pads
        (S3DMaster. "smd/SOT23_6.wrl" 0.11 -180.0))))))

                                        ; IPC-SM-782

                                        ; 8.1 Chip Resistors
(def IPC-resistors-chip
  (Library.
   (list
    (footprint-sm "RESC-1005-[0402]-IPC-100A" (mm 2.20) (mm 0.40) (mm 0.70))
    (footprint-sm "RESC-1608-[0603]-IPC-101A" (mm 2.80) (mm 0.60) (mm 1.00))
    (footprint-sm "RESC-2012-[0805]-IPC-102A" (mm 3.20) (mm 0.60) (mm 1.50))
    (footprint-sm "RESC-3216-[1206]-IPC-103A" (mm 4.40) (mm 1.20) (mm 1.80))
    (footprint-sm "RESC-3225-[1210]-IPC-104A" (mm 4.40) (mm 1.20) (mm 2.70))
    (footprint-sm "RESC-5025-[2010]-IPC-105A" (mm 6.20) (mm 2.60) (mm 2.70))
    (footprint-sm "RESC-6332-[2512]-IPC-106A" (mm 7.40) (mm 3.80) (mm 3.20)))))

                                        ; 8.2 Chip Capacitors
(def IPC-capacitors-chip
  (Library.
   (list
    (footprint-sm "CAPC-1005-[0402]-IPC-130A" (mm 2.20) (mm 0.40) (mm 0.70))
    (footprint-sm "CAPC-1310-[0504]-IPC-131A" (mm 2.40) (mm 0.40) (mm 1.30))
    (footprint-sm "CAPC-1608-[0603]-IPC-132A" (mm 2.80) (mm 0.60) (mm 1.00))
    (footprint-sm "CAPC-2012-[0805]-IPC-133A" (mm 3.20) (mm 0.60) (mm 1.50))
    (footprint-sm "CAPC-3216-[1206]-IPC-134A" (mm 4.40) (mm 1.20) (mm 1.80))
    (footprint-sm "CAPC-3225-[1210]-IPC-135A" (mm 4.40) (mm 1.20) (mm 2.70))
    (footprint-sm "CAPC-4532-[1812]-IPC-136A" (mm 5.80) (mm 2.00) (mm 3.40))
    (footprint-sm "CAPC-4564-[1825]-IPC-137A" (mm 5.80) (mm 2.00) (mm 6.80)))))

                                        ; 8.6 SOT 23
(def IPC-SOT-23
  (Library.
   (list
    (footprint-sot-23 "RLP-210" (mm 3.60) (mm 0.80) (mm 1.00) (mm 1.40) (mm 2.20) (mm 0.95)))))


(def IPC-libraries
  (list
   (list "resistors-chip.mod" IPC-resistors-chip)
   (list "capacitors-chip.mod" IPC-capacitors-chip)
   (list "sot-23.mod" IPC-SOT-23)))

;; (kiwiz.core/-main "junk")
