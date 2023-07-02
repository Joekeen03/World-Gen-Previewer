## Desired goals/features (in no particular order):

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
    * Ideally be able to do a "diff", which highlights (or only shows) cubes that differ between the two methods.
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
* Turn on/off different "layers" - so you could toggle primitives and per-cube bloxels on/off individual (for example), with one/both/none rendering.
* Ability to turn on/off parts of a given "layer", so you could say have generation features and bounding boxes for those features both render, and toggle the bounding boxes on/off
  * Or divide the primitives in a given layer into chunks, which you can turn on/off
  * Most flexible - likely "tags" tied to each primitive
  * Ideally, would be able to do this for voxel - how? Would probably want to tie them to "source" primitives
* Ability to switch between different generation phases, particularly for the voxelized structure
  * So for my giga-tree mod, you could turn the bark placement phase on/off, seeing how the tree looks before & after bark is placed
* For cube-based generator, control over which cubes generate; so you could get a cross-section of certain cubes (or have very large structures that you only generate part of?)
  * Or maybe just toggle certain cubes on/off
  * Also, ability to control order in which cubes generate - to test if your code has any dependencies.
  *  [Iffy] Ability to generate cubes over time - so at second 1, cubes 1-3 are generated, second 2 cubes 4-6, etc.
* Ability to render terrain meshes

Iffy:
* *Maybe* a way to add/remove blocks, to get an idea of what a new feature could look like?

Some of these feel a bit out of the scope of what I want - a simple, quick, practical way to design large-scale structures, and test their generation algorithms.