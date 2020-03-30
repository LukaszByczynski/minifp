# minifp - off time toy project

---

Simple toy project created to fully understand all hidden mechanisms behind all modern fp libraries like scalaz, cats, zio, ...

Roadmap:

- [x] Stack safe IO monad with following capitablities:
  - [x] bi-functor
  - [x] pure
  - [x] effect
  - [x] map
  - [x] mapError
  - [x] raise
  - [x] attempt
  - [x] flatMap
  - [x] ifM
  - [x] unit
  - [x] _>, _<, <\*>
- [ ] Core FP typeclasses like:
  - [ ] Monoid
  - [ ] Functor
  - [ ] Applicative
  - [ ] Monad
  - [ ] MonadError
- [ ] Partial MTL:
  - [ ] MonadAsk
  - [ ] MonadTell
  - [ ] MonadState
- [ ] Second wave of the IO improvments like:
  - [ ] bracket
  - [ ] async
  - [ ] switch
  - [ ] fibers
