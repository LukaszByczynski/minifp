# minifp - off time toy project
---

Simple toy project created to fully understand all hidden mechanisms behind all modern fp libraries like scalaz, cats, zio, ...

Roadmap:

- [ ] Stack safe IO monad with following capitablities:
  - [ ] pure
  - [ ] effect
  - [ ] map
  - [ ] mapError
  - [ ] raise
  - [ ] attempt
  - [ ] flatMap
  - [ ] ifM
  - [ ] foldM
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
