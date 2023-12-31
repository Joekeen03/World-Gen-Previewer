package main.java.ruby_phantasia.world_gen_previewer.api;

public interface GenerationPrimitiveVisitor<T> {
    T visit(GenerationSphere sphere);
    T visit(GenerationCylinder_EndOrigin cylinder);
    T visit(GenerationBox box);
}
