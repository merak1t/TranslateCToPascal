import java.util.stream.Collectors;

public class Visitor extends genBaseVisitor<String> {
    final String TAB = "\t";

    private String read(genParser.CommandContext ctx) {
        var res = new StringBuilder();
        res.append("scanf(\"");
        for (var node : ctx.NAME())
            res.append("%d");
        if (ctx.start.getText().equals("readln")) res.append("\\n");

        res.append("\", ");
        res.append(ctx.NAME()
                .stream()
                .map(t -> "&" + t.getSymbol().getText())
                .collect(Collectors.joining(", ")));
        res.append(");");
        return res.toString();
    }

    private String write(genParser.CommandContext ctx) {
        StringBuilder res = new StringBuilder();

        res.append("printf(\"");
        for (var tree : ctx.children) {
            if (tree.getPayload() instanceof genParser.ArithmeticContext) {
                res.append("%d");
            } else if (tree.getText().startsWith("'")) {
                res.append(tree.getText().replace("'", ""));
            }
        }
        if (ctx.start.getText().equals("writeln")) res.append("\\n");

        res.append("\"");
        for (var tree : ctx.children) {
            if (tree.getPayload() instanceof genParser.ArithmeticContext) {
                res.append(", ").append(tree.getText());
            }
        }
        res.append(");");
        return res.toString();
    }

    @Override
    public String visitCommand(genParser.CommandContext ctx) {
        switch (ctx.start.getText()) {
            case "read":
            case "readln":
                return read(ctx);
            case "write":
            case "writeln":
                return write(ctx);
            default:
                return (ctx.getText() + ";");

        }
    }

    public String visitCommands(genParser.CommandsContext ctx) {
        var res = new StringBuilder();
        for (var commandContext : ctx.command()) {
            res.append(TAB).append(visitCommand(commandContext)).append("\n");
        }
        return res.toString();
    }

    public String visitProgram(genParser.ProgramContext ctx) {
        var res = new StringBuilder();
        if (ctx.VAR() != null) {
            res = new StringBuilder(visitVariables(ctx.variables()));
            res.append("\n");
        }
        for (var context : ctx.function()) {
            res.append(visitFunction(context));
            res.append("\n\n");
        }
        res.append("int main() {\n");
        res.append(visitCommands(ctx.commands()));
        res.append(TAB + "return 0;\n}\n");
        return res.toString();
    }

    public String visitDeclaration(genParser.DeclarationContext ctx, boolean separate) {
        var res = new StringBuilder();
        if (!separate) {
            res.append(visitType(ctx.type()));
            res.append(ctx.NAME().stream().map(t -> t.getSymbol().getText()).collect(Collectors.joining(", ")));
        } else {
            res.append(ctx.NAME().stream().map(t -> visitType(ctx.type()) + t.getSymbol().getText())
                    .collect(Collectors.joining(", ")));
        }
        return res.toString();
    }

    public String visitType(genParser.TypeContext ctx) {
        var res = new StringBuilder();
        switch (ctx.getText()) {
            case "integer": {
                res.append("short ");
                break;
            }
            case "longint": {
                res.append("long ");
                break;
            }
            case "shortint": {
                res.append("char ");
                break;
            }
            case "real": {
                res.append("double ");
                break;
            }
            default:
                System.err.println(("unexpected " + ctx.getText()));
        }
        return res.toString();
    }

    public String visitVariables(genParser.VariablesContext ctx) {
        var res = new StringBuilder();
        for (var context : ctx.declaration()) {
            res.append(visitDeclaration(context, false)).append(";\n");
        }
        return res.toString();
    }

    public String visitFunction(genParser.FunctionContext ctx) {
        return visitHeader(ctx.header()) +
                "\n" +
                visitVariables(ctx.variables()) +
                visitCommands(ctx.commands()) +
                TAB + "return " +
                ctx.returnValue().arithmetic().getText() +
                ";\n}";
    }

    public String visitHeader(genParser.HeaderContext ctx) {
        return visitType(ctx.type()) +
                ctx.NAME().getSymbol().getText() +
                "(" +
                ctx.declaration().stream().map(d -> visitDeclaration(d, true)).collect(Collectors.joining(", ")) +
                ") {";
    }
}