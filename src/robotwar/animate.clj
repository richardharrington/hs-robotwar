(ns robotwar.animate
  (:use [clojure.string :only [join]]
        [clojure.pprint :only [pprint]]
        [robotwar.constants])
  (:require [clj-time.core :as time]
            [clj-time.periodic :as periodic]))

(defn worlds-for-terminal
  "takes worlds and a fast-forward factor, and
  adds two fields to each world: idx and timestamp.
  useful for our hacky display-in-terminal situation."
  [worlds fast-forward]
  (let [tick-duration (/ *GAME-SECONDS-PER-TICK* fast-forward)]
    (map-indexed (fn [idx world]
                   (into world
                         {:idx idx
                          :timestamp (int (* idx tick-duration 1000))}))
                 worlds)))

(defn worlds-for-browser
  "builds a sequence of worlds with the robots' brains
  removed, for more compact transmission by json.
  Fast-forward factor will be dynamically added by animation
  function in browser.
  TODO: remove some unnecessary robot fields if we need to 
  for speed (we're going to keep them in now for diagnostic
  purposes in the browser)"
  [worlds]
  (map (fn [world]
         (assoc world :robots (mapv #(dissoc % :brain) (:robots world))))
       worlds))

(defn near-point [[pos-x pos-y] [x y]] 
  (and (= (int pos-x) x)
       (= (int pos-y) y)))

(defn arena-text-grid
  "outputs the arena, with borders"
  [{:keys [width height robots]} print-width print-height]
  (let [horiz-border-char "-"
        vert-border-char "+"
        header-footer (apply str (repeat (+ (* print-width 3) 2) horiz-border-char))
        scale-x #(* % (/ print-width width))
        scale-y #(* % (/ print-height height))
        field (for [y (range print-height), x (range print-width)]
                (or (some (fn [{:keys [idx pos-x pos-y]}]
                        (when (near-point [(scale-x pos-x) (scale-y pos-y)] [x y])
                          (str "(" idx ")")))
                      robots)
                    "   "))]
    (str header-footer
         "\n" 
         (join "\n" (map #(join (apply str %) (repeat 2 vert-border-char))
                         (partition print-width field))) 
         "\n" 
         header-footer)))

(defn display-robots-info [{idx :idx :as world} time-since-start fps]
  (doseq [robot-idx (range (count (:robots world)))]
    (println (apply format 
                    "%d: x %.1f, y %.1f, v-x %.1f, v-y %.1f, desired-v-x %.1f, desired-v-y %.1f" 
                    (map #(get-in world [:robots robot-idx %]) 
                         [:idx :pos-x :pos-y :v-x :v-y :desired-v-x :desired-v-y]))))
  (println (format "Animation frame rate: %.1f frames per second", fps))
  (println "Round number:" idx)
  (println (format "Seconds elapsed in the game world: %d", (int (* idx *GAME-SECONDS-PER-TICK*))))
  (println (format "Seconds elapsed in the real world: %d", (time/in-secs time-since-start)))
  (println))

(defn animate
  "animates a sequence of worlds in the terminal"
  [initial-worlds print-width print-height fps]
  (let [frame-period (time/millis (* (/ 1 fps) 1000))
        starting-instant (time/now)]
    (loop [[world :as worlds] initial-worlds
           frame-start starting-instant]
      (println (arena-text-grid world print-width print-height))
      (display-robots-info world (time/interval starting-instant frame-start) fps) 
      (let [desired-next-frame-calc-start (time/plus frame-start frame-period)
            this-instant (time/now)
            next-frame-calc-start (if (time/after? this-instant desired-next-frame-calc-start)
                                    this-instant
                                    (do
                                      (-> (time/interval 
                                            this-instant 
                                            desired-next-frame-calc-start)
                                          (time/in-msecs)
                                          (Thread/sleep))
                                      desired-next-frame-calc-start))
            animation-timestamp (time/in-msecs (time/interval
                                                 starting-instant
                                                 next-frame-calc-start))]
        (recur (drop-while #(< (:timestamp %) animation-timestamp) 
                           worlds)
               next-frame-calc-start)))))
                           

