package main.java.ruby_phantasia.world_gen_previewer.api;

public interface GenerationPrimitive {
    public <T> T accept(GenerationPrimitiveVisitor<T> visitor);
}
