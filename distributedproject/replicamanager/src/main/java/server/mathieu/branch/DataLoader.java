package server.mathieu.branch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class DataLoader {

	private DataLoader() {

	};

	/**
	 * Load the given csv file (; separator) with Customer Records into the
	 * branch.
	 * 
	 * @param branch
	 * @param inputFile
	 *            CSV file of the form (firstName;lastName;address;phone;
	 *            accountNumber;accountTotal)
	 * @return
	 */
	public static void loadCustomerRecord(BranchImpl branch, String inputFileName) {
		try {
			File inputFile = new File(inputFileName);
			Files.lines(inputFile.toPath()).map(line -> Arrays.asList(line.split(";"))).forEach(fields -> {
				CustomerRecord record = new CustomerRecord(fields.get(0), fields.get(1), fields.get(2), fields.get(3),
						Integer.parseInt(fields.get(4)));
				branch.loadCustomerRecord(record);
				BranchImpl.logger.println("Loading record from file " + fields.toString());
			});
		} catch (IOException e) {
			e.printStackTrace();
		}

		return;
	}

	public static void loadManager(BranchImpl branch, String inputFileName) {
		try {
			File inputFile = new File(inputFileName);
			Files.lines(inputFile.toPath()).forEach(number -> branch.loadManagerRecord(number));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
