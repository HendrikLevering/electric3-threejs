(ns de.levering-it.three.utils
  (:require [hyperfiddle.electric3 :as e]
            [hyperfiddle.electric-dom3 :as dom]
            [hyperfiddle.electric-dom3-events :as events]
            [missionary.core :as m]
            [contrib.missionary-contrib :as mx]))

(def deg2rad (/ Math/PI 180))
(def deg90 (* deg2rad 90))
(def deg-90 (* deg2rad -90))
(def deg180 (* deg2rad 180))

;; keystates
#?(:cljs (defn squarewave [node enter-event leave-event init]
           (->> (mx/mix
                  (m/observe (fn [!] (events/with-listener node enter-event (fn [e] (! {(.-key e) true})))))
                  (m/observe (fn [!] (events/with-listener node leave-event (fn [e] (! {(.-key e) false}))))))
             (m/reductions {} init) (m/relieve {}))))

(e/defn KeyDown?
  ([] (KeyDown? dom/node))
  ([node] (e/client (e/input (squarewave node "keydown" "keyup" {})))))

(e/defn KeyState []
  (e/client
    (let [a! (atom {})]
      (swap! a! merge (KeyDown?))
      (e/watch a!))))

(defn with-t->dt [f]
  (let [last! (volatile! nil)]
    (fn [e & args]
      (when-not @last!
        (vreset! last! e))
      (apply f (let [dt (- e @last!)]
                 (vreset! last! e)
                 dt) args))))