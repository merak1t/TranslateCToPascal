import org.antlr.v4.runtime.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class Main {

    public static void main(String[] args) throws IOException {
        genLexer lexer = new genLexer(CharStreams.fromFileName("test.txt"));
        TokenStream tokens = new CommonTokenStream(lexer);
        genParser parser = new genParser(tokens);
        Visitor v = new Visitor();
        System.out.println(v.visitProgram(parser.program()));
    }
}