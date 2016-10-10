## What is Disclojure UI?

A [re-frame](https://github.com/Day8/re-frame) application to visualize and edit compositions created with [Leipzig](https://github.com/ctford/leipzig) for [Overtone](https://github.com/overtone/overtone).

![disclojure](https://github.com/pjagielski/disclojure-ui/raw/readme/resources/disclojure.png)

## What is Overtone?
[Overtone](https://github.com/overtone/overtone) is a suberb Clojure sound library created by [Sa0m Aaron](https://github.com/samaaron). It lets you design own synths, play samples, interact with MIDI devices through [SuperCollider](http://supercollider.github.io) platform. Overtone is realy good for sound design and *playing individual notes* but not that good at *modeling complex compositions*. That's why some abstraction layer libraries over it like [Leipzig](https://github.com/ctford/leipzig) and [mud](https://github.com/josephwilk/mud) were created.

## What is Leipzig?
[Leipzig](https://github.com/ctford/leipzig) is a composition library for [Overtone](https://github.com/overtone/overtone) created by [Chris Ford](https://github.com/ctford). The main idea behind Leipzig is that you can model (most of) melodies by sequence of notes with durations:
```clojure
{:time 0 :pitch 67 :duration 1/4 :part :bass}
```

Leipzig provides a DSL which allows you to model these melodies with convenient transformations of Clojure collections.

Consider classic Da Funk track by Daft Punk:
```clojure
(->> (phrase (concat [2]
                     (take 12 (cycle [1/2 1/2 1/2 2.5]))
                     [1 1])
             [0 -1 0 2 -3 -4 -3 -1 -5 -6 -5 -3 -7 -6 -5])
     (where :pitch (comp scale/G scale/minor))
     (all :part :supersaw)
     (all :amp 1))
```

The `phrase` function takes 2 collections: one with note durations and one with pitches. 

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
