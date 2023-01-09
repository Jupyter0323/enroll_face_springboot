package tutorials.biometricss;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NFace;
import com.neurotec.biometrics.NLAttributes;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.client.NBiometricClient;
import com.neurotec.licensing.NLicense;
import com.neurotec.licensing.NLicenseManager;
import com.sun.jna.Platform;
import org.springframework.util.Base64Utils;
import tutorials.util.LibraryManager;
import tutorials.util.Utils;

import java.io.File;

public class EnrollFaceFromImage {
	private static boolean libraryLoaded = false;

	private static String prefixPath = Platform.isWindows() ? "D:\\biometricss\\" : "/opt/biometricss/";

	public static String getUploadImagePath(String fileName) {
		String uploadPath = prefixPath + "tmp";
		File folder = new File(uploadPath);
		if (!folder.exists()) {
			folder.mkdirs();
		}

		return uploadPath + Utils.FILE_SEPARATOR + fileName;
	}

	public static boolean loadEngine() {
		if (libraryLoaded) {
			return true;
		}

		try {
			if (Platform.isWindows()) {
				LibraryManager.initLibraryPath(prefixPath + "WIN64_X64");
			} else if (Platform.isLinux()) {
				LibraryManager.initLibraryPath(prefixPath + "Linux_x86_64");
			}

			NLicenseManager.setTrialMode(true);
			System.out.println("\tTrial mode: " + true);

			final String license = "FaceExtractor";
			// Obtain license
			if (!NLicense.obtain("/local", 5000, license)) {
				System.err.format("Could not obtain license: %s%n", license);
				System.exit(-1);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}

		libraryLoaded = true;
		return true;
	}

	public byte[] recognizeFile(String fileName) {
		if (libraryLoaded == false) {
			return null;
		}

		NBiometricClient biometricClient = null;
		NSubject subject = null;
		NFace face = null;

		try {
			biometricClient = new NBiometricClient();
			subject = new NSubject();
			face = new NFace();

			// Set file name with face image
			face.setFileName(fileName);

			subject.getFaces().add(face);

			// Detect all faces features
			biometricClient.setFacesDetectAllFeaturePoints(false);
			NBiometricStatus status = biometricClient.createTemplate(subject);

			if (subject.getFaces().size() > 1)
				System.out.format("Found %d faces\n", subject.getFaces().size() - 1);

			// List attributes for all located faces
			for (NFace nface : subject.getFaces()) {
				for (NLAttributes attribute : nface.getObjects()) {

					System.out.println("Face:");
					System.out.format("\tLocation = (%d, %d), width = %d, height = %d\n", attribute.getBoundingRect().getBounds().x, attribute.getBoundingRect().getBounds().y,
							attribute.getBoundingRect().width, attribute.getBoundingRect().height);

					if ((attribute.getRightEyeCenter().confidence > 0) || (attribute.getLeftEyeCenter().confidence > 0)) {
						System.out.println("\tFound eyes:");
						if (attribute.getRightEyeCenter().confidence > 0) {
							System.out.format("\t\tRight: location = (%d, %d), confidence = %d%n", attribute.getRightEyeCenter().x, attribute.getRightEyeCenter().y,
									attribute.getRightEyeCenter().confidence);
						}
						if (attribute.getLeftEyeCenter().confidence > 0) {
							System.out.format("\t\tLeft: location = (%d, %d), confidence = %d%n", attribute.getLeftEyeCenter().x, attribute.getLeftEyeCenter().y,
									attribute.getLeftEyeCenter().confidence);
						}
					}
					if (false) {
						if (attribute.getNoseTip().confidence > 0) {
							System.out.println("\tFound nose:");
							System.out.format("\t\tLocation = (%d, %d), confidence = %d%n", attribute.getNoseTip().x, attribute.getNoseTip().y, attribute.getNoseTip().confidence);
						}
						if (attribute.getMouthCenter().confidence > 0) {
							System.out.println("\tFound mouth:");
							System.out.printf("\t\tLocation = (%d, %d), confidence = %d%n", attribute.getMouthCenter().x, attribute.getMouthCenter().y, attribute.getMouthCenter().confidence);
						}
					}
				}
			}

			if (status == NBiometricStatus.OK) {
				System.out.println("Template extracted");
				return Base64Utils.encode(subject.getTemplate().save().toByteArray());
			} else {
				System.out.format("Extraction failed: %s%n", status.toString());
				System.exit(-1);
			}
		} catch (Throwable th) {
			Utils.handleError(th);
		} finally {
			if (face != null) face.dispose();
			if (subject != null) subject.dispose();
			if (biometricClient != null) biometricClient.dispose();
		}

		return null;
	}
}
