Quick overview for anyone else who works on this codebase in the future:

- A warp plate pair is a warp plate + return plate
  - Pairs are stored in `WarpPlatesSavedData`
  - When you place a return plate, the saved data is updated with the position of the return plate.
- `PlateBlock` is the abstract class parent of both the warp plate (the rentable part) and the return plate.
- The original block bench models for the plates are in the root of the repository.
- Create is included in the dev env since the models use its textures
