(ns kiwiz.core
  (:use kiwiz.module kiwiz.libs)
  (:gen-class))

(defn write-library [path [file library]]
  (spit (str path "/" file)
        (with-out-str
          (doall
           (map println
                (flatten
                 (output library)))))))

(defn -main [& args]
  (let [dest (if (= 1 (count args))
               (first args)
               ".")]
    (dorun (map (partial write-library dest) IPC-libraries))
    (println (str "Libraries written to: " dest "/"))))
