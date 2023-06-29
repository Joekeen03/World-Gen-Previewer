package main.java.ruby_phantasia.world_gen_previewer;

import com.jme3.app.SimpleApplication;
import com.jme3.system.AppSettings;
import main.java.ruby_phantasia.world_gen_previewer.api.GenerationPrimitive;
import main.java.ruby_phantasia.world_gen_previewer.generators.Generator;
import main.java.ruby_phantasia.world_gen_previewer.jmeBackend.JMEBackend;
import main.java.ruby_phantasia.world_gen_previewer.generators.SphereGenerator;

public class Main {
    public static void main(String[] args) {
//        new LWJGLBackend().Run();

        Generator generator = new SphereGenerator(1, 100);
//        Generator generator = new TestGenerator();

        GenerationPrimitive[] spheres = generator.GeneratePrimitives();
        SimpleApplication app = new JMEBackend(spheres);

//        SimpleApplication app = new JMEBloxelApp();

        AppSettings settings = new AppSettings(true);
        settings.setTitle("World Generation Previewer");

        settings.setResizable(true);
        app.setSettings(settings);
        app.setShowSettings(false); // Disables initial setting screen
        app.start();

    } // main
}
