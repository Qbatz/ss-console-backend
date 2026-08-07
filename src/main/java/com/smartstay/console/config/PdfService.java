package com.smartstay.console.config;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Service
public class PdfService {

    public byte[] convertToPdf(byte[] fileBytes, String contentType, String fileName) throws Exception {

        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("File is empty");
        }

        String type = resolveContentType(contentType, fileName);

        if ("application/pdf".equalsIgnoreCase(type)) {
            return fileBytes;
        }

        if (type != null && type.startsWith("image/")) {
            return convertImageToPdf(fileBytes);
        }

        if ("text/plain".equalsIgnoreCase(type)) {
            return convertTextToPdf(fileBytes);
        }

        throw new IllegalArgumentException(
                "Unsupported file type for PDF conversion: " + type
        );
    }

    private byte[] convertImageToPdf(byte[] fileBytes) throws IOException {

        BufferedImage image = ImageIO.read(new ByteArrayInputStream(fileBytes));

        if (image == null) {
            throw new IOException("Invalid image");
        }

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output =
                        new ByteArrayOutputStream()
        ) {

            PDPage page = new PDPage();

            document.addPage(page);

            PDImageXObject pdfImage =
                    LosslessFactory.createFromImage(
                            document,
                            image
                    );

            float pageWidth =
                    page.getMediaBox().getWidth();

            float pageHeight =
                    page.getMediaBox().getHeight();

            float imageWidth = image.getWidth();
            float imageHeight = image.getHeight();

            float scale = Math.min(
                    pageWidth / imageWidth,
                    pageHeight / imageHeight
            );

            float width = imageWidth * scale;
            float height = imageHeight * scale;

            float x = (pageWidth - width) / 2;
            float y = (pageHeight - height) / 2;

            try (
                    PDPageContentStream contentStream =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                contentStream.drawImage(
                        pdfImage,
                        x,
                        y,
                        width,
                        height
                );
            }

            document.save(output);

            return output.toByteArray();
        }
    }

    private byte[] convertTextToPdf(byte[] fileBytes) throws IOException {

        String text = new String(fileBytes, StandardCharsets.UTF_8);

        try (
                PDDocument document = new PDDocument();
                ByteArrayOutputStream output = new ByteArrayOutputStream()
        ) {
            PDPage page = new PDPage();
            document.addPage(page);

            try (
                    PDPageContentStream contentStream =
                            new PDPageContentStream(
                                    document,
                                    page
                            )
            ) {

                contentStream.beginText();
                contentStream.setFont(
                        new PDType1Font(Standard14Fonts.FontName.HELVETICA),
                        10
                );

                contentStream.newLineAtOffset(
                        50,
                        750
                );

                for (String line : text.split("\n")) {

                    contentStream.showText(
                            line.length() > 100
                                    ? line.substring(0, 100)
                                    : line
                    );

                    contentStream.newLineAtOffset(
                            0,
                            -15
                    );
                }

                contentStream.endText();
            }

            document.save(output);

            return output.toByteArray();
        }
    }

    public String getFileNameWithoutExtension(String fileName) {

        int lastDot = fileName.lastIndexOf('.');

        if (lastDot <= 0) {
            return fileName;
        }

        return fileName.substring(0, lastDot);
    }

    private String resolveContentType(String contentType, String fileName) {

        if (contentType != null &&
                !contentType.isBlank() &&
                !"application/octet-stream".equalsIgnoreCase(contentType)) {

            return contentType;
        }

        String lowerFileName = fileName.toLowerCase();

        if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        }

        if (lowerFileName.endsWith(".jpg") ||
                lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        }

        if (lowerFileName.endsWith(".png")) {
            return "image/png";
        }

        if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        }

        if (lowerFileName.endsWith(".bmp")) {
            return "image/bmp";
        }

        if (lowerFileName.endsWith(".txt")) {
            return "text/plain";
        }

        return contentType;
    }
}
