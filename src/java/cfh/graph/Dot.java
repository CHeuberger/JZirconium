package cfh.graph;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.plaf.synth.SynthSeparatorUI;

public final class Dot {

    static final String CMD;
    static {
        String cmd = System.getProperty("Graph");
        if (cmd == null) {
            String path = System.getenv("GRAPHVIZ_HOME");
            if (path == null) {
                cmd = "/usr/local/bin/dot";
                System.err.println("neither \"Graph\" nor \"GRAPHVIZ_HOME\" environmrnt variables set, using \"" + cmd + "\"");
            } else {
                cmd = path + "/bin/dot";
            }
        }
        CMD = cmd;
    }

    public static void dot(String format, InputStream dotInput, OutputStream output) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(new String[] { CMD, "-T" + format });

        try (OutputStream processIn = process.getOutputStream()) {
            writeAll(dotInput, processIn);
        }

        try (InputStream processOut = process.getInputStream()) {
            writeAll(processOut, output);
        }

        int ret = process.waitFor();
        if (ret != 0)
            throw new RuntimeException("dot to " + format + " conversion failed, returned: " + ret);
    }
    
    public static void dotToSvg(InputStream dotInput, OutputStream svgOutput) throws IOException, InterruptedException {
        dot("svg", dotInput, svgOutput);
    }
    
    public static void dotToPng(InputStream dotInput, OutputStream pngOutput) throws IOException, InterruptedException {
        dot("png", dotInput, pngOutput);
    }
    
    private static void writeAll(InputStream input, OutputStream output) throws IOException {
        byte[] buffer = new byte[4096];

        int count;
        while ((count = input.read(buffer)) != -1) {
            output.write(buffer, 0, count);
        }
    }
}
