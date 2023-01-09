package com.example.servingwebcontent;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import tutorials.biometricss.EnrollFaceFromImage;
import tutorials.util.LibraryManager;
import tutorials.util.Utils;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

@Controller
public class EnrollFaceController {
    @RequestMapping("/enroll_face")
    @ResponseBody
    public Object enrollFace(@RequestParam("file") MultipartFile enrollFile, Model model) {
        Map<String, Object> result = new HashMap<>();
        try {
            System.out.println("Engine load status: " + EnrollFaceFromImage.loadEngine());
            System.out.println(enrollFile.getOriginalFilename());

            byte[] bytes = enrollFile.getBytes();
            String filePath = EnrollFaceFromImage.getUploadImagePath(enrollFile.getOriginalFilename());
            Path path = Paths.get(filePath);
            Files.write(path, bytes);
            EnrollFaceFromImage faceImage = new EnrollFaceFromImage();
            result.put("data", new String(faceImage.recognizeFile(filePath)));
        } catch (Exception e) {
            e.printStackTrace();
            result.put("data", "failed");
        }

        return result;
    }
}
