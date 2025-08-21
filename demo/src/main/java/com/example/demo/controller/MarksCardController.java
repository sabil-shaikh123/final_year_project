package com.example.demo.controller;


import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RestController
@CrossOrigin(origins = "*")
public class MarksCardController {
    private static final List<String> SUBJECTS = Arrays.asList(
            "ENGLISH", "HINDI", "PHYSICS", "CHEMISTRY", "MATHEMATICS", "BIOLOGY"
    );
    private List<String> tempSubjects = Arrays.asList(
            "ENGLISH", "HINDI", "PHYSICS", "CHEMISTRY", "MATHEMATICS", "BIOLOGY"
    );
    @GetMapping("/h")
    public String test(){
        return "this is working";
    }
    @PostMapping("/image")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) return ResponseEntity.badRequest().body("File is empty");

        try {
            // Read image
            BufferedImage image = ImageIO.read(file.getInputStream());

            // OCR
            ITesseract tesseract = new Tesseract();
            // ❗ Set this path to where Tesseract is installed (change for Windows)
            tesseract.setDatapath("C:\\Program Files\\Tesseract-OCR\\tessdata"); // or "/usr/share/tesseract-ocr/4.00/tessdata" on Linux
            tesseract.setLanguage("eng");

            String extractedText = tesseract.doOCR(image);
            System.out.println("=== OCR Text ===\n" + extractedText);

            //printting all the extracted text
            System.out.println("the extracted text is :");
            System.out.println(extractedText);


            Map<String, Object> result = new HashMap<>();

//            // Extract name from first non-empty line
//            String[] lines = extractedText.split("\r?\n");
//            for (String line : lines) {
//                if (!line.trim().isEmpty()) {
//                    result.put("name", line.trim());
//                    break;
//                }
//            }

            // Extract subject marks
            Map<String, Integer> marksMap = new HashMap<>();
            for (String subject : SUBJECTS) {
//                if (tempSubjects.contains(subject)){
//                    int mark = extractSubjectMark(extractedText, subject);
//                    marksMap.put(subject, mark);
//                    tempSubjects.remove(subject);
//                }
                int mark = extractSubjectMark1(extractedText, subject);
                marksMap.put(subject, mark);
            }

//            result.put("marks", marksMap);
            System.out.println("the extracted marks are "+marksMap);
            return ResponseEntity.ok(marksMap);

        } catch (IOException | TesseractException e) {
            return ResponseEntity.status(500).body("Error processing image: " + e.getMessage());
        }
    }

    private int extractSubjectMark(String text, String subject) {
        String escapedSubject = Pattern.quote(subject);
        Pattern pattern = Pattern.compile("^.*" + escapedSubject + ".*?(\\d{2,3})\\D*$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            try {
                return Integer.parseInt(matcher.group(1));
            } catch (NumberFormatException e) {
                System.out.println("Invalid number for subject: " + subject);
            }
        } else {
            System.out.println("Subject not found or invalid format: " + subject);
        }

        return -1;
    }

    //this the new extractSubjeect mehthod
    private int extractSubjectMark1(String text, String subject) {
        String escapedSubject = Pattern.quote(subject);
        Pattern pattern = Pattern.compile("^.*" + escapedSubject + ".*$",
                Pattern.CASE_INSENSITIVE | Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(text);

        if (matcher.find()) {
            String line = matcher.group();  // full line containing the subject
            Matcher numMatcher = Pattern.compile("\\d{1,3}").matcher(line);

            List<Integer> numbers = new ArrayList<>();
            while (numMatcher.find()) {
                numbers.add(Integer.parseInt(numMatcher.group()));
            }

            // DEBUG
            System.out.println("Subject " + subject + " numbers: " + numbers);

            // Usually marks are the 3rd number (after RegNo & MaxMarks)
            if (numbers.size() >= 3) {
                return numbers.get(numbers.size()-2); // 0=RegNo, 1=MaxMarks, 2=Obtained
            }
        }
        return -1;
    }

}