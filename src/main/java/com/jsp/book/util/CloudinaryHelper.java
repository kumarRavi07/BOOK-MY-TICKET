package com.jsp.book.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

@Component
public class CloudinaryHelper {

	private static final String MOVIE_FOLDER = "BMT-Movies";
	private static final String THEATER_FOLDER = "BMT-Theater";
	private static final String QR_FOLDER = "BMT-Theater-QR";

	private static final String MOVIE_FALLBACK_IMAGE = "/poster-placeholder.svg";
	private static final String THEATER_FALLBACK_IMAGE = "/theater-placeholder.svg";
	private static final String QR_FALLBACK_IMAGE = "/qr-placeholder.svg";

	private final Cloudinary cloudinary;
	private final Path uploadRoot;

	public CloudinaryHelper(@Value("${cloudinary.url:}") String cloudinaryUrl,
			@Value("${app.upload-dir:uploads}") String uploadDir) {
		if (cloudinaryUrl != null && !cloudinaryUrl.trim().isEmpty()) {
			this.cloudinary = new Cloudinary(cloudinaryUrl);
		} else {
			this.cloudinary = null;
		}
		this.uploadRoot = Paths.get(uploadDir).toAbsolutePath().normalize();
	}

	public String generateImageLink(MultipartFile file) {
		return upload(file, MOVIE_FOLDER, MOVIE_FALLBACK_IMAGE);
	}

	public String getTheaterImageLink(MultipartFile file) {
		return upload(file, THEATER_FOLDER, THEATER_FALLBACK_IMAGE);
	}

	public String saveTicketQr(byte[] qr) {
		return upload(qr, QR_FOLDER, QR_FALLBACK_IMAGE);
	}

	/* ---------- Private helpers ---------- */

	private String upload(MultipartFile file, String folder, String fallbackImage) {
		if (file == null || file.isEmpty()) {
			return fallbackImage;
		}
		try {
			byte[] data = file.getBytes();
			String cloudinaryUrl = uploadToCloudinary(data, folder);
			if (cloudinaryUrl != null) {
				return cloudinaryUrl;
			}
			return saveLocal(data, folder, extensionFor(file));
		} catch (IOException e) {
			return fallbackImage;
		}
	}

	private String upload(byte[] data, String folder, String fallbackImage) {
		if (data == null || data.length == 0) {
			return fallbackImage;
		}
		try {
			String cloudinaryUrl = uploadToCloudinary(data, folder);
			if (cloudinaryUrl != null) {
				return cloudinaryUrl;
			}
			return saveLocal(data, folder, ".png");
		} catch (IOException e) {
			return fallbackImage;
		}
	}

	@SuppressWarnings("unchecked")
	private String uploadToCloudinary(byte[] data, String folder) {
		if (cloudinary == null) {
			return null;
		}
		try {
			Map<String, Object> params = ObjectUtils.asMap("folder", folder, "use_filename", true);
			return (String) cloudinary.uploader().upload(data, params).get("secure_url");
		} catch (IOException e) {
			return null;
		}
	}

	private String saveLocal(byte[] data, String folder, String extension) throws IOException {
		String safeFolder = folder.replaceAll("[^A-Za-z0-9._-]", "-");
		Path directory = uploadRoot.resolve(safeFolder);
		Files.createDirectories(directory);

		String fileName = UUID.randomUUID() + extension;
		Files.write(directory.resolve(fileName), data);
		return "/uploads/" + safeFolder + "/" + fileName;
	}

	private String extensionFor(MultipartFile file) {
		String originalName = StringUtils.cleanPath(file.getOriginalFilename() == null ? "" : file.getOriginalFilename());
		int dotIndex = originalName.lastIndexOf('.');
		if (dotIndex >= 0 && dotIndex < originalName.length() - 1) {
			String extension = originalName.substring(dotIndex).toLowerCase(Locale.ROOT);
			if (extension.matches("\\.(jpg|jpeg|png|gif|webp|svg)")) {
				return extension;
			}
		}

		String contentType = file.getContentType();
		if ("image/png".equals(contentType)) {
			return ".png";
		}
		if ("image/gif".equals(contentType)) {
			return ".gif";
		}
		if ("image/webp".equals(contentType)) {
			return ".webp";
		}
		if ("image/svg+xml".equals(contentType)) {
			return ".svg";
		}
		return ".jpg";
	}
}
