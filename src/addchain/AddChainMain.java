package addchain;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddChainMain {
	// XXX Change this when a square function is implemented
	public static final String SQUARE_FUNCTION_STRING = "square";
	// XXX Change this depending on how multiple squarings is implemented
	public static final String SQUARE_ASSIGN_MULTI_FUNCTION_STRING = "square_assign_multi";

	public static void main(String[] args) {
		// Get arguments
		if (args.length < 1) {
			System.err.println("Error: Expected file path for input.");
			System.err.println("Try:");
			System.err.println("    java -jar add-chain-converter.jar ./chains/Fq.txt");
			System.err.println("OR");
			System.err.println("    java -jar add-chain-converter.jar ./chains/Fq.txt --verbose");
			System.exit(0);
		}
		String chainFilePath = args[0];

		boolean verbose = false;
		// passing --verbose will assume that `mul` and `square` functions do not exist
		if (args.length > 1) {
			if (args[1].toLowerCase().equals("--verbose")) {
				verbose = true;
			}
		}

		// Load file
		List<String> chainLines;
		try {
			chainLines = streamToList(new FileInputStream(new File(chainFilePath)));
		} catch (IOException e) {
			throw new RuntimeException("Could not load file", e);
		}

		// Generate output
		System.out.println("");
		System.out.println(AddChainLine.convertToRust(chainLines, verbose));
	}

	public static List<String> streamToList(InputStream stream) throws IOException {
		try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream))) {
			List<String> result = new ArrayList<>();

			String line;
			while ((line = reader.readLine()) != null) {
				result.add(line);
			}

			return result;
		}
	}
}
