package main.java.ruby_phantasia.world_gen_previewer.api;

/**
 * Simple primitives, with one alpha value that controls the primitive's overall transparency.
 */
public abstract class GenerationPrimitive {
    public final float alpha;


    public GenerationPrimitive( float alpha) {
        this.alpha = alpha;
    }

    public abstract <T> T accept(GenerationPrimitiveVisitor<T> visitor);
}
