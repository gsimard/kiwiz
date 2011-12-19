(ns kiwiz.core)

;; date +"%a %d %b %Y %r %Z"

(defprotocol Library-Printer
  "A procotol for printing footprints to a pcbnew library."
  (output [this] "Returns the string representation of this structure"))

(defrecord Library [modules]
  Library-Printer
  (output [this]
    (list
     "PCBNEW-LibModule-V1  Sun 18 Dec 2011 08:44:26 PM EST"
     "# encoding utf-8"
     "$INDEX"
     (map :name modules)
     "$EndINDEX"
     (map output modules)
     "$EndLIBRARY")))

(defrecord Module [name pads]
  Library-Printer
  (output [this]
    (list
     (str "$MODULE " name)
     (map output pads)
     (str "$EndMODULE " name))))

(defn write-library [file library]
  (spit file
        (with-out-str
          (doall
           (map println
                (flatten
                 (output library)))))))


(defn -main [& args]
  (write-library "my-lib.mod"
                 (Library.
                  (map map->Module
                       '({:name "M1" :pads nil}
                         {:name "M2" :pads nil})))))

(-main)
