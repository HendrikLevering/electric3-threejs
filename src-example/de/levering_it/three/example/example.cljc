(ns de.levering-it.three.example.example
  (:require [hyperfiddle.electric3 :as e]
            [de.levering-it.three.three :as tt]
            [de.levering-it.three.bindings :as tbp]
            [de.levering-it.three.utils :as tu]
            [hyperfiddle.electric-dom3 :as dom]
            [hyperfiddle.electric-forms3 :refer [Checkbox]]
            [clojure.string :as s]))

(defn sign [x]
  (cond
    (pos? x) 1
    (neg? x) -1
    :else 0))

(defn sign-change? [old new]
  (cond
    (zero? new) false
    (zero? old) false
    :else (not= (sign old) (sign new))))


(defn- compute-look-at
  "Compute the 3D 'look-at' direction from yaw and pitch.
   Ignores roll for simplicity. The vector is normalized."
  [yaw pitch]
  ;; Typical 3D 'forward' convention in many game engines:
  ;;   x = cos(pitch) * cos(yaw)
  ;;   y = sin(pitch)
  ;;   z = cos(pitch) * sin(yaw)
  (let [cp (Math/cos pitch)
        sp (Math/sin pitch)
        cy (Math/cos yaw)
        sy (Math/sin yaw)
        lx (* cp cy)
        ly sp
        lz (* cp sy)]
    [lx ly lz]))

 (def ^:private max-pitch-rads
   "Maximum absolute pitch in radians (±85°)."
   #?(:clj (Math/toRadians 85)
      :cljs 1.4835298641951802))

 (defn clamp
   "Clamp `val` between `min-val` and `max-val`."
   [val min-val max-val]
   (max min-val (min max-val val)))

