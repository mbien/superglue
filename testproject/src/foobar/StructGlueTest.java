package foobar;
import com.mbien.structgen.Struct;

/**
 * @author Michael Bien
 */
public class StructGlueTest {

//    @Struct(name="RenderingConfig", header="struct.h")
//    MyRenderingConfig config;

    @Struct(header="struct.h")
    RenderingConfig config2;

    @Struct(header="struct.h")
    RenderingConfig config3;

    //already recursively generated via RenderingConfig
    Vec vec;
    Camera cam;

    @Struct(header="struct.h")
    Vec vec2;

    public StructGlueTest() {
        @Struct(header="struct.h")
        RenderingConfig config4 = RenderingConfig.create();

    }

}