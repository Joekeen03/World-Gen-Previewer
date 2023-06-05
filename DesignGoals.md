Desired goals/features (in no particular order):

* Mouse control of camera - Minecraft-style, with cursor locked to center, and movement away from center moving the camera around (w/ same restrictions on up/down angle as MC)
* Ability to pause mouse control of camera by pressing Escape; clicking on window restarts mouse control
  * Kinda a precursor to an eventual GUI
* Ability to lock camera in orbit mode, where it orbits a specific point.
* GUI where you can change generation parameters - so you don't have to recompile every time a variable's value is tweaked.
* Ability to reload generation code, without closing the renderer
  * Exception safety - exceptions in the generation code do not crash the renderer
* Screen-captures - can take a screenshot of the renderer's output and save it to an image, upon pressing F2 (MC shortcut)
* Ability to generate and render an entire structure
  * Ability to generate structure in full, without worrying about cube-by-cube generation (i.e. a single generate() method that fills a monolithic voxel array representing the entire world)
  * Ability to emulate CubicChunk's generation methodology in full, generating the structure cube-by-cube; i.e. the generator makes a generateCube method available, which generates a specified cube (16^3 blocks) as it would in CubicChunks
    * Initially, just want to imitate the basic methodology, so I can test the overarching logic of the cube-by-cube generation
    * Eventually, would like to perfectly imitate the CubicChunks interface, such that you could just copy the code into a mod. Or maybe make it so the renderer can actually load a mod?
  * Possibly a way to render a "primitive" version of a structure - a structure formed of shapes other than blocks, so you can get the basic details of the structure right w/o having to worry about voxelizing.
    * Could be combined with a voxelizeMesh() method, which takes this raw mesh and converts it to bloxels?
  * Possibly a way to display different versions of a structure side-by-side, like the version created via generate() and the version created by generateCube(), so you can identify discrepancies in the structure
* Ability to render chunk/cube boundaries
* Bloxel renderer
  * Initially, just be able to take an array specifying the cube at each location, and render it as a series of cubes, one for each location w/ a block.
  * Then optimize it so it culls faces that are interior - only visible from the outside
  * Then optimize it further, so it merges various triangles into fewer larger triangles.
* Imitate how MC renders blocks
  * Ability to render textures on blocks
  * Ability to render transparent textures on blocks
  * Render lighting
    * Start with skylight
    * Then blocklight
  * Rendering block models?
* Way to render mob models, so you can see what it might look like with mobs on it?
* Ability to render a floor
* Flexibility in screen sizes:
  * Windowed - can be resized
  * Fullscreen - both exclusive and borderless
* Anti-aliasing

Iffy:
* *Maybe* a way to add/remove blocks, to get an idea of what a new feature could look like?

Some of these feel a bit out of the scope of what I want - a simple, quick, practical way to design large-scale structures, and test their generation algorithms.

[CURRENT] Basic functionality:
* [NYI] Render bloxels with simple textures (no transparency)
* [IN-PROGRESS] Basic mouse control of camera.
* [IN-PROGRESS] Ability to render primitives
* [NYI] Ability to generate a structure in full (primitives)
* [DONE] Window locked to maximized window.

Next level:
* Pause mouse control
* Ability to voxelize a mesh of primitives
* Screen captures?

Third level:
* Ability to generate a structure in full (bloxelized)
* Render chunk/cube boundaries
* 