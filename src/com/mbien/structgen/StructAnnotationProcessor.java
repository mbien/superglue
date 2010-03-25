/*
 * Created on Tuesday, March 23 2010 at 18:45
 */
package com.mbien.structgen;

import com.sun.gluegen.GlueGen;
import com.sun.gluegen.JavaEmitter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.tools.FileObject;
import javax.tools.StandardLocation;

import static java.io.File.*;

/**
 * 
 * @author Michael Bien
 */
@SupportedAnnotationTypes(value = {"com.mbien.structgen.Struct"})
@SupportedSourceVersion(SourceVersion.RELEASE_5)
public class StructAnnotationProcessor extends AbstractProcessor {

    private static final String DEFAULT = "_default_";

    private Filer filer;
    private Messager messager;
    private Elements eltUtils;
    private String outputDir;

    private final static Set<String> generatedStructs = new HashSet<String>();

    public StructAnnotationProcessor() {
    }

    @Override
    public void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);

        filer    = processingEnv.getFiler();
        messager = processingEnv.getMessager();
        eltUtils = processingEnv.getElementUtils();

        outputDir= processingEnv.getOptions().get("structoutputdir");
        outputDir= outputDir == null ? "gensrc" : outputDir;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

        Set<? extends Element> elements = env.getElementsAnnotatedWith(Struct.class);

        for (Element element : elements) {

            String pakage = eltUtils.getPackageOf(element).toString();

            try {
                Struct struct = element.getAnnotation(Struct.class);
                String headerRelPath = struct.header();
                // yes, this is a hack but there are simple to many bugs in this api.
                // e.g.: 6647998
                FileObject header = filer.getResource(StandardLocation.SOURCE_PATH, pakage, headerRelPath);

                File headerFile = null;
                String root = null;
                if(header.toUri().isAbsolute()) {
                    headerFile = new File(header.toUri());
                }else{
                    root = System.getProperty("user.dir");
                    headerFile = new File(root + separator + header.toUri());
                }
                
                System.out.println();
                System.out.println(" - - - - > "+header.toUri());
                System.out.println();

                generateStructBinding(element, struct, root, pakage, headerFile);
            } catch (IOException ex) {
                throw new RuntimeException("IOException encountered, run if you can!", ex);
            }

        }
        return true;

    }

    private void generateStructBinding(Element element, Struct struct, String root, String pakage, File header) throws IOException {

        String declaredType = element.asType().toString();
        String structName   = struct.name().equals(DEFAULT) ? declaredType : struct.name();

        if(generatedStructs.contains(structName)) {
            return;
        }

        System.out.println("generating struct accessor for struct: "+structName);

        generatedStructs.add(structName);

        String output       = root   + separator + outputDir;
        String config       = output + separator + header.getName() + ".cfg";

        File configFile = new File(config);

        FileWriter writer = null;
        try{
            writer = new FileWriter(configFile);
            writer.write("Package "+pakage+"\n");
            writer.write("EmitStruct "+structName+"\n");
            if(!struct.name().equals(DEFAULT)) {
                writer.write("RenameJavaType " + struct.name()+" " + declaredType +"\n");
            }
        }finally{
            if(writer != null) {
                writer.close();
            }
        }

        //TODO this isn't very clean since we won't get any exceptions this way
        GlueGen.main(
//                "-I"+path+"/build/",
                "-O" + output,
                "-E" + AnnotationProcessorJavaStructEmitter.class.getName(),
                "-C" + config,
                header.getPath());

        configFile.delete();

    }

    public static class AnnotationProcessorJavaStructEmitter extends JavaEmitter {

        @Override
        protected PrintWriter openFile(String filename) throws IOException {

            //look for recursive generated structs... keep it DRY
            if(   !filename.endsWith("32.java")
               && !filename.endsWith("64.java") ) {

                //TODO remove this hack by passing an instance to gluegen instead of the class
                String name = filename.substring(filename.lastIndexOf(separator)+1, filename.length()-5);
                System.out.println("generating -> " + name);
                generatedStructs.add(name);

            }

            return super.openFile(filename);
        }
        
    }

}