(defn- do-single-step
  "Perform a single physics step of `dt-ms` (<= 50ms ideally).
   Returns the updated state."
  [dt-ms state opts actions]
  (let [{:keys [x y z
                vx vy vz
                yaw pitch roll
                mass mi_yaw mi_pitch mi_roll]} state

        {:keys [force
                torque_yaw
                torque_pitch
                torque_roll
                friction
                G
                max-speed
                initial-jump-speed]} opts

        ;; Convert milliseconds to seconds for physics calculations
        move-left? (:move-left actions)
        move-right? (:move-right actions)
        jump? (:jump actions)
        move-forward? (:move-forward actions)
        move-backward? (:move-backward actions)
        look-up? (:look-up actions)
        look-down? (:look-down actions)
        turn-left? (:turn-left actions)
        turn-right? (:turn-right actions)
        dt              (/ dt-ms 1000.0)
        mass            (or mass 1.0)
        mi-yaw          (or mi_yaw 1.0)
        mi-pitch        (or mi_pitch 1.0)
        mi-roll         (or mi_roll 1.0)
        friction        (or friction 0.0)
        G               (or G 0.0)
        force           (or force 0.0)
        torque-yaw      (or torque_yaw 0.0)
        torque-pitch    (or torque_pitch 0.0)
        torque-roll     (or torque_roll 0.0)
        max-speed       (or max-speed 10.0)
        initial-jump    (or initial-jump-speed 5.0)]

    ;; ---------------------------------------------------------
    ;; If the action is :jump and we are on the ground (y=0),
    ;; we set vy to a positive initial jump speed.
    ;; (We do this BEFORE the actual physics step.)
    ;; ---------------------------------------------------------
    (let [vy (if (and jump? (zero? y))
               initial-jump
               vy)]

      ;; ---------------------------------------------------------
      ;; 1) Determine directional movement forces based on action
      ;; ---------------------------------------------------------
      (let [forward-dir-x (Math/cos yaw)
            forward-dir-z (Math/sin yaw)
            right-dir-x   (- (Math/sin yaw))
            right-dir-z   (Math/cos yaw)
            forward (fn [[x y]]
                      [(+ x (* force forward-dir-x)) (+ y (* force forward-dir-z))])
            backward (fn [[x y]]
                       [(+ x (* (- force) forward-dir-x)) (+ y (* (- force) forward-dir-z))])
            left (fn [[x y]]
                   [(+ x (* (- force) right-dir-x)) (+ y (* (- force) right-dir-z))])
            right (fn [[x y]]
                    [(+ x (* force right-dir-x)) (+ y (* force right-dir-z))])

            [fx-input fz-input]
            (cond-> [0 0]
              move-forward? forward
              move-backward? backward
              move-left? left
              move-right? right)]

        ;; ---------------------------------------------------------
        ;; 2) Sum forces => acceleration = F/m
        ;;    friction is linear in velocity => -friction * v
        ;;    gravity if G>0 => downward force = -G
        ;; ---------------------------------------------------------
        (let [gravity-force-y  (* (- G) mass)
              friction-force-x (if (zero? y)
                                 (* friction gravity-force-y vx)
                                 0)
              friction-force-z (if (zero? y)
                                 (* friction gravity-force-y vz)
                                 0)

              total-force-x (+ fx-input friction-force-x)
              total-force-y (+ gravity-force-y)
              total-force-z (+ fz-input friction-force-z)

              ax (/ total-force-x mass)
              ay (/ total-force-y mass)
              az (/ total-force-z mass)]

          ;; ---------------------------------------------------------
          ;; 3) Integrate linear velocity
          ;; ---------------------------------------------------------
          (let [new-vx (+ vx (* ax dt))
                new-vy (+ vy (* ay dt))
                new-vz (+ vz (* az dt))

                ;; 3a) Clamp horizontal speed in XZ plane
                horizontal-speed (Math/sqrt (+ (* new-vx new-vx)
                                              (* new-vz new-vz)))
                [final-vx final-vz]
                (if (and (> horizontal-speed 0.00001)
                      (> horizontal-speed max-speed))
                  (let [scale (/ max-speed horizontal-speed)]
                    [(* new-vx scale)
                     (* new-vz scale)])
                  [new-vx  new-vz])
                final-vx (if (and (not (or move-forward? move-backward? move-left? move-right?)) (sign-change? final-vx vx))
                           0
                           final-vx)
                final-vz (if (and (not (or move-forward? move-backward? move-left? move-right?)) (sign-change? final-vz vz))
                           0
                           final-vz)]


            ;; ---------------------------------------------------------
            ;; 4) Update positions (Euler integration)
            ;; ---------------------------------------------------------
            (let [final-x (+ x (* final-vx dt))
                  final-y (+ y (* new-vy dt))
                  final-z (+ z (* final-vz dt))]

              ;; ---------------------------------------------------------
              ;; 5) Update angular rotation (yaw, pitch, roll)
              ;;    Simple Euler approach:
              ;;    angle += (torque / moment-of-inertia) * dt
              ;;    Also handle look-up/look-down directly.
              ;; ---------------------------------------------------------
              (let [yaw-acc   (/ torque-yaw mi-yaw)
                    left #(- % (* yaw-acc dt))
                    right #(+ % (* yaw-acc dt))
                    pitch-acc (/ torque-pitch mi-pitch)
                    up #(+ % (* pitch-acc dt))
                    down #(- % (* pitch-acc dt))
                    new-yaw (cond-> yaw
                              turn-left?   left
                              turn-right? right)

                    ;; For direct look controls:
                    new-pitch (cond->  pitch
                                look-up?   up
                                look-down? down)
                    clamped-pitch (clamp new-pitch (- max-pitch-rads) max-pitch-rads)

                    roll-acc  (/ torque-roll mi-roll)
                    new-roll  (+ roll (* roll-acc dt))]

                ;; ---------------------------------------------------------
                ;; 6) Floor collision check: cannot sink below y=0
                ;;    If y < 0, clamp to 0 and zero out vy.
                ;; ---------------------------------------------------------
                (let [[clamped-y clamped-vy]
                      (if (neg? final-y)
                        [0 0]        ;; Land on the floor
                        [final-y new-vy])
                      look-at (compute-look-at new-yaw clamped-pitch)]

                  ;; ---------------------------------------------------------
                  ;; 7) Return updated state
                  ;; ---------------------------------------------------------
                  {:x        final-x
                   :y        clamped-y
                   :z        final-z
                   :vx       final-vx
                   :vy       clamped-vy
                   :vz       final-vz
                   :yaw      new-yaw
                   :pitch    clamped-pitch
                   :roll     new-roll
                   :mass     mass
                   :mi_yaw   mi-yaw
                   :mi_pitch mi-pitch
                   :mi_roll  mi-roll
                   :eyes-height (:eyes-height state)
                   :look-at  look-at})))))))))

