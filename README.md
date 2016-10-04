## What is Disclojure UI?

A [re-frame](https://github.com/Day8/re-frame) application to visualize and edit compositions created with [Disclojure](https://github.com/pjagielski/disclojure) and [Leipzig](https://github.com/ctford/leipzig). A browser-based DAW for [Overtone](https://github.com/overtone/overtone).

## What is Disclojure?
[Disclojure](https://github.com/pjagielski/disclojure) is a live-coding environment for [Overtone](https://github.com/overtone/overtone) and [Leipzig](https://github.com/ctford/leipzig). 

## What is Leipzig?
[Leipzig](https://github.com/ctford/leipzig) is a composition library for [Overtone](https://github.com/overtone/overtone) created by [Chris Ford](https://github.com/ctford). It provides a DSL which allows you to model melodies playable by Overtone with convenient transformations of Clojure collections.

An example Leipzig melody:
```clojure
              ; Row, row, row  your boat,
  (->> (phrase [3/3  3/3  2/3  1/3  3/3]
               [  0    0    0    1    2])
       (all :part :leader)
       (where :pitch (comp scale/C scale/major)))
```

## Visualizing melodies
<img src="https://github.com/pjagielski/disclojure-ui/raw/readme/resources/melody.gif" height="550px"/>

## Beat editing
<img src="https://github.com/pjagielski/disclojure-ui/raw/readme/resources/beats.gif" width="650px"/>

### Running

```clojure
lein repl
(require 'repl)
(reloaded.repl/reset)
```

In another terminal:
```
lein figwheel
```

Wait a bit, then browse to [http://localhost:3005](http://localhost:3449).

You can see example session in this **live-coding demo**: [![disclojure ui demo](http://img.youtube.com/vi/K98oZPca3Fw/0.jpg)](http://www.youtube.com/watch?v=K98oZPca3Fw)
