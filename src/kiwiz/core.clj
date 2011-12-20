(ns kiwiz.core
  (:use kiwiz.module kiwiz.libs))

(defn write-library [file library]
  (spit file
        (with-out-str
          (doall
           (map println
                (flatten
                 (output library)))))))

(defn -main [& args]
  (write-library "junk/resistors-chip.mod" IPC-resistors-chip))
