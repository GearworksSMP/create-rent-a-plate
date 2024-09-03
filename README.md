# Create: Rent-A-Plate

This mod allows you to place down rentable warp plates in creative mode. A player in survival mode can then rent the warp plate. When paying the specified amount the player will receive a return plate which they can place anywhere in the world. Once both plates are placed any player can teleport between them. 

Plates are only active for a specified duration and the return plate is removed after expiration. Players can renew the plates a specified duration before expiry.

## Technical notes
- A warp plate pair is a warp plate + return plate
  - Pairs are stored in `WarpPlatesSavedData`
  - When you place a return plate, the saved data is updated with the position of the return plate.
- `PlateBlock` is the abstract class parent of both the warp plate (the rentable part) and the return plate.
- The original block bench models for the plates are in the root of the repository.
- Create is included in the dev env since the models use its textures