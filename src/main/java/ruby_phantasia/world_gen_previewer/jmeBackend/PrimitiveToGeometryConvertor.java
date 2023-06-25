package main.java.ruby_phantasia.world_gen_previewer.jmeBackend;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Sphere;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitive;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitiveVisitor;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationSphere;

import java.util.ArrayList;

public class PrimitiveToGeometryConvertor implements GenerationPrimitiveVisitor<Geometry> {
    final AssetManager assetManager;

    public PrimitiveToGeometryConvertor(AssetManager assetManager) {
        this.assetManager = assetManager;
    }

    private Material CreateSimpleLitMaterial(ColorRGBA color) {
        Material material = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        material.setBoolean("UseMaterialColors", true);
        material.setColor("Ambient", color);
        material.setColor("Diffuse", color);
        return material;
    }

    @Override
    public Geometry visit(GenerationSphere sphere) {
        Sphere sphereMesh = new Sphere(20, 20, sphere.radius);
        Geometry sphereGeometry = new Geometry("Sphere", sphereMesh);
        sphereGeometry.setLocalTranslation(sphere.position.x(), sphere.position.y(), sphere.position.z());
        sphereGeometry.setMaterial(CreateSimpleLitMaterial(JMEUtility.ConvertVec4fcToColorRGBA(sphere.color)));
        return sphereGeometry;
    }
}