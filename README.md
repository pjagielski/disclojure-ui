## What is Disclojure UI?

A [re-frame](https://github.com/Day8/re-frame) application to visualize and edit compositions created with [Leipzig](https://github.com/ctford/leipzig) for [Overtone](https://github.com/overtone/overtone).

<img src="https://github.com/pjagielski/disclojure-ui/raw/readme/resources/disclojure2.png" height="550px"/>

## What is Overtone?
[Overtone](https://github.com/overtone/overtone) is a suberb Clojure sound library created by [Sam Aaron](https://github.com/samaaron). It lets you design own synths, play samples, interact with MIDI devices through [SuperCollider](http://supercollider.github.io) platform. Overtone is realy good for sound design and *playing individual notes* but not that good at *modeling complex compositions*. That's why some abstraction layer libraries over it like [Leipzig](https://github.com/ctford/leipzig) and [mud](https://github.com/josephwilk/mud) were created.

## What is Leipzig?
[Leipzig](https://github.com/ctford/leipzig) is a composition library for [Overtone](https://github.com/overtone/overtone) created by [Chris Ford](https://github.com/ctford). The main idea behind Leipzig is that you can model (most of) melodies by sequence of notes with durations:
```clojure
{:time 0 :pitch 67 :duration 1/4 :part :bass}
```

Leipzig provides a DSL which allows you to model these melodies with convenient transformations of Clojure collections.

Consider classic [Da Funk](https://www.youtube.com/watch?v=mmi60Bd4jSs) track by Daft Punk:
```clojure
(->> (phrase (concat [2]
                     (take 12 (cycle [1/2 1/2 1/2 2.5]))
                     [1 1])
             [7 6 7 9 4 3 4 6 2 1 2 4 0 1 2])
     (where :pitch (comp scale/G scale/minor))
     (all :part :supersaw)
     (all :amp 1))
```

The `phrase` function takes 2 collections - with note durations and pitches - which are `zip`ped to create base melody items with `:time`, `:pitch` and `:duration` entries. You can use standard Clojure collection functions like `cycle`, `take` and `concat` there. The pitches are steps of a given scale, which is provided with `scale` namespace, when you can `comp`ose scale root, type (`minor`, `major`, `pentatonic` and many more), you can go octave down with `low` and octave up with `high`. Then you just specify what instrument (`part`) should play this melody and other properties of the notes, like `:amp`, `:cutoff` and other. So, the resulting Leipzig structure looks like this:
```clojure
[{:pitch 67, :time 0, :duration 2, :part :da-funk, :amp 1}
 {:pitch 65, :time 2, :duration 1/2, :part :da-funk, :amp 1}
 {:pitch 67, :time 5/2, :duration 1/2, :part :da-funk, :amp 1}
 ...]
```

## How can Disclojure-UI help working with Leipzig melodies?
As a non-musician it was hard for me to understand the context of the notes without actually seeing them on the screen. So I built Disclojure-UI as a browser-based DAW with a piano-roll like view that visualizes melodies created with Leipzig. The data is synced in both directions - when you change the Leipzig structure in the REPL (via websocket) and when you just edit the notes by clicking in the web GUI (via REST api):

<img src="https://github.com/pjagielski/disclojure-ui/raw/readme/resources/melody3.gif" height="550px"/>

Apart from editing melodies, Disclojure-UI helps you edit beats in a separate `beat` view:

<img src="https://github.com/pjagielski/disclojure-ui/raw/readme/resources/beats2.gif" width="500px"/>

## How it works?
Frontend is built with [re-frame](https://github.com/Day8/re-frame) and [sente](https://github.com/ptaoussanis/sente) for websockets. 
Backend manages state with [component](https://github.com/stuartsierra/component) and uses [system](https://github.com/danielsz/system) and [compojure-api](https://github.com/metosin/compojure-api) to expose REST API, also visible as Swagger spec:

<img src="https://github.com/pjagielski/disclojure-ui/raw/readme/resources/api.png" width="500px"/>


## Running

Compile Clojurescript:
```clojure
lein cljsbuild once dev
```

Run:
```clojure
lein repl
(require 'repl)
(reloaded.repl/reset)
```
Browse to [http://localhost:3005](http://localhost:3005).

Interact (better with some IDE like Cursive, vim-clojure, Emacs):
```clojure
(require '[leipzig.melody :refer :all])
(require '[leipzig.scale :as scale])
(require '[leipzig.live :as live])
(require '[disclojure.live :as l])
(->> (phrase (concat [2]
                     (take 12 (cycle [1/2 1/2 1/2 2.5]))
                     [1 1])
             [7 6 7 9 4 3 4 6 2 1 2 4 0 1 2])
     (where :pitch (comp scale/G scale/minor))
     (all :part :supersaw)
     (all :amp 1)
     (l/assoc-track :supersaw))
(live/jam (l/track))
...
(live/stop)
```

More info about live coding in [disclojure](https://github.com/pjagielski/disclojure) docs.

You can see example session in this **live-coding demo**: [![disclojure ui demo](http://img.youtube.com/vi/K98oZPca3Fw/0.jpg)](http://www.youtube.com/watch?v=K98oZPca3Fw)

## License

Copyright © 2016 Piotr Jagielski

The project name refers to [Disclosure](https://www.youtube.com/watch?v=W_vM8ePGuRM) band.

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
