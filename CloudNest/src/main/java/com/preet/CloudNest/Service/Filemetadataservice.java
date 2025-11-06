package com.preet.CloudNest.Service;

import com.preet.CloudNest.Documents.Filemetadatadocument;
import com.preet.CloudNest.Documents.Profiledocument;
import com.preet.CloudNest.Dto.FilemetadataDto;
import com.preet.CloudNest.Repository.Filemetadatarepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class Filemetadataservice {

    private final Profileservice profileservice;
    private final usercreditsservice usercreditsservice;
    private final Filemetadatarepository filemetadatarepository;

    public List<FilemetadataDto> uploadfiles(MultipartFile[] files) throws IOException {

        Profiledocument currentProfile = profileservice.getCurrentProfile();

        if (currentProfile == null) {
            throw new RuntimeException("User profile not found. Provide a valid JWT.");
        }


        List<Filemetadatadocument> savedfiles = new ArrayList<>();


        if (!usercreditsservice.hasEnoughCredits(files.length)) {
            throw new RuntimeException("Not enough credits to upload files");
        }

        Path uploadPath = Paths.get("uploads").toAbsolutePath().normalize();

        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw e;
        }


        for (int i = 0; i < files.length; i++) {
            MultipartFile file = files[i];

            String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(file.getOriginalFilename());

            Path targetLocation = uploadPath.resolve(fileName);

            try {
                Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
                System.out.println("DEBUG: File copied successfully");
            } catch (IOException e) {
                throw e;
            }

            Filemetadatadocument filemetadata = Filemetadatadocument.builder()
                    .fileLocation(targetLocation.toString())
                    .name(file.getOriginalFilename())
                    .size(file.getSize())
                    .type(file.getContentType())
                    .clerkId(currentProfile.getClerkId())
                    .isPublic(false)
                    .uploadedAt(LocalDateTime.now())
                    .build();

            try {
                Filemetadatadocument savedFile = filemetadatarepository.save(filemetadata);
                savedfiles.add(savedFile);
                System.out.println("DEBUG: File metadata saved successfully with ID: " + savedFile.getId());
            } catch (Exception e) {
                System.out.println("DEBUG: CRITICAL - Failed to save file metadata: " + e.getMessage());
                throw e;
            }

            try {
                usercreditsservice.consumecredits();
            } catch (Exception e) {

            }
        }

        List<FilemetadataDto> result = savedfiles.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());

        return result;
    }

    private FilemetadataDto mapToDto(Filemetadatadocument filemetadatadocument) {


        return FilemetadataDto.builder()
                .fileLocation(filemetadatadocument.getFileLocation())
                .id(filemetadatadocument.getId())
                .name(filemetadatadocument.getName())
                .size(filemetadatadocument.getSize())
                .type(filemetadatadocument.getType())
                .clerkId(filemetadatadocument.getClerkId())
                .isPublic(filemetadatadocument.isPublic())
                .uploadedAt(filemetadatadocument.getUploadedAt())
                .build();
    }

    public List<FilemetadataDto> getfiles() {

        Profiledocument currentprofile = profileservice.getCurrentProfile();

        if (currentprofile == null) {
            throw new RuntimeException("User profile not found. Provide a valid JWT.");
        }
        List<Filemetadatadocument> files = filemetadatarepository.findByClerkId(currentprofile.getClerkId());

        List<FilemetadataDto> result = files.stream().map(this::mapToDto).collect(Collectors.toList());

        return result;
    }

    public FilemetadataDto getpublicifle(String id){
        Optional<Filemetadatadocument> fileoptinal = filemetadatarepository.findById(id);
        if(fileoptinal.isEmpty() || !fileoptinal.get().isPublic()){
            throw new RuntimeException("File not found or is not public");
        }
        Filemetadatadocument document = fileoptinal.get();
        return mapToDto(document);

    }
    public FilemetadataDto getdownloadablefile(String id){
        Filemetadatadocument file = filemetadatarepository.findById(id).orElseThrow(()->new RuntimeException("file not found"));
        return mapToDto(file);
    }
    public void deltefile(String id){
        System.out.println("=== DELETE FILE OPERATION STARTED ===");
        System.out.println("File ID to delete: " + id);

        try {
            // Step 1: Get current profile
            System.out.println("Step 1: Getting current profile...");
            Profiledocument currentpofile = profileservice.getCurrentProfile();
            if (currentpofile == null) {
                System.out.println("ERROR: Current profile is null");
                throw new RuntimeException("User profile not found. Provide a valid JWT.");
            }
            System.out.println("✅ Current profile found - ClerkId: " + currentpofile.getClerkId());

            // Step 2: Find file in database
            System.out.println("Step 2: Finding file in database...");
            Filemetadatadocument file = filemetadatarepository.findById(id)
                    .orElseThrow(() -> {
                        System.out.println("ERROR: File not found in database with ID: " + id);
                        return new RuntimeException("File not found");
                    });
            System.out.println("✅ File found in database:");
            System.out.println("  - File ID: " + file.getId());
            System.out.println("  - File Name: " + file.getName());
            System.out.println("  - File Location: " + file.getFileLocation());
            System.out.println("  - Owner ClerkId: " + file.getClerkId());

            // Step 3: Check ownership
            System.out.println("Step 3: Checking file ownership...");
            if (!file.getClerkId().equals(currentpofile.getClerkId())) {
                System.out.println("ERROR: File ownership mismatch!");
                System.out.println("  - File owner: " + file.getClerkId());
                System.out.println("  - Current user: " + currentpofile.getClerkId());
                throw new RuntimeException("File does not belong to current user");
            }
            System.out.println("✅ File ownership verified");

            // Step 4: Delete physical file
            System.out.println("Step 4: Deleting physical file...");
            Path filepath = Paths.get(file.getFileLocation());
            System.out.println("  - Full file path: " + filepath.toAbsolutePath());
            System.out.println("  - File exists: " + Files.exists(filepath));
            System.out.println("  - File is readable: " + Files.isReadable(filepath));
            System.out.println("  - File is writable: " + Files.isWritable(filepath));

            boolean fileDeleted = Files.deleteIfExists(filepath);
            System.out.println("  - Physical file deletion result: " + fileDeleted);

            if (Files.exists(filepath)) {
                System.out.println("WARNING: Physical file still exists after deletion attempt!");
            } else {
                System.out.println("✅ Physical file successfully deleted");
            }

            // Step 5: Delete from database
            System.out.println("Step 5: Deleting from database...");
            System.out.println("  - About to delete record with ID: " + id);

            // Check if record exists before deletion
            boolean existsBeforeDeletion = filemetadatarepository.existsById(id);
            System.out.println("  - Record exists before deletion: " + existsBeforeDeletion);

            filemetadatarepository.deleteById(id);

            // Verify deletion
            boolean existsAfterDeletion = filemetadatarepository.existsById(id);
            System.out.println("  - Record exists after deletion: " + existsAfterDeletion);

            if (existsAfterDeletion) {
                System.out.println("ERROR: Database record still exists after deletion!");
                throw new RuntimeException("Failed to delete file record from database");
            } else {
                System.out.println("✅ Database record successfully deleted");
            }

            System.out.println("=== DELETE FILE OPERATION COMPLETED SUCCESSFULLY ===");

        } catch (Exception e) {
            System.out.println("=== DELETE FILE OPERATION FAILED ===");
            System.out.println("Error type: " + e.getClass().getSimpleName());
            System.out.println("Error message: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error deleting file: " + e.getMessage());
        }
    }

    public FilemetadataDto togglepublic(String id){
        Filemetadatadocument file = filemetadatarepository.findById(id)
                .orElseThrow(()->new RuntimeException("file not found"));
        file.setPublic(!file.isPublic());
        filemetadatarepository.save(file);
        return mapToDto(file);


    }


//    public List<FilemetadataDto> getfiles(){
//        Profiledocument currentprofile = profileservice.getCurrentProfile();
//       List<Filemetadatadocument> files =  filemetadatarepository.findByClerkId(currentprofile.getClerkId());
//       files.stream().map(this::mapToDto).collect(Collectors.toList());
//    }
}