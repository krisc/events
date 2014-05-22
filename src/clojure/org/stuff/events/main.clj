(ns org.stuff.events.main
  (:use [neko.activity :only [defactivity set-content-view!]]
        [neko.threading :only [on-ui]]
        [neko.ui :only [make-ui config]]
        [clojure.string :only [join]])
  (:import (java.util Calendar)
           (android.app Activity)
           (android.app DatePickerDialog DatePickerDialog$OnDateSetListener)
           (android.app DialogFragment)))

(declare ^android.widget.LinearLayout mylayout)
(declare add-event)
(declare date-picker)

(defn show-picker [activity dp]
  (. dp show (. activity getFragmentManager) "datePicker"))

(def listing (atom (sorted-map)))

(defn format-events [events]
  (->> (map (fn [[location event]]
              (format "%s - %s\n" location event))
            events)
       (join "                      ")))

(defn format-listing [lst]
  (->> (map (fn [[date events]]
              (format "%s - %s" date (format-events events)))
            lst)
       join))

(defn main-layout [activity]
  [:linear-layout {:orientation :vertical
                   :id-holder true
                   :def `mylayout}
   [:edit-text {:hint "Event name"
                :id ::name}]
   [:edit-text {:hint "Event location"
                :id ::location}]
   [:linear-layout {:orientation :horizontal}
    [:text-view {:hint "Event date",
                 :id ::date}]
    [:button {:text "..."
              :on-click (fn [_] (show-picker activity
                                             (date-picker activity)))}]]
   [:button {:text "+ Event",
             :on-click (fn [_] (add-event))}]
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
      (swap! listing update-in [date-key] (fnil conj []) [(get-elmt ::location) (get-elmt ::name)])
      (update-ui))))

(defn date-picker [activity]
  (proxy [DialogFragment DatePickerDialog$OnDateSetListener] []
    (onCreateDialog [savedInstanceState]
      (let [c (Calendar/getInstance)
            year (.get c Calendar/YEAR)
            month (.get c Calendar/MONTH)
            day (.get c Calendar/DAY_OF_MONTH)]
        (DatePickerDialog. activity this year month day)))
    (onDateSet [view year month day]
      (on-ui (.setText (::date (.getTag mylayout))
                       (str year
                            (format "%02d" (inc month))
                            (format "%02d" day)))))))

(defactivity org.stuff.events.MyActivity
  :def a
  :on-create
  (fn [this bundle]
    (on-ui
     (set-content-view! a
      (make-ui (main-layout this))))
    (on-ui
     (set-elmt ::listing (format-listing @listing)))))
