(ns de.levering-it.electric.three.example.simple
  (:require [hyperfiddle.electric3 :as e]
            [de.levering-it.electric.three :as three]
            [de.levering-it.electric.three.bindings :as tb]
            [de.levering-it.electric.three.utils :as tu]
            [de.levering-it.electric.three.impl.experimental.physics :refer [move-entity]]
            [hyperfiddle.electric-dom3 :as dom]
            [hyperfiddle.electric-forms3 :refer [Checkbox]]
            [clojure.string :as s]))



(e/defn Example []
  (e/client
    (three/WebGLRenderer
      (let [camera (three/PerspectiveCamera 75 (/ tb/view-port-width tb/view-port-height) 0.1 2000
                     (case (three/props {:position {:x 0 :y 1 :z 0}})
                       ; case syncing is needed because position has to be set
                       ; bevore lookAt is called
                       (.lookAt tb/node 1 0.5 0)))
            box (three/BoxGeometry 1 1 1)
            material (three/MeshBasicMaterial nil
                       (three/props {:color (three/Color 1 0 0)}))]
        (binding [tb/camera camera]
          (three/Scene
            (three/GridHelper 20 20)
            (three/Mesh box material
              (three/props {:position {:x 5 :y 0.5 :z 0}}))
            ))))))