(ns org.stuff.events.main
  (:use [neko.activity :only [defactivity set-content-view!]]
        [neko.threading :only [on-ui]]
        [neko.ui :only [make-ui config]]
        [neko.application :only [defapplication]])
  (:import (java.util Calendar)
           (android.app Activity)
           (android.app DatePickerDialog DatePickerDialog$OnDateSetListener)
           (android.app DialogFragment)))

(declare ^android.widget.LinearLayout mylayout)
(declare add-event)
(declare date-picker)
(declare show-picker)

(defn mt-listing [] (atom (sorted-map)))
(def listing (mt-listing))

(defn format-events [e]
  ; loop through events within dates
  (loop [events e ret "" loop0 true]
    (if-not events          
      ret
      (let [loc (first (first events))
            name (second (first events))]
        (recur (next events)
               (if loop0
                 (str ret loc " - " name "\n")
                 ;NOTE: hard coding whitespace below
                 (str ret "                      " loc " - " name "\n"))
               false)))))

(defn format-listing [lst]
  ; loop through dates
  (loop [keyval (seq lst) ret ""]
    (if-not keyval
      ret
      (let [date (first (first keyval))]
        (recur (next keyval) 
               (str ret date " - " 
                    ; loop through events within dates
                    (format-events (second (first keyval)))))))))

(def main-layout [:linear-layout {:orientation :vertical,
                                  :id-holder :true,
                                  :def `mylayout}
                  [:edit-text {:hint "Event name",
                               :id ::name}]
                  [:edit-text {:hint "Event location",
                               :id ::location}]
                  [:linear-layout {:orientation :horizontal}
                   [:text-view {:hint "Event date",
                                :id ::date}]
                   [:button {:text "...",
                             :on-click (fn [_] (show-picker (date-picker)))}]]
                  [:button {:text "+ Event",
                            :on-click (fn [_](add-event))}]
                  [:text-view {:text (format-listing @listing),
                              :id ::listing}]])

(defn get-elmt [elmt]
  (str (.getText (elmt (.getTag mylayout)))))

(defn set-elmt [elmt s]
  (on-ui (config (elmt (.getTag mylayout)) :text s)))

(defn update-ui []
  (set-elmt ::listing (format-listing @listing))
  (set-elmt ::location "")
  (set-elmt ::name "")
  (set-elmt ::date ""))

(defn add-event []
  (let [date-key (try
                   (read-string (get-elmt ::date))
                   (catch RuntimeException e "Date string is empty!"))]
    (when (number? date-key)
      (if (some #{date-key} (keys @listing))
        ; add to existing date
        (swap! listing assoc date-key
               (conj (@listing date-key)
                     [(get-elmt ::location) (get-elmt ::name)]))
        ; add new date
        (swap! listing assoc date-key
               [[(get-elmt ::location) (get-elmt ::name)]]))
      (update-ui))))

(defactivity org.stuff.events.MyActivity
  :on-create
  (fn [this bundle]
    (on-ui
     (set-content-view! myActivity
      (make-ui main-layout)))
    (on-ui
     (set-elmt ::listing (format-listing @listing)))))

(defn date-picker []
  (proxy [DialogFragment DatePickerDialog$OnDateSetListener] []
    (onCreateDialog [savedInstanceState]
      (let [c (Calendar/getInstance)
            year (.get c Calendar/YEAR)
            month (.get c Calendar/MONTH)
            day (.get c Calendar/DAY_OF_MONTH)]
        (DatePickerDialog. myActivity this year month day)))
     (onDateSet [view year month day]
       (on-ui (.setText (::date (.getTag mylayout))
                        (str year
                             (format "%02d" (inc month))
                             (format "%02d" day)))))))

(defn show-picker [dp]
  (. dp show (. myActivity getFragmentManager) "datePicker"))