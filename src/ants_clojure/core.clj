(ns ants-clojure.core
  (:require [clojure.java.io :as io])
  (:gen-class :extends javafx.application.Application))

(def width 800)
(def height 600)
(def antCount 100)

(def ants (atom []))

(defn createAnts []
  (for [i (range antCount)]
    {:x (rand-int width)
        :y (rand-int height)
     :color (javafx.scene.paint.Color/BLACK)
     :red? false}))

(defn drawAnts! [context]
  (.clearRect context 0 0 width height)
  (doseq [ant @ants]
    (.setFill context (:color ant))
    (.fillOval context (:x ant) (:y ant) 5 5)))

(defn randomStep []
  (- (* 2 (rand)) 1))

(defn moveAnt [ant]
  (Thread/sleep 1)
  (assoc ant
    :x (+ (randomStep) (:x ant))
    :y (+ (randomStep) (:y ant))))



(defn redder [ant]
  (Thread/sleep 1)
  (let [thing
        (filter (fn [a]
                  (and
                       (< (Math/abs (- (:x ant) (:x a))) 17)
                       (< (Math/abs (- (:y ant) (:y a))) 17)))
          @ants)
        cr-count (count thing)]
    (assoc ant :color
      (if (> cr-count 1)
          javafx.scene.paint.Color/RED
        javafx.scene.paint.Color/BLACK))))

(defn moveAnts []
  (doall (pmap redder (pmap moveAnt (deref @ants)))))


(def lastTimestamp (atom 0))

(defn fps [now]
  (let [diff (- now @lastTimestamp)
        diff-seconds (/ diff 1000000000)]
    (int (/ 1 diff-seconds))))


(defn -start [app stage]
  (let [root (javafx.fxml.FXMLLoader/load (io/resource "main.fxml"))
        scene (javafx.scene.Scene. root width height)
        canvas (.lookup scene "#canvas")
        context (.getGraphicsContext2D canvas)
        fps-label (.lookup scene "#fps")
        timer (proxy [javafx.animation.AnimationTimer] []
                (handle [now]
                  (.setText fps-label (str (fps now)))
                  (reset! lastTimestamp now)
                  (reset! ants (moveAnts))
                  (drawAnts! context)))]
      (.setTitle stage "Ants")
      (.setScene stage scene)
      (.show stage)
    (reset! ants (createAnts))
    (.start timer)))

(defn -main []
  (javafx.application.Application/launch ants_clojure.core (into-array String [])))