(defn move-entity
  "Updates the entity's state with possible sub-stepping if dt-ms > 50.
   - `dt-ms`: time delta in milliseconds
   - `state`: current entity state map (keys: :x, :y, :z, :vx, :vy, :vz,
             :yaw, :pitch, :roll, :mass, :mi_yaw, :mi_pitch, :mi_roll)
   - `opts`:  options map (keys can include: :force, :torque_yaw, :torque_pitch,
             :torque_roll, :friction, :G, :max-speed, :initial-jump-speed)
   - `action`: set of keywords describing the player's intent:
        - Movement: :move-forward, :move-backward, :move-left, :move-right
        - Turning:  :turn-left, :turn-right
        - Looking:  :look-up, :look-down
        - Jumping:  :jump

   Returns the updated state map."
  [state dt-ms actions opts]
  (let [max-step-ms 50.0]
    (loop [acc-state state
           dt-left   dt-ms]
      (if (pos? dt-left)
        ;; Step in chunks of up to 50 ms
        (let [this-step (min max-step-ms dt-left)
              new-state (do-single-step this-step acc-state opts actions)]
          (recur new-state (max 0 (- dt-left this-step))))
        ;; All time consumed, return final
        acc-state))))

;; Suppose we have an initial player state at rest, on the ground, facing yaw=0.
(def player-state
  {:x     0.0
   :y     0.0
   :z     0.0
   :eyes-height 1.75
   :vx    0.0
   :vy    0.0
   :vz    0.0
   :yaw   0.0
   :pitch 0.0
   :roll  0.0

   ;; Physical parameters
   :mass     80.0        ;; 80 kg
   :mi_yaw   1.0
   :mi_pitch 1.0
   :mi_roll  1.0
   :look-at [1 0 0]})

(def opts
  {:force             1000.0   ;; N (Newtons)
   :torque_yaw        5.0
   :torque_pitch      0.0
   :torque_roll       0.0
   :friction          0.5
   :G                 1.6 #_9.81
   :max-speed         5.0
   :initial-jump-speed 3.0 })

