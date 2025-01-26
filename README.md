# electric-three
[![Clojars Project](https://img.shields.io/clojars/v/de.levering-it/electric-three.svg)](https://clojars.org/de.levering-it/electric-three)
![example branch parameter](https://github.com/HendrikLevering/electric3-threejs/actions/workflows/clojure.yaml/badge.svg?branch=main)

Three.js bindings for Electric V3 clojure.

## Usage

This library provides [Three.js](https://threejs.org/) bindings for [Electric v3](https://electric.hyperfiddle.net/), allowing you to create reactive 3D graphics applications.## Getting Started

* Install three.js dependencys: `npm install three`
* Add the dependency to your `deps.edn`:

```clojure
de.levering-it/electric-three {:mvn/version "0.0.4"}
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

Keypoints:

* three/WebGLRenderer provides the webgl context and mounts a canvas to the dom
* (binding [tb/camera camera] ...): A scene will be rendered with the camera, that is bound
to the tb/camera variable.
* (three/Scene ...) This creates a scene graph. Scenegraph objects can be created inside a scene and are mounted as a child to the parent scene graph object (it works the same way as electric dom).
* scenegraph macros are transparent in the sense that they return the value of the last form in their body (same behaviour as electric dom macros).
* resource objects macros like geometries and materials and cameras return the resource instance object
* resource and scenegraph objects can be modified with the three/props macro (works similiar to dom/props)
* the renderer requires, that the canvas has a defined height. You can either give the parent dom element a defined height or you can use dom/node inside three/WebGLRenderer to get the canvas element and alter it with dom/props

## Start the example

This repository has included some examples. To run the examples do the following steps:

* Clone the repository
* Run `npm install`
* REPL: Use the `:example-app` alias and run `(dev/-main)` to start the dev build
* Visit http://localhost:8080 in your browser
* Hot reload is enabled - changes will reflect immediately in the browser


# Notes

## Stability
The library was tested with "three": "^0.143.0". I expect that that it will work with newer versions as well. Since Electric V3 is still in alpha and under active development, I can't guarantee that it wont break in the future.

## npm-deps
Shadow-cljs CLI-tool will automatically install npm dependencies, if they are declared by a library in a [deps.cljs](https://shadow-cljs.github.io/docs/UsersGuide.html#publish-deps-cljs) file.
However, Electric starts shadow-cljs via api and this won't install npm dependencies.
You can add `(npm-deps/main nil nil)` to your shadow-cljs start code:

```clojure
    (npm-deps/main nil nil)
    (shadow-server/start!) ; no-op in calva shadow-cljs configuration which starts this out of band
    (shadow/watch :dev)
```

And require it like this:
```clojure
#?(:clj [shadow.cljs.devtools.server.npm-deps :as npm-deps])
```

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