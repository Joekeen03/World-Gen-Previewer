package main.java.ruby_phantasia.world_gen_previewer;

import com.jme3.scene.Geometry;
import com.jme3.system.AppSettings;
import com.jme3.system.JmeContext;
import com.jme3.system.lwjgl.LwjglWindow;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitive;
import main.java.ruby_phantasia.world_gen_previewer.jmeBackend.JMEBackend;
import main.java.ruby_phantasia.world_gen_previewer.lwjglBackend.LWJGLBackend;
import main.java.ruby_phantasia.world_gen_previewer.spheroidGenerator.SphereGenerator;

public class Main {
    public static void main(String[] args) {
//        new LWJGLBackend().Run();

        SphereGenerator generator = new SphereGenerator(1, 100);
        GenerationPrimitive[] spheres = generator.GenerateSpheres();
        JMEBackend app = new JMEBackend(spheres);
        AppSettings settings = new AppSettings(true);
        settings.setTitle("World Generation Previewer");

        settings.setResizable(true);
        app.setSettings(settings);
        app.setShowSettings(false); // Disables initial setting screen
        app.start();

    } // main
}
