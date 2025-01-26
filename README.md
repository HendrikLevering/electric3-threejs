# Electric v3 Three.js bindings

## Usage

This library provides [Three.js](https://threejs.org/) bindings for [Electric v3](https://electric.hyperfiddle.net/), allowing you to create reactive 3D graphics applications.## Getting Started


* Add the dependency to your `deps.edn`:
* Install three.js dependencys: `npm install three` or add `"three": "^0.143.0"` to your package.json

```clojure
io.github.hendriklevering/electric3-threejs {:git/url "https://github.com/HendrikLevering/electric3-threejs"
                                                        :git/sha "e94e2829036095163acde3d731e894fb3f33cda8"}
```

## Example

```clojure
(ns my.app
  (:require [hyperfiddle.electric3 :as e]
            [hyperfiddle.electric-dom3 :as dom]
            [de.levering-it.electric.three :as three]
            [de.levering-it.electric.three.bindings :as tb]))


(e/defn Example []
   (dom/div
    (dom/props {:style {:height "50vh"}})
    (e/client
      (three/WebGLRenderer
        (let [camera (three/PerspectiveCamera 75 (/ tb/view-port-width tb/view-port-height) 0.1 2000
                       (case (three/props {:position {:x 0 :y 1 :z 0}})
                                 ; case syncing is needed because position has to be set
                                 ; bevore lookAt is called
                         (.lookAt tb/node 1 0.5 0)))
              box (three/BoxGeometry 1 1 1)
              material (three/MeshBasicMaterial nil
                         (three/props {:color (three/Color 1 0 0)}))]
          (binding [tb/camera camera]
            (three/Scene
              (three/GridHelper 20 20)
              (three/Mesh box material
                (three/props {:position {:x 5 :y 0.5 :z 0}})))))))))
```

## Start the example

This repository has included some examples. To run the examples do the following steps:

* Clone the repository
* Run `npm install`
* REPL: Use the `:example-app` alias and run `(dev/-main)` to start the dev build
* Visit http://localhost:8080 in your browser
* Hot reload is enabled - changes will reflect immediately in the browser


# Notes

The library was tested with "three": "^0.143.0". I expect that that it will work with newer versions as well. Since Electric V3 is still in alpha and under active development, I can't guarantee that it wont break in the future.

## Development

Pullrequests are welcome :-)

### Design decisions

The library is a thin wrapper around the Three.js library. It will eagerly render
every bound camera at any animation frame. This means that things will get rendered even if there hasn't changed anything in the scene. This is done to avoid some complexity in the library especially with mutable nature of three.js objects.
I guess that OpenGL is often used in highly dynamic environments, where lots of chanaging things have to be rendered. So I think, that this is a reasonable approach.
I experimented with a full reactive approach, but it was too slow, if more than
100 three.js resource objects changed between to frames.

## Links

* Electric github with source code: https://github.com/hyperfiddle/electric
* Tutorial: https://electric.hyperfiddle.net/

Copyright (c) 2025 Levering IT GmbH