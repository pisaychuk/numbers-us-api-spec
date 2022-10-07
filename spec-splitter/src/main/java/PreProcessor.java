import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class PreProcessor {

    public static void main(String[] args) throws Exception {
        RemoveInternalFields preprocessor = new RemoveInternalFields();

        final CommandLine cmd = parseCmdArgs(args);
        String readFilePath = cmd.getOptionValue("r");
        String writeFilePath = cmd.getOptionValue("w");
        boolean internalDoc = cmd.hasOption("i");
        boolean addXmlDeclaration = cmd.hasOption("d");

        preprocessor.removeInternalData(readFilePath, writeFilePath, internalDoc, addXmlDeclaration);
    }

    private static CommandLine parseCmdArgs(String[] args) {
        Options options = new Options();

        Option input = new Option("r", "readFile", true, "Read file to process (full path)");
        input.setRequired(true);
        options.addOption(input);

        Option output = new Option("w", "writeFile", true, "Save result to file (full path)");
        output.setRequired(true);
        options.addOption(output);

        Option internalDoc = new Option("i", "internalDoc", false, "Create internal doc");
        options.addOption(internalDoc);

        Option addXmlDeclaration = new Option("d", "declaration", false, "Add default XML declaration");
        options.addOption(addXmlDeclaration);

        try {
            CommandLineParser parser = new DefaultParser();
            return parser.parse(options, args);
        } catch (ParseException e) {
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("Api Doc Preprocessor arguments", options);
            System.exit(1);
        }
        return null;
    }
}
