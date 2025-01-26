(ns de.levering-it.electric.three.impl.experimental.impl
  (:require [hyperfiddle.electric3 :as e]
            [hyperfiddle.electric-dom3 :as dom]
            #?(:cljs ["three" :as three])
            [missionary.core :as m]
            [de.levering-it.electric.three.bindings :as tbp]
            [cljs.core :as c])
  #?(:cljs (:require-macros [de.levering-it.electric.three.impl.experimental.impl])))

; internal bindings
(e/declare obj-port)
(e/declare rerender-flag)
(e/declare render-token)

#?(:cljs (defn interop-js
           ([^js cls]                                  #?(:cljs (new cls)))
           ([^js cls a#]                               #?(:cljs (new cls a#)))
           ([^js cls a# b#]                            #?(:cljs (new cls a# b#)))
           ([^js cls a# b# c#]                         #?(:cljs (new cls  a# b# c#)))
           ([^js cls a# b# c# d#]                      #?(:cljs (new cls a# b# c# d#)))
           ([^js cls a# b# c# d# e#]                   #?(:cljs (new cls a# b# c# d# e#)))
           ([^js cls a# b# c# d# e# f#]                #?(:cljs (new cls a# b# c# d# e# f#)))
           ([^js cls a# b# c# d# e# f# g#]             #?(:cljs (new cls a# b# c# d# e# f# g#)))
           ([^js cls a# b# c# d# e# f# g# h#]          #?(:cljs (new cls a# b# c# d# e# f# g# h#)))
           ([^js cls a# b# c# d# e# f# g# h# i#]       #?(:cljs (new cls a# b# c# d# e# f# g# h# i#)))
           ([^js cls a# b# c# d# e# f# g# h# i# j#]    #?(:cljs (new cls a# b# c# d# e# f# g# h# i# j#)))
           ([^js cls a# b# c# d# e# f# g# h# i# j# k#] #?(:cljs (new cls a# b# c# d# e# f# g# h# i# j# k#)))))

#?(:cljs (defn setlistener [^js obj val]
           (set! (.-listeners  obj) val)))

#?(:cljs (defn dispose [^js obj]
           (.dispose obj)))

(deftype WrappedResource [obj])

(defn refresh-resource [obj]
  (new WrappedResource  obj))

#?(:cljs (defn unwrap [arg]
           (if (instance? WrappedResource arg) (.-obj arg) arg)))

(defmacro set-prop-fn [path obj]
  `(fn [val#]

     (set! (.. ~obj ~@path) val#)
     #_(reset! obj-port (refresh-resource ~obj))))

(defmacro unmount-prop [fn]
  `(new (m/observe (fn [!#] (!# nil) ~fn))))

(defn flatten-props
  ([m] (flatten-props m []))
  ([m p]
   (if (map? m)
     (mapcat
       (fn [[k v]]
         (flatten-props v (conj p k))) m)
     [[p m]])))

#?(:cljs (defn create [ctor c-args]
           (let [c-args (map unwrap c-args)]
             (apply interop-js ctor c-args))))

(e/defn ResourceObjFullReactive [ctor c-args body]
  (let [obj (e/client (create ctor (e/$ c-args)))
        obj! (e/client (atom (new WrappedResource obj)))]
    (binding [obj-port obj!]
      (e/$ body))
    (e/client (e/on-unmount #(do (dispose obj))))
    (e/client (e/watch obj!))))

(e/defn ResourceObj [ctor c-args body]
  (let [obj (e/client (create ctor (e/$ c-args)))]
    (binding [tbp/node obj]
      (e/$ body))
    (e/client (e/on-unmount #(dispose obj)))
    obj))

(defn request-rerender [port _]
  (reset! port (new WrappedResource nil)))

#?(:cljs (defn remove-from-parent [^js obj]
           (.removeFromParent obj)))

(e/defn SceneGraphObjFullReactive [ctor c-args body]
  (let [obj (e/client (create ctor (e/$ c-args)))
        obj! (e/client (atom (new WrappedResource obj)))]
    (request-rerender rerender-flag (e/watch obj!))
    (.add tbp/node obj)
    (e/client
      (e/on-unmount #(do
                       (remove-from-parent obj)
                       (dispose obj)
                       (request-rerender rerender-flag nil))))
    (binding [obj-port obj!
              tbp/node obj]

      (request-rerender rerender-flag (e/watch obj!))
      (e/$ body))))

(e/defn SceneGraphObj [ctor c-args body]
  (let [obj (e/client (create ctor (e/$ c-args)))]
    (.add tbp/node obj)
    (e/client
      (e/on-unmount #(do
                       (remove-from-parent obj)
                       (dispose obj))))
    (binding [tbp/node obj]
      (e/$ body))))

(defn resource-obj*
  ([ctor c-args forms]
   (let [s (symbol ctor)]
     `(e/$ ResourceObj  ~s (e/fn [] [~@(take c-args forms)]) (e/fn [] (do ~@(drop c-args forms)))))))

(defn scene-obj*
  ([ctor c-args forms]
   (let [s (symbol ctor)]
     `(e/$ SceneGraphObj  ~s (e/fn [] [~@(take c-args forms)]) (e/fn [] (do ~@(drop c-args forms)))))))

(defn size> [node]
  #?(:cljs (->> (m/observe (fn [!]
                             (! (-> node .getBoundingClientRect))
                             (let [resize-observer (js/ResizeObserver. (fn [[nd] _]
                                                                         (! (-> nd .-target .getBoundingClientRect))))]
                               (.observe resize-observer node)
                               #(.disconnect resize-observer))))
             (m/relieve {}))))

(defn -size [rect]
  #?(:cljs [(.-width rect) (.-height rect)]))

#?(:cljs (defn setPixelRatio [^js obj]
           (.setPixelRatio obj (.-devicePixelRatio js/window))))

#?(:cljs (defn render [^js renderer ^js scene ^js camera]
           (.updateProjectionMatrix camera)
           (.render renderer scene camera)))

(e/defn WebGLRenderer*FullReactive [Body]
  (let [!rerender-flag (e/client (atom nil))
        render-token (e/client (e/watch !rerender-flag))
        renderer (e/client (interop-js three/WebGLRenderer (clj->js {:antialias true})))
        elem (e/client (.-domElement renderer))
        w-h (e/client (-size (e/input (size> dom/node))))
        width (e/client (first w-h))
        height (e/client (second w-h))
        obj-port! (e/client (atom (new WrappedResource renderer)))]
    (e/client (.appendChild  dom/node elem)
      (.setSize renderer width height true)
      (request-rerender !rerender-flag (e/watch obj-port!))
      (setPixelRatio renderer)
      (e/on-unmount #(do
                       (dispose renderer)
                       (some-> (.-parentNode elem) (.removeChild elem)))))
    (binding [rerender-flag !rerender-flag
              tbp/renderer (e/watch obj-port!)
              obj-port obj-port!
              render-token render-token
              tbp/view-port-width width
              tbp/view-port-height height
              dom/node elem]
      (Body))))

(e/defn WebGLRenderer* [Body]
  (let [renderer (e/client (interop-js three/WebGLRenderer (clj->js {:antialias true})))
        elem (e/client (.-domElement renderer))
        w-h (e/client (-size (e/input (size> dom/node))))
        width (e/client (first w-h))
        height (e/client (second w-h))]
    (e/client (.appendChild  dom/node elem)
      (.setSize renderer width height true)
      (setPixelRatio renderer)
      (e/on-unmount #(do
                       (dispose renderer)
                       (some-> (.-parentNode elem) (.removeChild elem)))))
    (binding [tbp/renderer renderer
              tbp/view-port-width width
              tbp/view-port-height height
              dom/node elem]
      (Body))))

#?(:cljs (defn update-camera [^js camera]
           (.updateProjectionMatrix camera)))

#?(:cljs (defn render-scene [renderer scene camera token]
           (when token
             (if (vector? camera)
               (doseq [c  camera]
                 (if (instance? three/CubeCamera c)
                   (do
                     (.update c renderer scene))
                   (do
                     (update-camera c)
                     (render renderer scene c))))
               (do (if (instance? three/CubeCamera camera)
                     (.update camera renderer scene)
                     (do
                       (update-camera camera)
                       (render renderer scene camera))))))))

(e/defn Scene* [Body]
  (let [scene (e/client (interop-js three/Scene))]
    (e/on-unmount #(dispose scene))
    (binding [tbp/node scene]
      (let [r (Body)]
        (e/client
          (render-scene tbp/renderer scene tbp/camera (e/System-time-ms)))
        r))))

(e/defn Scene*FullReactive [Body]
  (let [scene (e/client (interop-js three/Scene))
        obj-port! (e/client (atom (new WrappedResource scene)))]
    (e/on-unmount #(dispose scene))
    (binding [obj-port obj-port!
              tbp/node scene]
      (let [r (Body)]
        (e/client (render-scene tbp/renderer (e/watch obj-port!) tbp/camera render-token))
        r))))

(defn props*FullReactive [m]
  (let [obj-sym (gensym "obj")]
    `(let [~obj-sym (unwrap @obj-port)]
       ~@(map (fn [[k v]]
                (let [path (map #(symbol (str "-" (name %))) k)]
                  `(let [org-val# (.. ~obj-sym ~@path)]
                     ((set-prop-fn ~path ~obj-sym) ~v)
                     (unmount-prop #((set-prop-fn ~path ~obj-sym) org-val#)))))
           (sort-by first (flatten-props m))))))

(defn props* [m]
  `(do
     ~@(map (fn [[k v]]
              (let [path (map #(symbol (str "-" (name %))) k)]
                `(let [org-val# (.. tbp/node ~@path)]
                   ((set-prop-fn ~path tbp/node) ~v)
                   (unmount-prop #((set-prop-fn ~path tbp/node) org-val#)))))
         (sort-by first (flatten-props m)))))