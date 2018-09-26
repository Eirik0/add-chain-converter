package addchain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class AddChainMain {
	// XXX Set `VERBOSE` to false if mul and square are implemented
	public static final boolean VERBOSE = false;
	// XXX Change this when a square function is implemented
	public static final String SQUARE_FUNCTION_STRING = "square";
	// XXX Change this depending on how multiple squarings is implemented
	public static final String SQUARE_ASSIGN_MULTI_FUNCTION_STRING = "square_assign_multi";

	public static void main(String[] args) {
		List<String> chainLines;
		try {
			chainLines = streamToList(AddChainMain.class.getResourceAsStream("/addchain_in.txt"));
		} catch (IOException e) {
			throw new RuntimeException("Could not load file", e);
		}
		
		System.out.println(AddChainLine.convertToRust(chainLines, VERBOSE));
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
