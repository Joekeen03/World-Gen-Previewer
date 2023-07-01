package main.java.ruby_phantasia.world_gen_previewer.jmeBackend;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.material.RenderState;
import com.jme3.math.ColorRGBA;
import com.jme3.renderer.queue.RenderQueue;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.scene.shape.Cylinder;
import com.jme3.scene.shape.Sphere;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationBox;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationCylinder_EndOrigin;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitiveVisitor;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationSphere;
import main.java.ruby_phantasia.world_gen_previewer.helper.DefaultVectors;
import org.joml.Vector3f;
import org.joml.Vector3fc;

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
        sphereGeometry.setMaterial(CreateSimpleLitMaterial(JMEUtility.ConvertVec3fcAndAlphaToColorRGBA(sphere.color, sphere.alpha)));
        return sphereGeometry;
    }

    @Override
    public Geometry visit(GenerationCylinder_EndOrigin cylinder) {
        Cylinder cylinderMesh = new Cylinder(20, 20, cylinder.radius, cylinder.length, true);
        Geometry cylinderGeometry = new Geometry("Cylinder", cylinderMesh);
        // A JME cylinder's position is the location of its volumetric center, not the center of an end.
        Vector3f center = DefaultVectors.Z_POSITIVE.mul(cylinder.length/2, new Vector3f())
                .rotate(cylinder.rotation).add(cylinder.endPosition);
        SetLocalTranslationFromJOMLVector3fc(cylinderGeometry, center);
        cylinderGeometry.setLocalRotation(JMEUtility.ConvertJOMLQuaternionToJMEQuaternion(cylinder.rotation));
        cylinderGeometry.setMaterial(CreateSimpleLitMaterial(JMEUtility.ConvertVec3fcAndAlphaToColorRGBA(cylinder.color, cylinder.alpha)));
        return cylinderGeometry;
    }

    @Override
    public Geometry visit(GenerationBox box) {
        Box boxMesh = new Box(box.dimensions.x()/2, box.dimensions.y()/2, box.dimensions.z()/2);
        Geometry boxGeometry = new Geometry("Box", boxMesh);
        SetLocalTranslationFromJOMLVector3fc(boxGeometry, box.position);
        boxGeometry.setMaterial(CreateSimpleLitMaterial(JMEUtility.ConvertVec3fcAndAlphaToColorRGBA(box.color, box.alpha)));
        return boxGeometry;
    }

    private static void SetLocalTranslationFromJOMLVector3fc(Geometry geometry, Vector3fc position) {
        geometry.setLocalTranslation(position.x(), position.y(), position.z());
    }
}