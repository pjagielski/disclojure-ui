# overdaw-fe

A [re-frame](https://github.com/Day8/re-frame) application to visualize compositions created with [Disclojure](https://github.com/pjagielski/disclojure) and [Leipzig](https://github.com/ctford/leipzig)

## Demo

[![disclojure ui demo](http://img.youtube.com/vi/K98oZPca3Fw/0.jpg)](http://www.youtube.com/watch?v=K98oZPca3Fw)

### Run application:

```clojure
lein repl
(require 'repl)
(reloaded.repl/reset)
```

In another terminal:
```
lein figwheel
```

Figwheel will automatically push cljs changes to the browser.

Wait a bit, then browse to [http://localhost:3005](http://localhost:3449).
