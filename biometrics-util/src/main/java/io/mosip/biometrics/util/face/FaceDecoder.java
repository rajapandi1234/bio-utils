package io.mosip.biometrics.util.face;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

import io.mosip.biometrics.util.CommonUtil;
import io.mosip.biometrics.util.ConvertRequestDto;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;

public class FaceDecoder {
	private static final Logger LOGGER = LoggerFactory.getLogger(FaceDecoder.class);
	private static final String ISO_VERSION = "ISO19794_5_2011";

	private FaceDecoder() {
		throw new IllegalStateException("FaceDecoder class");
	}

	@SuppressWarnings({ "java:S100", "java:S112" })
	private static FaceBDIR getFaceBDIRISO19794_5_2011(byte[] isoData, int onlyImageInformation) throws Exception {
		try (ByteArrayInputStream bais = new ByteArrayInputStream(isoData);
				DataInputStream inputStream = new DataInputStream(bais);) {
			FaceBDIR faceBDIR = null;
			if (onlyImageInformation == 1)
				faceBDIR = new FaceBDIR(inputStream, true);
			else
				faceBDIR = new FaceBDIR(inputStream);
			return faceBDIR;
		}
	}

	public static FaceBDIR getFaceBDIR(ConvertRequestDto convertRequestDto) throws Exception {
		if (convertRequestDto.getVersion().equals(ISO_VERSION))
			return getFaceBDIRISO19794_5_2011(convertRequestDto.getInputBytes(),
					convertRequestDto.getOnlyImageInformation());
		throw new UnsupportedOperationException();
	}

	@SuppressWarnings({ "java:S100", "unused" })
	private static byte[] convertFaceISO19794_5_2011ToImage(byte[] isoData) throws Exception {
		ImageData imageData = getFaceBDIRISO19794_5_2011(isoData, 0).getRepresentation().getRepresentationData()
				.getImageData();
		try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
			ImageIO.write(ImageIO.read(new ByteArrayInputStream(imageData.getImage())), "jpg", baos);
			return baos.toByteArray();
		} catch (IOException e) {
			LOGGER.error("Failed to get jpg image", e);
		}
		return imageData.getImage();
	}

	public static BufferedImage convertFaceISOToBufferedImage(ConvertRequestDto convertRequestDto) throws Exception {
		if (convertRequestDto.getVersion().equals(ISO_VERSION)) {
			FaceBDIR faceBDIR = getFaceBDIRISO19794_5_2011(convertRequestDto.getInputBytes(),
					convertRequestDto.getOnlyImageInformation());
			return ImageIO.read(new ByteArrayInputStream(
					faceBDIR.getRepresentation().getRepresentationData().getImageData().getImage()));
		}
		throw new UnsupportedOperationException();
	}

	public static byte[] convertFaceISOToImageBytes(ConvertRequestDto convertRequestDto) throws Exception {
		if (convertRequestDto.getVersion().equals(ISO_VERSION)) {
			FaceBDIR faceBDIR = getFaceBDIRISO19794_5_2011(convertRequestDto.getInputBytes(),
					convertRequestDto.getOnlyImageInformation());
			int imageDataType = faceBDIR.getRepresentation().getRepresentationHeader().getImageInformation()
					.getImageDataType();
			if (imageDataType == ImageDataType.JPEG2000_LOSSY || imageDataType == ImageDataType.JPEG2000_LOSS_LESS)
				return CommonUtil.convertJP2ToJPEGUsingOpenCV(
						faceBDIR.getRepresentation().getRepresentationData().getImageData().getImage(),
						convertRequestDto.getCompressionRatio());
			else
				return faceBDIR.getRepresentation().getRepresentationData().getImageData().getImage();
		}
		throw new UnsupportedOperationException();
	}
}