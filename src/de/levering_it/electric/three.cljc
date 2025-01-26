(ns de.levering-it.electric.three
  (:require #?(:cljs ["three" :as three])
            [de.levering-it.electric.three.impl :as impl]
            [hyperfiddle.electric-dom3 :as dom]
            [missionary.core :as m]
            [hyperfiddle.electric3 :as e])
  #?(:cljs (:require-macros [de.levering-it.electric.three])))


;; set properties of three.js obj
(defmacro props [m]
  (impl/props* m))


;; Cameras
(def PerspectiveCamera-Ctor #?(:cljs three/PerspectiveCamera))
(defmacro PerspectiveCamera [& body] (impl/resource-obj* `PerspectiveCamera-Ctor 4 body))

(def CubeCamera-Ctor #?(:cljs three/CubeCamera))
(defmacro CubeCamera [& body] (impl/resource-obj* `CubeCamera-Ctor 3 body))


;; Geometry
(def BoxGeometry-Ctor #?(:cljs three/BoxGeometry))
(defmacro BoxGeometry [& body] (impl/resource-obj* `BoxGeometry-Ctor 6 body))

(def CapsuleGeometry-Ctor #?(:cljs three/CapsuleGeometry))
(defmacro CapsuleGeometry [& body] (impl/resource-obj* `CapsuleGeometry-Ctor 4 body))

(def CircleGeometry-Ctor #?(:cljs three/CircleGeometry))
(defmacro CircleGeometry [& body] (impl/resource-obj* `CircleGeometry-Ctor 4 body))

(def ConeGeometry-Ctor #?(:cljs three/ConeGeometry))
(defmacro ConeGeometry [& body] (impl/resource-obj* `ConeGeometry-Ctor 7 body))

(def CylinderGeometry-Ctor #?(:cljs three/CylinderGeometry))
(defmacro CylinderGeometry [& body] (impl/resource-obj* `CylinderGeometry-Ctor 8 body))

(def DodecahedronGeometry-Ctor #?(:cljs three/DodecahedronGeometry))
(defmacro DodecahedronGeometry [& body] (impl/resource-obj* `DodecahedronGeometry-Ctor 2 body))

(def EdgesGeometry-Ctor #?(:cljs three/EdgesGeometry))
(defmacro EdgesGeometry [& body] (impl/resource-obj* `EdgesGeometry-Ctor 2 body))

(def ExtrudeGeometry-Ctor #?(:cljs three/ExtrudeGeometry))
(defmacro ExtrudeGeometry [& body] (impl/resource-obj* `ExtrudeGeometry-Ctor 2 body))

(def IcosahedronGeometry-Ctor #?(:cljs three/IcosahedronGeometry))
(defmacro IcosahedronGeometry [& body] (impl/resource-obj* `IcosahedronGeometry-Ctor 2 body))

(def LatheGeometry-Ctor #?(:cljs three/LatheGeometry))
(defmacro LatheGeometry [& body] (impl/resource-obj* `LatheGeometry-Ctor 3 body))

(def OctahedronGeometry-Ctor #?(:cljs three/OctahedronGeometry))
(defmacro OctahedronGeometry [& body] (impl/resource-obj* `OctahedronGeometry-Ctor 2 body))

(def PlaneGeometry-Ctor #?(:cljs three/PlaneGeometry))
(defmacro PlaneGeometry [& body] (impl/resource-obj* `PlaneGeometry-Ctor 4 body))

(def PolyhedronGeometry-Ctor #?(:cljs three/PolyhedronGeometry))
(defmacro PolyhedronGeometry [& body] (impl/resource-obj* `PolyhedronGeometry-Ctor 4 body))

(def RingGeometry-Ctor #?(:cljs three/RingGeometry))
(defmacro RingGeometry [& body] (impl/resource-obj* `RingGeometry-Ctor 7 body))

(def ShapeGeometry-Ctor #?(:cljs three/ShapeGeometry))
(defmacro ShapeGeometry [& body] (impl/resource-obj* `ShapeGeometry-Ctor 2 body))

(def SphereGeometry-Ctor #?(:cljs three/SphereGeometry))
(defmacro SphereGeometry [& body] (impl/resource-obj* `SphereGeometry-Ctor 7 body))

(def TetrahedronGeometry-Ctor #?(:cljs three/TetrahedronGeometry))
(defmacro TetrahedronGeometry [& body] (impl/resource-obj* `TetrahedronGeometry-Ctor 2 body))

(def TorusGeometry-Ctor #?(:cljs three/TorusGeometry))
(defmacro TorusGeometry [& body] (impl/resource-obj* `TorusGeometry-Ctor 5 body))

(def TorusKnotGeometry-Ctor #?(:cljs three/TorusKnotGeometry))
(defmacro TorusKnotGeometry [& body] (impl/resource-obj* `TorusKnotGeometry-Ctor 7 body))

(def TubeGeometry-Ctor #?(:cljs three/TubeGeometry))
(defmacro TubeGeometry [& body] (impl/resource-obj* `TubeGeometry-Ctor 5 body))

(def WireframeGeometry-Ctor #?(:cljs three/WireframeGeometry))
(defmacro WireframeGeometry [& body] (impl/resource-obj* `WireframeGeometry-Ctor 1 body))


;; Helpers
(def GridHelper-Ctor #?(:cljs three/GridHelper))
(defmacro GridHelper [& body] (impl/scene-obj* `GridHelper-Ctor 2 body))

(def AxesHelper-Ctor #?(:cljs three/AxesHelper))
(defmacro AxesHelper [& body] (impl/scene-obj* `AxesHelper-Ctor 1 body))

(def BoxHelper-Ctor #?(:cljs three/BoxHelper))
(defmacro BoxHelper [& body] (impl/scene-obj* `BoxHelper-Ctor 2 body))

(def Box3Helper-Ctor #?(:cljs three/Box3Helper))
(defmacro Box3Helper [& body] (impl/scene-obj* `Box3Helper-Ctor 2 body))

(def CameraHelper-Ctor #?(:cljs three/CameraHelper))
(defmacro CameraHelper [& body] (impl/scene-obj* `CameraHelper-Ctor 1 body))

(def DirectionalLightHelper-Ctor #?(:cljs three/DirectionalLightHelper))
(defmacro DirectionalLightHelper [& body] (impl/scene-obj* `DirectionalLightHelper-Ctor 2 body))

(def HemisphereLightHelper-Ctor #?(:cljs three/HemisphereLightHelper))
(defmacro HemisphereLightHelper [& body] (impl/scene-obj* `HemisphereLightHelper-Ctor 2 body))

(def PlaneHelper-Ctor #?(:cljs three/PlaneHelper))
(defmacro PlaneHelper [& body] (impl/scene-obj* `PlaneHelper-Ctor 2 body))

(def PointLightHelper-Ctor #?(:cljs three/PointLightHelper))
(defmacro PointLightHelper [& body] (impl/scene-obj* `PointLightHelper-Ctor 2 body))

(def PolarGridHelper-Ctor #?(:cljs three/PolarGridHelper))
(defmacro PolarGridHelper [& body] (impl/scene-obj* `PolarGridHelper-Ctor 6 body))

(def SkeletonHelper-Ctor #?(:cljs three/SkeletonHelper))
(defmacro SkeletonHelper [& body] (impl/scene-obj* `SkeletonHelper-Ctor 1 body))

(def SpotLightHelper-Ctor #?(:cljs three/SpotLightHelper))
(defmacro SpotLightHelper [& body] (impl/scene-obj* `SpotLightHelper-Ctor 2 body))


;; Lights
(def AmbientLight-Ctor #?(:cljs three/AmbientLight))
(defmacro AmbientLight [& body] (impl/scene-obj* `AmbientLight-Ctor 2 body))

(def DirectionalLight-Ctor #?(:cljs three/DirectionalLight))
(defmacro DirectionalLight [& body] (impl/scene-obj* `DirectionalLight-Ctor 2 body))

(def HemisphereLight-Ctor #?(:cljs three/HemisphereLight))
(defmacro HemisphereLight [& body] (impl/scene-obj* `HemisphereLight-Ctor 3 body))

(def PointLight-Ctor #?(:cljs three/PointLight))
(defmacro PointLight [& body] (impl/scene-obj* `PointLight-Ctor 4 body))

(def RectAreaLight-Ctor #?(:cljs three/RectAreaLight))
(defmacro RectAreaLight [& body] (impl/scene-obj* `RectAreaLight-Ctor 4 body))

(def SpotLight-Ctor #?(:cljs three/SpotLight))
(defmacro SpotLight [& body] (impl/scene-obj* `SpotLight-Ctor 6 body))

(def LightProbe-Ctor #?(:cljs three/LightProbe))
(defmacro LightProbe [& body] (impl/scene-obj* `LightProbe-Ctor 2 body))


;; Materials
(def MeshBasicMaterial-Ctor #?(:cljs three/MeshBasicMaterial))
(defmacro MeshBasicMaterial [& body] (impl/resource-obj* `MeshBasicMaterial-Ctor 1 body))

(def MeshPhysicalMaterial-Ctor #?(:cljs three/MeshPhysicalMaterial))
(defmacro MeshPhysicalMaterial [& body] (impl/resource-obj* `MeshPhysicalMaterial-Ctor 1 body))

(def MeshDepthMaterial-Ctor #?(:cljs three/MeshDepthMaterial))
(defmacro MeshDepthMaterial [& body] (impl/resource-obj* `MeshDepthMaterial-Ctor 1 body))

(def MeshLambertMaterial-Ctor #?(:cljs three/MeshLambertMaterial))
(defmacro MeshLambertMaterial [& body] (impl/resource-obj* `MeshLambertMaterial-Ctor 1 body))

(def MeshMatcapMaterial-Ctor #?(:cljs three/MeshMatcapMaterial))
(defmacro MeshMatcapMaterial [& body] (impl/resource-obj* `MeshMatcapMaterial-Ctor 1 body))

(def MeshNormalMaterial-Ctor #?(:cljs three/MeshNormalMaterial))
(defmacro MeshNormalMaterial [& body] (impl/resource-obj* `MeshNormalMaterial-Ctor 1 body))

(def MeshPhongMaterial-Ctor #?(:cljs three/MeshPhongMaterial))
(defmacro MeshPhongMaterial [& body] (impl/resource-obj* `MeshPhongMaterial-Ctor 1 body))

(def MeshStandardMaterial-Ctor #?(:cljs three/MeshStandardMaterial))
(defmacro MeshStandardMaterial [& body] (impl/resource-obj* `MeshStandardMaterial-Ctor 1 body))

(def MeshToonMaterial-Ctor #?(:cljs three/MeshToonMaterial))
(defmacro MeshToonMaterial [& body] (impl/resource-obj* `MeshToonMaterial-Ctor 1 body))

(def LineBasicMaterial-Ctor #?(:cljs three/LineBasicMaterial))
(defmacro LineBasicMaterial [& body] (impl/resource-obj* `LineBasicMaterial-Ctor 1 body))

(def LineDashedMaterial-Ctor #?(:cljs three/LineDashedMaterial))
(defmacro LineDashedMaterial [& body] (impl/resource-obj* `LineDashedMaterial-Ctor 1 body))

(def PointsMaterial-Ctor #?(:cljs three/PointsMaterial))
(defmacro PointsMaterial [& body] (impl/resource-obj* `PointsMaterial-Ctor 1 body))

(def RawShaderMaterial-Ctor #?(:cljs three/RawShaderMaterial))
(defmacro RawShaderMaterial [& body] (impl/resource-obj* `RawShaderMaterial-Ctor 1 body))

(def ShaderMaterial-Ctor #?(:cljs three/ShaderMaterial))
(defmacro ShaderMaterial [& body] (impl/resource-obj* `ShaderMaterial-Ctor 1 body))

(def ShadowMaterial-Ctor #?(:cljs three/ShadowMaterial))
(defmacro ShadowMaterial [& body] (impl/resource-obj* `ShadowMaterial-Ctor 1 body))

(def SpriteMaterial-Ctor #?(:cljs three/SpriteMaterial))
(defmacro SpriteMaterial [& body] (impl/resource-obj* `SpriteMaterial-Ctor 1 body))


;; Objects
(def Mesh-Ctor #?(:cljs three/Mesh))
(defmacro Mesh [& body] (impl/scene-obj* `Mesh-Ctor 2 body))

(def Group-Ctor #?(:cljs three/Group))
(defmacro Group [& body] (impl/scene-obj* `Group-Ctor 0 body))

(def Line-Ctor #?(:cljs three/Line))
(defmacro Line [& body] (impl/scene-obj* `Line-Ctor 2 body))

(def LineLoop-Ctor #?(:cljs three/LineLoop))
(defmacro LineLoop [& body] (impl/scene-obj* `LineLoop-Ctor 2 body))

(def LineSegments-Ctor #?(:cljs three/LineSegments))
(defmacro LineSegments [& body] (impl/scene-obj* `LineSegments-Ctor 2 body))

(def Points-Ctor #?(:cljs three/Points))
(defmacro Points [& body] (impl/scene-obj* `Points-Ctor 2 body))

(def Sprite-Ctor #?(:cljs three/Sprite))
(defmacro Sprite [& body] (impl/scene-obj* `Sprite-Ctor 1 body))


;; Scene
(def Fog-Ctor #?(:cljs three/Fog))
(defmacro Fog [& body] (impl/resource-obj* `Fog-Ctor 3 body))

(def FogExp2-Ctor #?(:cljs three/FogExp2))
(defmacro FogExp2 [& body] (impl/resource-obj* `FogExp2-Ctor 2 body))

(defmacro WebGLRenderer [& body] `(impl/WebGLRenderer* (e/fn [] ~@body)))
(defmacro Scene [& body] `(impl/Scene* (e/fn [] ~@body)))

(def WebGLCubeRenderTarget-Ctor #?(:cljs three/WebGLCubeRenderTarget))
(defmacro WebGLCubeRenderTarget [& body] (impl/resource-obj* `WebGLCubeRenderTarget-Ctor 2 body))

;; Math
(def Color-Ctor #?(:cljs three/Color))
(defmacro Color [& body] (impl/resource-obj* `Color-Ctor 3 body))

(def Vector2-Ctor #?(:cljs three/Vector2))
(defmacro Vector2 [& body] (impl/resource-obj* `Vector2-Ctor 2 body))

(def Vector3-Ctor #?(:cljs three/Vector3))
(defmacro Vector3 [& body] (impl/resource-obj* `Vector3-Ctor 3 body))

(def Vector4-Ctor #?(:cljs three/Vector4))
(defmacro Vector4 [& body] (impl/resource-obj* `Vector4-Ctor 4 body))

(def Euler-Ctor #?(:cljs three/Euler))
(defmacro Euler [& body] (impl/resource-obj* `Euler-Ctor 4 body))

(def Quaternion-Ctor #?(:cljs three/Quaternion))
(defmacro Quaternion [& body] (impl/resource-obj* `Quaternion-Ctor 4 body))

(def Matrix3-Ctor #?(:cljs three/Matrix3))
(defmacro Matrix3 [& body] (impl/resource-obj* `Matrix3-Ctor 0 body))

(def Matrix4-Ctor #?(:cljs three/Matrix4))
(defmacro Matrix4 [& body] (impl/resource-obj* `Matrix4-Ctor 0 body))

(def Box2-Ctor #?(:cljs three/Box2))
(defmacro Box2 [& body] (impl/resource-obj* `Box2-Ctor 2 body))

(def Box3-Ctor #?(:cljs three/Box3))
(defmacro Box3 [& body] (impl/resource-obj* `Box3-Ctor 2 body))

(def Sphere-Ctor #?(:cljs three/Sphere))
(defmacro Sphere [& body] (impl/resource-obj* `Sphere-Ctor 2 body))

(def Plane-Ctor #?(:cljs three/Plane))
(defmacro Plane [& body] (impl/resource-obj* `Plane-Ctor 2 body))

(def Ray-Ctor #?(:cljs three/Ray))
(defmacro Ray [& body] (impl/resource-obj* `Ray-Ctor 2 body))

(def Line3-Ctor #?(:cljs three/Line3))
(defmacro Line3 [& body] (impl/resource-obj* `Line3-Ctor 2 body))

(def Triangle-Ctor #?(:cljs three/Triangle))
(defmacro Triangle [& body] (impl/resource-obj* `Triangle-Ctor 3 body))


;; Loaders
(def TextureLoader-Ctor #?(:cljs three/TextureLoader))
(defmacro TextureLoader [& body] (impl/resource-obj* `TextureLoader-Ctor 1 body))

(def CubeTextureLoader-Ctor #?(:cljs three/CubeTextureLoader))
(defmacro CubeTextureLoader [& body] (impl/resource-obj* `CubeTextureLoader-Ctor 1 body))

(def ObjectLoader-Ctor #?(:cljs three/ObjectLoader))
(defmacro ObjectLoader [& body] (impl/resource-obj* `ObjectLoader-Ctor 1 body))

(def MaterialLoader-Ctor #?(:cljs three/MaterialLoader))
(defmacro MaterialLoader [& body] (impl/resource-obj* `MaterialLoader-Ctor 1 body))

(def BufferGeometryLoader-Ctor #?(:cljs three/BufferGeometryLoader))
(defmacro BufferGeometryLoader [& body] (impl/resource-obj* `BufferGeometryLoader-Ctor 1 body))

(def AudioLoader-Ctor #?(:cljs three/AudioLoader))
(defmacro AudioLoader [& body] (impl/resource-obj* `AudioLoader-Ctor 1 body))

(def ImageLoader-Ctor #?(:cljs three/ImageLoader))
(defmacro ImageLoader [& body] (impl/resource-obj* `ImageLoader-Ctor 1 body))

(def FileLoader-Ctor #?(:cljs three/FileLoader))
(defmacro FileLoader [& body] (impl/resource-obj* `FileLoader-Ctor 1 body))

(def GLTFLoader-Ctor #?(:cljs three/GLTFLoader))
(defmacro GLTFLoader [& body] (impl/resource-obj* `GLTFLoader-Ctor 1 body))

(def DRACOLoader-Ctor #?(:cljs three/DRACOLoader))
(defmacro DRACOLoader [& body] (impl/resource-obj* `DRACOLoader-Ctor 1 body))

(def FontLoader-Ctor #?(:cljs three/FontLoader))
(defmacro FontLoader [& body] (impl/resource-obj* `FontLoader-Ctor 1 body))

(defn vector3 [x y z]
  #?(:cljs (three/Vector3. x y z)))

(defn vector2 [x y]
  #?(:cljs (three/Vector2. x y)))

(defn vector4 [x y z w]
  #?(:cljs (three/Vector4. x y z w)))

(defn euler [x y z order]
  #?(:cljs (three/Euler. x y z order)))

(def backSide #?(:cljs three/BackSide))
(def frontSide #?(:cljs three/FrontSide))
(def doubleSide #?(:cljs three/DoubleSide))

(def linearMapping #?(:cljs three/LinearMapping))
(def nearestFilter #?(:cljs three/NearestFilter))
(def linearFilter #?(:cljs three/LinearFilter))

(def repeatWrapping #?(:cljs three/RepeatWrapping))
(def clampToEdgeWrapping #?(:cljs three/ClampToEdgeWrapping))
(def mirroredRepeatWrapping #?(:cljs three/MirroredRepeatWrapping))

(def nearestMipmapNearestFilter #?(:cljs three/NearestMipmapNearestFilter))
(def nearestMipmapLinearFilter #?(:cljs three/NearestMipmapLinearFilter))
(def linearMipmapNearestFilter #?(:cljs three/LinearMipmapNearestFilter))
(def linearMipmapLinearFilter #?(:cljs three/LinearMipmapLinearFilter))