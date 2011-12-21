(ns kiwiz.core
  (:use kiwiz.module kiwiz.libs))

(defn write-library [[file library]]
  (spit file
        (with-out-str
          (doall
           (map println
                (flatten
                 (output library)))))))

(defn -main [& args]
  (map write-library IPC-libraries))