(comment
  (cond-> 1
    false inc
    true inc
    )
  (-> player-state
    (move-entity 5 :move-forward opts)
    (move-entity 5 :none opts)
    #_(move-entity 10 :move-forward opts))
  )

(e/defn Example []
  (e/client
    (let [cb (dom/div (dom/text "wireframe:") (Checkbox false))]
      (dom/div
        (dom/props {:style {:min-height "50vh"}})
        (tt/WebGLRenderer
          (dom/props {:antialias true})
          (let [key-state (binding [dom/node (.-body js/document)] (tu/KeyState))
                up? (get key-state "ArrowUp")
                down? (get key-state "ArrowDown")
                left? (get key-state "ArrowLeft")
                right? (get key-state "ArrowRight")
                shift? (get key-state "Shift")
                spacebar? (get key-state " ")
                state! (atom player-state)
                state (e/watch state!)
                x (:x state)
                y (:y state)
                z (:z state)
                eyes-height (+ y (:eyes-height state))
                [lx ly lz] (:look-at state)
                t (e/System-time-ms)]

            (e/client
              (let [actions (cond-> #{}
                              spacebar? (conj :jump)
                              (and shift? left?) (conj :move-left)
                              (and shift? right?) (conj :move-right)
                              (and (not shift?) left?) (conj :turn-left)
                              (and (not shift?) right?) (conj :turn-right)
                              up? (conj :move-forward)
                              down? (conj :move-backward))]
                ((tu/with-t->dt (fn [dt state!]
                                  (swap! state! move-entity dt actions opts))) t state!)))
            (let [c  (tt/PerspectiveCamera 75 (/ tbp/view-port-width tbp/view-port-height) 0.1 2000
                       (case (tt/props {:position {:x x :y eyes-height :z z}})
                         (.lookAt tbp/node (+ x lx) (+ eyes-height ly) (+ z lz))))
                  box (tt/BoxGeometry 1 1 1)
                  box2 (tt/BoxGeometry 1 1 1)
                  material (tt/MeshBasicMaterial nil
                             (tt/props {:wireframe false
                                        :color (if cb (tt/Color 1 0 0) (tt/Color 0 1 0))}))
                  skybox (tt/BoxGeometry 1000 1000 1000)
                  skybox-mat (tt/MeshBasicMaterial nil
                               (tt/props {:color (tt/Color 0.4 0.6 0.9), :side tt/backSide }))
                  texture (.load
                            (tt/TextureLoader )
                            "stone2.jpg")
                  ground-texture  (.load (tt/TextureLoader) "grass2.jpg"
                                    (fn [t]
                                      (set! (.-wrapS t) tt/repeatWrapping)
                                      (set! (.-wrapT t) tt/repeatWrapping)
                                      (.set (.-repeat t) 100 100)))
                  ground (tt/PlaneGeometry 1000 1000 1 1)
                  ground-mat (tt/MeshPhongMaterial nil
                               (tt/props {:shininess 100
                                          :map ground-texture
                                          :specular (tt/Color 0.5 0.5 0.5)}))
                  phong-material (tt/MeshPhongMaterial nil
                                   (tt/props {:shininess 100
                                              :wireframe cb
                                              :map texture
                                              :specular (tt/Color 0.5 0.5 0.5)}))

                  render-target (tt/WebGLCubeRenderTarget 512 (clj->js {:generateMipmaps true
                                                                        :minFilter tt/linearMipmapLinearFilter}))
                  cube-x 5
                  cube-z 0
                  cube-camera (tt/CubeCamera 0.2 2000 render-target
                                (tt/props {:position {:x cube-x :y eyes-height :z cube-z}}))
                  cube-material (tt/MeshBasicMaterial nil
                                  (tt/props {:envMap (.-texture render-target)
                                             :reflectivity 0.5}))]
              ground-mat ;prevent unloading
              (binding [tbp/camera [cube-camera c]]
                (tt/Scene
                  #_(tt/AmbientLight 0x404040)

                  (tt/HemisphereLight 0xffffff 0x888888 1)
                  (tt/Mesh skybox skybox-mat)


                  (tt/Mesh box material
                    (tt/props {:position {:x x :y (+ y 0.5) :z z}}))
                  (if cb
                    (tt/GridHelper 1000 1000)
                    (tt/Mesh ground ground-mat
                      (tt/props {:rotation {:x  tu/deg-90}})))

                  (tt/Mesh box2 cube-material
                    (tt/props {:position {:x cube-x :y 1.75 :z cube-z}}))

                  (let [items 10]
                    (e/for [i (e/diff-by identity (range items))]
                      (tt/Mesh box phong-material
                        (tt/props {:position {:x -5 :y 0.5 :z (* 2 (- (/ items 2) i))}}))
                      (tt/Mesh box phong-material
                        (tt/props {:position {:x 5 :y 0.5 :z (* 2 (- (/ items 2) i))}}))))))
              (binding [tbp/camera [cube-camera c]]
                (tt/Scene
                  #_(tt/AmbientLight 0x404040)

                  (tt/HemisphereLight 0xffffff 0x888888 1)
                  (tt/Mesh skybox skybox-mat)


                  (tt/Mesh box material
                    (tt/props {:position {:x x :y (+ y 0.5) :z z}}))
                  (if cb
                    (tt/GridHelper 1000 1000)
                    (tt/Mesh ground ground-mat
                      (tt/props {:rotation {:x  tu/deg-90}})))

                  (tt/Mesh box2 cube-material
                    (tt/props {:position {:x cube-x :y 1.75 :z cube-z}}))

                  (let [items 10]
                    (e/for [i (e/diff-by identity (range items))]
                      (tt/Mesh box phong-material
                        (tt/props {:position {:x -5 :y 0.5 :z (* 2 (- (/ items 2) i))}}))
                      (tt/Mesh box phong-material
                        (tt/props {:position {:x 5 :y 0.5 :z (* 2 (- (/ items 2) i))}})))))))))))))