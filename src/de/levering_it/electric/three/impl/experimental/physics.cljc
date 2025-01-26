(ns de.levering-it.electric.three.impl.experimental.physics)

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