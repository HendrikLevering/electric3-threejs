(ns de.levering-it.electric.three.example
  (:require [hyperfiddle.electric3 :as e]
            [hyperfiddle.electric-dom3 :as dom]
            [de.levering-it.electric.three.example.world3d :as world3d]
            [de.levering-it.electric.three.example.simple :as simple]))



(e/defn Examples []
  (e/client

    (let [examples {"3d World" (e/fn [] (world3d/Example))
                    "Simple" (e/fn [] (simple/Example))}
          selected (dom/div
                     (dom/label (dom/text "Select Example: "))
                     (dom/select
                       (e/for [label (e/diff-by first (keys examples))]
                         (dom/option (dom/props {:value label}) (dom/text label)))
                       (dom/On "change" #(-> % .-target .-value) (-> examples keys first))))]
      (when (not (nil? selected))
        (dom/div
          (dom/props {:style {:height "50vh"}})
          (e/$ (examples selected)))))))